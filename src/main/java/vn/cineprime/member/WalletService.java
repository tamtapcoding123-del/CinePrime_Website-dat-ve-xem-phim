package vn.cineprime.member;

import java.security.SecureRandom;
import java.time.*;
import java.util.*;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.cineprime.domain.Store;
import vn.cineprime.notification.Notifications;
import static vn.cineprime.domain.Domain.*;

/**
 * Member benefits after the tier itself: wallet vouchers (issued, redeemed with points, held by one pending order, used on payment),
 * points adjustments, birthday/yearly/next-purchase vouchers, gifts handed over at the counter, campaigns and member notices.
 * Every issue is keyed by (customer, campaign key) with a unique index, so a re-run job or a retried request never issues twice.
 * Lock order: customer → voucher template → wallet copy; gifts: staff → grant → product.
 */
@Service @Transactional
public class WalletService {
    private static final Pattern KEY=Pattern.compile("[A-Za-z0-9:_-]{6,80}");
    private static final char[] ALPHABET="ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private final SecureRandom random=new SecureRandom();
    private final Store db;private final Clock clock;private final TierService tiers;private final Notifications notices;
    public WalletService(Store db,Clock clock,TierService tiers,Notifications notices){this.db=db;this.clock=clock;this.tiers=tiers;this.notices=notices;}
    private Instant now(){return clock.instant();}
    private LocalDate today(){return now().atZone(TierService.ZONE).toLocalDate();}
    private static String requestKey(String key){if(key==null||!KEY.matcher(key).matches())throw new IllegalArgumentException("Thiếu mã yêu cầu. Tải lại trang rồi thử lại.");return key;}
    private User customer(Long id){User u=db.lock(User.class,id);if(!"CUSTOMER".equals(u.role))throw new IllegalArgumentException("Chỉ áp dụng cho tài khoản khách hàng.");return u;}
    private Instant endOfMonth(LocalDate day){return day.withDayOfMonth(1).plusMonths(1).atStartOfDay(TierService.ZONE).toInstant();}

    // ---------- Issuing ----------
    /** Caller holds the customer lock. Returns the existing copy when this campaign key was already issued to the customer. */
    public UserVoucher issue(User u,Voucher template,String source,String key,Instant expires,Long actor,boolean notify){
        Optional<UserVoucher> existing=db.first(UserVoucher.class,"where e.userId=?1 and e.campaignKey=?2",u.id,key);if(existing.isPresent())return existing.get();
        Voucher v=db.lock(Voucher.class,template.id);
        if(!"PERSONAL".equals(v.audience))throw new IllegalArgumentException("Voucher "+v.code+" là mã công khai, không phát vào ví.");
        if(!v.active||now().isBefore(v.startsAt)||!now().isBefore(v.endsAt))throw new IllegalArgumentException("Voucher "+v.code+" đang tắt hoặc ngoài thời gian phát hành.");
        if(db.count(UserVoucher.class,"where e.voucherId=?1",v.id)>=v.quota)throw new IllegalArgumentException("Voucher "+v.code+" đã phát hết số lượng.");
        UserVoucher uv=new UserVoucher();uv.userId=u.id;uv.voucherId=v.id;uv.code=code();uv.source=source;uv.campaignKey=key;uv.issuedAt=now();uv.expiresAt=expires;uv.actorId=actor;db.save(uv);
        if(notify)notices.notify(u,"wallet:"+uv.id,"Bạn nhận được voucher mới · CinePrime",v.description+"\nMã của bạn: "+uv.code+"\nHạn dùng: "+db.date(uv.expiresAt)+"\nXem trong mục Ví voucher của tài khoản.",false);
        return uv;
    }
    private String code(){for(int attempt=0;attempt<20;attempt++){StringBuilder s=new StringBuilder("CP");for(int i=0;i<8;i++)s.append(ALPHABET[random.nextInt(ALPHABET.length)]);String c=s.toString();if(db.first(UserVoucher.class,"where e.code=?1",c).isEmpty()&&db.first(Voucher.class,"where e.code=?1",c).isEmpty())return c;}throw new IllegalStateException("Không tạo được mã voucher.");}
    /** Template may be off or exhausted; automatic jobs skip it instead of failing the customer's whole run. */
    private boolean tryIssue(User u,Long templateId,String source,String key,Instant expires){
        if(templateId==null)return false;if(db.first(UserVoucher.class,"where e.userId=?1 and e.campaignKey=?2",u.id,key).isPresent())return false;
        try{issue(u,db.get(Voucher.class,templateId),source,key,expires,null,true);return true;}catch(IllegalArgumentException skipped){return false;}
    }
    /** Birth-month vouchers and the tier gift. The date must have been saved before the birth month began (no last-minute edits). */
    public int birthday(Long userId){
        User u=db.lock(User.class,userId);if(!"CUSTOMER".equals(u.role)||!u.enabled||u.birthDate==null||u.birthDateSetAt==null)return 0;
        LocalDate day=today();if(u.birthDate.getMonthValue()!=day.getMonthValue())return 0;
        if(!u.birthDateSetAt.isBefore(day.withDayOfMonth(1).atStartOfDay(TierService.ZONE).toInstant()))return 0;
        Tier t=tiers.refresh(u);int issued=0;Instant until=endOfMonth(day);
        for(int n=1;n<=t.birthdayVouchers;n++)if(tryIssue(u,t.birthdayVoucherId,"BIRTHDAY","bday:"+day.getYear()+":"+n,until))issued++;
        if(t.birthdayGiftId!=null&&db.first(GiftGrant.class,"where e.userId=?1 and e.campaignKey=?2",u.id,"bday-gift:"+day.getYear()).isEmpty()){grant(u,db.get(Product.class,t.birthdayGiftId),"BIRTHDAY","bday-gift:"+day.getYear(),until.plus(Duration.ofDays(30)),null,"Quà sinh nhật hạng "+t.name);issued++;}
        return issued;
    }
    /** Yearly vouchers of the current tier; numbered per year so moving up mid-year adds only the missing ones. */
    public int yearly(Long userId){
        User u=db.lock(User.class,userId);if(!"CUSTOMER".equals(u.role)||!u.enabled)return 0;Tier t=tiers.refresh(u);int y=today().getYear(),issued=0;
        Instant until=LocalDate.of(y+1,1,1).atStartOfDay(TierService.ZONE).toInstant();
        for(int n=1;n<=t.yearlyVouchers;n++)if(tryIssue(u,t.yearlyVoucherId,"YEARLY","year:"+y+":"+n,until))issued++;
        return issued;
    }
    /** "Voucher for the next purchase": once per paid order (keyed by the order), caller holds the customer lock. */
    public void afterPayment(User u,Order o){if(!"CUSTOMER".equals(u.role))return;Tier t=tiers.refresh(u);if(t.nextVoucherId!=null){Voucher v=db.get(Voucher.class,t.nextVoucherId);tryIssue(u,v.id,"NEXT","next:"+o.id,now().plus(Duration.ofDays(v.validDays)));}}

    /** A cancelled sale takes back its unused next-purchase voucher (a used one stays used). */
    public void cancelNext(Long userId,Long orderId){db.first(UserVoucher.class,"where e.userId=?1 and e.campaignKey=?2",userId,"next:"+orderId).ifPresent(snapshot->{UserVoucher uv=db.lock(UserVoucher.class,snapshot.id);if("ISSUED".equals(uv.status)){uv.status="REVOKED";uv.orderId=null;uv.reservedUntil=null;}});}

    // ---------- Using a wallet copy on an order ----------
    public boolean usable(UserVoucher uv,Long orderId){return "ISSUED".equals(uv.status)&&now().isBefore(uv.expiresAt)&&(uv.orderId==null||uv.orderId.equals(orderId)||uv.reservedUntil==null||!now().isBefore(uv.reservedUntil));}
    /** Caller holds the order (and its owner) lock. Returns the wallet copy with this code, locked and checked, or empty when the code is not a wallet code. */
    public Optional<UserVoucher> claim(Order o,Long userId,String code){
        Optional<UserVoucher> found=db.first(UserVoucher.class,"where e.code=?1",code);if(found.isEmpty())return found;
        UserVoucher uv=db.lock(UserVoucher.class,found.get().id);
        if(userId==null||!uv.userId.equals(userId))throw new IllegalArgumentException("Mã ưu đãi không tồn tại.");
        if(!usable(uv,o.id))throw new IllegalArgumentException("USED".equals(uv.status)?"Voucher đã được dùng.":"REVOKED".equals(uv.status)?"Voucher đã bị thu hồi.":!now().isBefore(uv.expiresAt)?"Voucher đã hết hạn.":"Voucher đang được giữ cho một đơn khác.");
        Voucher v=db.get(Voucher.class,uv.voucherId);if(!v.active)throw new IllegalArgumentException("Voucher tạm ngừng áp dụng.");
        if(v.minTier!=null&&tiers.tier(db.get(User.class,userId).tierCode).rankOrder<tiers.tier(v.minTier).rankOrder)throw new IllegalArgumentException("Voucher dành cho hạng "+tiers.tier(v.minTier).name+" trở lên.");
        return Optional.of(uv);
    }
    public void hold(UserVoucher uv,Order o){uv.orderId=o.id;uv.reservedUntil=o.expiresAt;}
    /** Frees the copy held by this order (cart change, expiry, cancel). */
    public void release(Long orderId){for(UserVoucher snapshot:db.list(UserVoucher.class,"where e.orderId=?1 and e.status='ISSUED'",orderId)){UserVoucher uv=db.lock(UserVoucher.class,snapshot.id);if("ISSUED".equals(uv.status)&&orderId.equals(uv.orderId)){uv.orderId=null;uv.reservedUntil=null;}}}
    public void use(Order o){if(o.userVoucherId==null)return;UserVoucher uv=db.lock(UserVoucher.class,o.userVoucherId);if("ISSUED".equals(uv.status)&&o.id.equals(uv.orderId)){uv.status="USED";uv.usedAt=now();uv.reservedUntil=null;}}

    /**
     * Voucher amount on what is left after the member price. Lines already carry their tier discount.
     * ORDER/TICKET/COMBO: FIXED or PERCENT of that base, capped by maximum. FREE_TICKET: the dearest non-couple seat, capped by maximum.
     * COMBO_UPGRADE: for up to value combos B in the cart that a smaller combo A upgrades to, the price gap B − A (largest gap first).
     */
    public long discount(Order o,Voucher v,long net){
        List<OrderSeat> seats=db.list(OrderSeat.class,"where e.orderId=?1 order by e.id",o.id);List<OrderProduct> lines=db.list(OrderProduct.class,"where e.orderId=?1 order by e.id",o.id);
        long seatNet=seats.stream().mapToLong(l->l.price-l.discount).sum(),foodNet=0;Map<Long,Product> products=new HashMap<>();
        for(OrderProduct l:lines){Product p=db.get(Product.class,l.productId);products.put(p.id,p);if(!"MERCH".equals(p.kind))foodNet=Math.addExact(foodNet,Math.multiplyExact(l.price,(long)l.quantity)-l.discount);}
        long amount=switch(v.scope){
            case "TICKET"->percentOrFixed(v,seatNet);
            case "COMBO"->percentOrFixed(v,foodNet);
            case "FREE_TICKET"->{long best=seats.stream().filter(l->!"COUPLE".equals(l.seatType)).mapToLong(l->l.price-l.discount).max().orElse(0);yield v.maximum>0?Math.min(best,v.maximum):best;}
            case "COMBO_UPGRADE"->{List<Long> gaps=new ArrayList<>();
                for(OrderProduct l:lines){Product big=products.get(l.productId);if(!"COMBO".equals(big.kind))continue;
                    long gap=db.list(Product.class,"where e.upgradeToId=?1",big.id).stream().mapToLong(small->big.price-small.price).filter(g->g>0).max().orElse(0);
                    long unitNet=l.price-l.discount/Math.max(1,l.quantity);for(int i=0;i<l.quantity;i++)gaps.add(Math.min(gap,unitNet));}
                gaps.sort(Comparator.reverseOrder());yield gaps.stream().limit(Math.max(1,v.value)).mapToLong(Long::longValue).sum();}
            default->percentOrFixed(v,net);
        };
        return Math.max(0,Math.min(net,amount));
    }
    private static long percentOrFixed(Voucher v,long base){long d="PERCENT".equals(v.kind)?Math.multiplyExact(base,v.value)/100:v.value;if(v.maximum>0)d=Math.min(d,v.maximum);return Math.min(base,d);}

    // ---------- Points ----------
    @Transactional(readOnly=true) public long balance(Long userId){return db.query("select coalesce(sum(p.points),0) from "+db.entityName(Point.class)+" p where p.userId=?1",Long.class,userId).getFirst();}
    /** Spends points on a redeemable template; the request key makes a double click or retry return the same copy. */
    public UserVoucher redeem(Long userId,Long voucherId,String key){
        requestKey(key);User u=customer(userId);if(!u.enabled)throw new IllegalArgumentException("Tài khoản đã khóa.");
        Optional<UserVoucher> done=db.first(UserVoucher.class,"where e.userId=?1 and e.campaignKey=?2",u.id,"redeem:"+key);if(done.isPresent())return done.get();
        Voucher v=db.get(Voucher.class,voucherId);if(!"PERSONAL".equals(v.audience)||v.pointsCost<=0)throw new IllegalArgumentException("Voucher này không đổi bằng điểm.");
        if(v.minTier!=null&&tiers.refresh(u).rankOrder<tiers.tier(v.minTier).rankOrder)throw new IllegalArgumentException("Voucher dành cho hạng "+tiers.tier(v.minTier).name+" trở lên.");
        long have=balance(u.id);if(have<v.pointsCost)throw new IllegalArgumentException("Bạn có "+have+" điểm, cần "+v.pointsCost+" điểm để đổi.");
        UserVoucher uv=issue(u,v,"REDEEM","redeem:"+key,now().plus(Duration.ofDays(v.validDays)),null,false);
        Point p=new Point();p.userId=u.id;p.kind="REDEEM";p.points=-v.pointsCost;p.ref="redeem:"+uv.id;p.note="Đổi "+v.code;p.createdAt=now();db.save(p);
        db.audit(u.id,"POINTS_REDEEM",v.code+" · −"+v.pointsCost+" · "+uv.code);return uv;
    }
    /** Admin correction with a reason; the balance never goes below zero. */
    public void adjust(Long actor,Long userId,long delta,String note,String key){
        requestKey(key);String text=note==null?"":note.trim();if(text.isEmpty()||text.length()>500)throw new IllegalArgumentException("Nhập lý do điều chỉnh điểm (tối đa 500 ký tự).");
        if(delta==0||Math.abs(delta)>1_000_000)throw new IllegalArgumentException("Số điểm điều chỉnh khác 0, tối đa ±1.000.000.");
        User u=customer(userId);if(db.first(Point.class,"where e.ref=?1","adjust:"+key).isPresent())return;
        if(balance(u.id)+delta<0)throw new IllegalArgumentException("Khách chỉ còn "+balance(u.id)+" điểm, không thể trừ "+(-delta)+" điểm.");
        Point p=new Point();p.userId=u.id;p.kind="ADJUST";p.points=delta;p.ref="adjust:"+key;p.note=text;p.actorId=actor;p.createdAt=now();db.save(p);
        notices.notify(u,"points:"+p.ref,"Điểm thưởng được điều chỉnh · CinePrime",(delta>0?"Cộng ":"Trừ ")+Math.abs(delta)+" điểm. Lý do: "+text,false);
        db.audit(actor,"POINTS_ADJUST","#"+u.id+" · "+(delta>0?"+":"")+delta+" · "+text);
    }

    // ---------- Birth date ----------
    /** The customer sets it once; later changes go through Admin (audit). */
    public void birthDate(Long userId,LocalDate date,Long actor){
        User u=customer(userId);if(date==null)throw new IllegalArgumentException("Chọn ngày sinh.");
        if(date.isAfter(today())||date.isBefore(today().minusYears(120)))throw new IllegalArgumentException("Ngày sinh không hợp lệ.");
        if(actor==null&&u.birthDate!=null)throw new IllegalArgumentException("Ngày sinh đã lưu. Liên hệ CinePrime nếu cần sửa.");
        if(actor!=null)db.audit(actor,"BIRTHDATE_SET","#"+u.id+" · "+u.birthDate+" → "+date);
        u.birthDate=date;u.birthDateSetAt=now();
    }

    // ---------- Gifts ----------
    public GiftGrant grant(User u,Product p,String reason,String key,Instant expires,Long actor,String note){
        Optional<GiftGrant> existing=db.first(GiftGrant.class,"where e.userId=?1 and e.campaignKey=?2",u.id,key);if(existing.isPresent())return existing.get();
        if(!"MERCH".equals(p.kind))throw new IllegalArgumentException("Quà tặng phải là sản phẩm lưu niệm.");
        GiftGrant g=new GiftGrant();g.userId=u.id;g.productId=p.id;g.reason=reason;g.campaignKey=key;g.issuedAt=now();g.expiresAt=expires;g.actorId=actor;g.note=note;db.save(g);
        notices.notify(u,"gift:"+g.id,"Bạn có quà tặng tại quầy · CinePrime","Quà: "+p.name+(note==null?"":"\n"+note)+"\nNhận tại quầy bất kỳ rạp CinePrime trước "+db.date(expires)+". Báo nhân viên số điện thoại hoặc email tài khoản.",false);return g;
    }
    /** Staff hands a gift over at their cinema: idempotent, takes one item of stock. */
    public GiftGrant handOver(Long staffId,Long grantId){
        User staff=db.lock(User.class,staffId);if(!"STAFF".equals(staff.role)||!staff.enabled||staff.cinemaId==null)throw new IllegalArgumentException("Tài khoản không có quyền trao quà.");
        GiftGrant g=db.lock(GiftGrant.class,grantId);if("HANDED_OVER".equals(g.status))return g;
        if(!"ISSUED".equals(g.status))throw new IllegalArgumentException("Quà đã bị thu hồi.");if(!now().isBefore(g.expiresAt))throw new IllegalArgumentException("Quà đã hết hạn nhận.");
        Product p=db.lock(Product.class,g.productId);if(p.stock!=null){if(p.stock<1)throw new IllegalArgumentException(p.name+" đã hết hàng tại kho. Báo quản lý nhập thêm.");p.stock=p.stock-1;}
        g.status="HANDED_OVER";g.handedAt=now();g.handedBy=staffId;g.cinemaId=staff.cinemaId;db.audit(staffId,"GIFT_HANDOVER","#"+g.id+" · "+p.name+" · khách #"+g.userId);return g;
    }
    public void revokeGift(Long actor,Long grantId){GiftGrant g=db.lock(GiftGrant.class,grantId);if(!"ISSUED".equals(g.status))throw new IllegalArgumentException("Chỉ thu hồi quà chưa trao.");g.status="REVOKED";db.audit(actor,"GIFT_REVOKE","#"+g.id);}
    public void revokeVoucher(Long actor,Long id){UserVoucher uv=db.lock(UserVoucher.class,id);if(!"ISSUED".equals(uv.status))throw new IllegalArgumentException("Chỉ thu hồi voucher chưa dùng.");if(uv.orderId!=null&&uv.reservedUntil!=null&&now().isBefore(uv.reservedUntil))throw new IllegalArgumentException("Voucher đang được giữ cho một đơn. Thử lại sau 5 phút.");uv.status="REVOKED";uv.orderId=null;uv.reservedUntil=null;db.audit(actor,"VOUCHER_REVOKE",uv.code);}

    // ---------- Admin: one customer, campaigns, notices ----------
    public UserVoucher give(Long actor,Long userId,Long voucherId,String key){requestKey(key);User u=customer(userId);Voucher v=db.get(Voucher.class,voucherId);UserVoucher uv=issue(u,v,"ADMIN","admin:"+key,now().plus(Duration.ofDays(v.validDays)),actor,true);db.audit(actor,"VOUCHER_GIVE","#"+u.id+" · "+v.code+" · "+uv.code);return uv;}
    public GiftGrant giveGift(Long actor,Long userId,Long productId,String note,String key){requestKey(key);User u=customer(userId);GiftGrant g=grant(u,db.get(Product.class,productId),"ADMIN","admin-gift:"+key,now().plus(Duration.ofDays(60)),actor,note==null||note.isBlank()?null:note.trim());db.audit(actor,"GIFT_GIVE","#"+u.id+" · #"+g.id);return g;}
    /** Customers at or above a tier, one transaction each through the proxy: returns ids for the caller to loop. */
    @Transactional(readOnly=true) public List<Long> audience(String minTier){
        int rank=minTier==null||minTier.isBlank()?0:tiers.tier(minTier).rankOrder;
        return db.query("select u.id from "+db.entityName(User.class)+" u, "+db.entityName(Tier.class)+" t where u.role='CUSTOMER' and u.enabled=true and u.tierCode=t.code and t.rankOrder>=?1 order by u.id",Long.class,rank);
    }
    /** One customer's share of a campaign: count copies of a template (and/or a gift), keyed by the campaign name. */
    public int campaign(Long actor,Long userId,Long voucherId,int count,Long productId,String campaign,String message){
        User u=db.lock(User.class,userId);int done=0;
        if(voucherId!=null){Voucher v=db.get(Voucher.class,voucherId);for(int n=1;n<=count;n++)if(db.first(UserVoucher.class,"where e.userId=?1 and e.campaignKey=?2",u.id,"camp:"+campaign+":"+n).isEmpty()){issue(u,v,"CAMPAIGN","camp:"+campaign+":"+n,now().plus(Duration.ofDays(v.validDays)),actor,true);done++;}}
        if(productId!=null&&db.first(GiftGrant.class,"where e.userId=?1 and e.campaignKey=?2",u.id,"camp-gift:"+campaign).isEmpty()){grant(u,db.get(Product.class,productId),"CAMPAIGN","camp-gift:"+campaign,now().plus(Duration.ofDays(60)),actor,message);done++;}
        if(message!=null&&!message.isBlank()&&voucherId==null&&productId==null)notices.notify(u,"notice:"+campaign+":"+u.id,"Thông báo từ CinePrime",message.trim(),false);
        return done;
    }
    public static String campaignName(String raw){String c=raw==null?"":raw.trim().toUpperCase(Locale.ROOT);if(!c.matches("[A-Z0-9_-]{3,40}"))throw new IllegalArgumentException("Mã chiến dịch 3–40 ký tự: chữ không dấu, số, - hoặc _. Dùng lại cùng mã sẽ không phát trùng.");return c;}
}
