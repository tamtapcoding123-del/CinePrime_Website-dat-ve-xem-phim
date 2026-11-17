package vn.cineprime.member;

import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.cineprime.domain.Store;
import vn.cineprime.notification.Notifications;
import static vn.cineprime.domain.Domain.*;

/**
 * Membership tiers. Qualifying spend = amount actually paid on PAID orders (online and counter) in one calendar year, Asia/Ho_Chi_Minh;
 * a cancelled sale drops out by itself. Earned tier = highest tier whose minimum ≤ max(spend this year, spend last year), so a tier
 * earned in year Y is kept through 31/12 of Y+1. The cached tier moves up as soon as it is earned and moves down only at the first
 * refresh of a new year (yearly review). Callers hold the customer's row lock before refresh/review; prices use the tier
 * percentages snapshotted on the order, never the current configuration.
 */
@Service @Transactional
public class TierService {
    public static final ZoneId ZONE=ZoneId.of("Asia/Ho_Chi_Minh");
    public static final List<String> CODES=List.of("SILVER","GOLD","PLATINUM","DIAMOND","VIP");
    private final Store db;private final Clock clock;private final Notifications notices;
    public TierService(Store db,Clock clock,Notifications notices){this.db=db;this.clock=clock;this.notices=notices;}

    @Transactional(readOnly=true) public List<Tier> tiers(){return db.list(Tier.class,"order by e.rankOrder");}
    @Transactional(readOnly=true) public Tier tier(String code){return db.first(Tier.class,"where e.code=?1",code).orElseThrow(()->new IllegalArgumentException("Hạng thành viên không tồn tại."));}
    public int year(){return clock.instant().atZone(ZONE).getYear();}
    public Instant now(){return clock.instant();}
    @Transactional(readOnly=true) public long spend(Long userId,int year){
        Instant from=LocalDate.of(year,1,1).atStartOfDay(ZONE).toInstant(),until=LocalDate.of(year+1,1,1).atStartOfDay(ZONE).toInstant();
        return db.query("select coalesce(sum(o.total),0) from "+db.entityName(Order.class)+" o where o.userId=?1 and o.status='PAID' and o.paidAt>=?2 and o.paidAt<?3",Long.class,userId,from,until).getFirst();
    }
    /** Highest tier whose minimum the amount reaches (tiers are ordered by rank with strictly increasing minimums, the lowest at 0). */
    public Tier earned(List<Tier> tiers,long amount){Tier best=tiers.getFirst();for(Tier t:tiers)if(t.minSpend<=amount)best=t;return best;}
    /** What a refresh would leave on this customer now, without writing: used by read screens and the counter price preview. */
    @Transactional(readOnly=true) public Tier effective(User u){
        Tier current=tier(u.tierCode);if(!"CUSTOMER".equals(u.role))return current;
        int y=year();Tier target=earned(tiers(),Math.max(spend(u.id,y),spend(u.id,y-1)));
        boolean review=u.tierReviewYear==null||u.tierReviewYear<y;
        return review||target.rankOrder>current.rankOrder?target:current;
    }
    /** Caller holds the lock on u. Applies an upgrade, or the yearly review, and records/notifies a change. */
    public Tier refresh(User u){
        if(!"CUSTOMER".equals(u.role))return tier(u.tierCode);
        int y=year();long amount=Math.max(spend(u.id,y),spend(u.id,y-1));Tier current=tier(u.tierCode),target=earned(tiers(),amount);
        boolean review=u.tierReviewYear==null||u.tierReviewYear<y;
        if(review?target.rankOrder!=current.rankOrder:target.rankOrder>current.rankOrder)change(u,current,target,review?"ANNUAL_REVIEW":"UPGRADE",amount,null,null);
        u.tierReviewYear=y;return tier(u.tierCode);
    }
    /** One customer in its own transaction (called through the proxy by the daily job and the Admin button); true when the tier changed. */
    public boolean review(Long userId){User u=db.lock(User.class,userId);String before=u.tierCode;refresh(u);return !before.equals(u.tierCode);}
    /** Admin override with a reason; it holds until the next yearly review, and later spend can still move it up. */
    public void assign(Long actorId,Long userId,String code,String note){
        String text=note==null?"":note.trim();if(text.isEmpty()||text.length()>500)throw new IllegalArgumentException("Nhập lý do điều chỉnh hạng (tối đa 500 ký tự).");
        User u=db.lock(User.class,userId);if(!"CUSTOMER".equals(u.role))throw new IllegalArgumentException("Chỉ điều chỉnh hạng cho tài khoản khách hàng.");
        Tier current=tier(u.tierCode),target=tier(code);if(current.code.equals(target.code))throw new IllegalArgumentException("Khách đang ở hạng "+target.name+".");
        int y=year();change(u,current,target,"ADMIN",Math.max(spend(u.id,y),spend(u.id,y-1)),text,actorId);u.tierReviewYear=y;
        db.audit(actorId,"TIER_ASSIGN","#"+u.id+" · "+current.code+" → "+target.code+" · "+text);
    }
    private void change(User u,Tier from,Tier to,String reason,long amount,String note,Long actor){
        TierChange c=new TierChange();c.userId=u.id;c.fromCode=from.code;c.toCode=to.code;c.reason=reason;c.spend=amount;c.note=note;c.actorId=actor;c.createdAt=clock.instant();db.save(c);
        u.tierCode=to.code;u.tierSince=clock.instant();
        boolean up=to.rankOrder>from.rankOrder;
        notices.notify(u,"tier:"+u.id+":"+to.code+":"+year()+":"+reason,up?"Chúc mừng bạn lên hạng "+to.name+" · CinePrime":"Hạng thành viên của bạn: "+to.name,
            (up?"Bạn đã đạt hạng "+to.name+".":"Hạng thành viên năm "+year()+" của bạn là "+to.name+".")+(to.ticketPct+to.comboPct>0?"\nƯu đãi tự động: giảm "+to.ticketPct+"% giá vé và "+to.comboPct+"% combo bắp nước.":"")+"\nXem quyền lợi tại mục Điểm thưởng trong tài khoản CinePrime.",up);
    }

    /** Tier discount of the order's current lines with its snapshotted percentages; writes the line discounts and order.tierDiscount. */
    public long applyTier(Order o){
        long sum=0;
        for(OrderSeat l:db.list(OrderSeat.class,"where e.orderId=?1 order by e.id",o.id)){l.discount=Math.multiplyExact(l.price,(long)o.tierTicketPct)/100;sum=Math.addExact(sum,l.discount);}
        for(OrderProduct l:db.list(OrderProduct.class,"where e.orderId=?1 order by e.id",o.id)){if("MERCH".equals(db.get(Product.class,l.productId).kind)){l.discount=0;continue;}l.discount=Math.multiplyExact(Math.multiplyExact(l.price,(long)o.tierComboPct)/100,(long)l.quantity);sum=Math.addExact(sum,l.discount);}
        o.tierDiscount=sum;return sum;
    }
    /** Snapshots the tier on the order (null tier = no member price) and reprices it; the caller adds any voucher on top. */
    public void price(Order o,Tier t){o.tierCode=t==null?null:t.code;o.tierTicketPct=t==null?0:t.ticketPct;o.tierComboPct=t==null?0:t.comboPct;applyTier(o);o.discount=o.tierDiscount;o.total=Math.subtractExact(o.subtotal,o.discount);}
    /** Read-only price of the order's lines for a tier (counter preview before the customer is attached at payment). */
    @Transactional(readOnly=true) public long quote(Order o,Tier t){
        long sum=0;if(t==null)return 0;
        for(OrderSeat l:db.list(OrderSeat.class,"where e.orderId=?1",o.id))sum=Math.addExact(sum,Math.multiplyExact(l.price,(long)t.ticketPct)/100);
        for(OrderProduct l:db.list(OrderProduct.class,"where e.orderId=?1",o.id))if(!"MERCH".equals(db.get(Product.class,l.productId).kind))sum=Math.addExact(sum,Math.multiplyExact(Math.multiplyExact(l.price,(long)t.comboPct)/100,(long)l.quantity));
        return sum;
    }

    /**
     * Member-only showtime and early sales. A null tier is a walk-in (no account): it may book only public showtimes once public sales are open.
     * Before publicSaleAt, a tier with earlyAccessHours may book from publicSaleAt − that many hours.
     */
    @Transactional(readOnly=true) public void gate(Showtime s,Tier t){
        if(s.minTier!=null){Tier need=tier(s.minTier);if(t==null||t.rankOrder<need.rankOrder)throw new IllegalArgumentException("Suất chiếu dành riêng cho hội viên hạng "+need.name+" trở lên"+(t==null?". Hãy gắn tài khoản khách.":"."));}
        if(s.publicSaleAt!=null&&clock.instant().isBefore(s.publicSaleAt)){int hours=t==null?0:t.earlyAccessHours;Instant early=s.publicSaleAt.minusSeconds(hours*3600L);
            if(hours==0||clock.instant().isBefore(early))throw new IllegalArgumentException("Suất mở bán công khai lúc "+db.date(s.publicSaleAt)+(hours>0?". Hạng "+t.name+" được đặt sớm từ "+db.date(early)+".":". Hội viên hạng cao được đặt sớm hơn."));}
    }
    /** Admin: who may book a showtime and when public sales start. Existing tickets are untouched. */
    public void access(Long actor,Long showId,String minTier,Instant publicSaleAt){
        Showtime s=db.lock(Showtime.class,showId);String need=minTier==null||minTier.isBlank()?null:tier(minTier).code;
        if(publicSaleAt!=null&&!publicSaleAt.isBefore(s.startsAt))throw new IllegalArgumentException("Giờ mở bán công khai phải trước giờ chiếu.");
        s.minTier=need;s.publicSaleAt=publicSaleAt;db.audit(actor,"SHOW_ACCESS","#"+s.id+" · hạng "+(need==null?"mọi khách":need)+" · mở bán "+(publicSaleAt==null?"ngay":db.date(publicSaleAt)));
    }

    /** Saves all tier rules at once. Codes are fixed; the lowest tier starts at 0 and minimums strictly increase with rank. */
    public void configure(Long actorId,Map<String,String> values){
        List<Tier> tiers=new ArrayList<>();for(Tier snapshot:tiers())tiers.add(db.lock(Tier.class,snapshot.id));
        long previous=-1;StringBuilder log=new StringBuilder();
        for(Tier t:tiers){
            String name=values.getOrDefault("name_"+t.code,"").trim();if(name.isEmpty()||name.length()>100)throw new IllegalArgumentException("Tên hạng "+t.code+" từ 1 đến 100 ký tự.");
            long min=number(values.get("minSpend_"+t.code),"Mức chi tiêu hạng "+name,0,10_000_000_000L);
            int ticket=(int)number(values.get("ticketPct_"+t.code),"% giảm vé hạng "+name,0,50),combo=(int)number(values.get("comboPct_"+t.code),"% giảm combo hạng "+name,0,50);
            String benefits=values.getOrDefault("benefits_"+t.code,"").replace("\r","").trim();if(benefits.length()>2000)throw new IllegalArgumentException("Quyền lợi hạng "+name+" tối đa 2.000 ký tự.");
            if(previous<0&&min!=0)throw new IllegalArgumentException("Hạng thấp nhất ("+name+") phải bắt đầu từ 0 đ.");
            if(previous>=0&&min<=previous)throw new IllegalArgumentException("Mức chi tiêu hạng "+name+" phải lớn hơn hạng liền trước.");
            Long birthday=template(values.get("birthdayVoucherId_"+t.code),name),yearly=template(values.get("yearlyVoucherId_"+t.code),name),next=template(values.get("nextVoucherId_"+t.code),name);
            int birthdayCount=(int)number(values.getOrDefault("birthdayVouchers_"+t.code,"0"),"Số voucher sinh nhật hạng "+name,0,10),yearlyCount=(int)number(values.getOrDefault("yearlyVouchers_"+t.code,"0"),"Số voucher mỗi năm hạng "+name,0,10),early=(int)number(values.getOrDefault("earlyAccessHours_"+t.code,"0"),"Số giờ đặt sớm hạng "+name,0,168);
            if(birthdayCount>0&&birthday==null||yearlyCount>0&&yearly==null)throw new IllegalArgumentException("Hạng "+name+": chọn voucher khi số lượng lớn hơn 0.");
            Long gift=null;String giftRaw=values.getOrDefault("birthdayGiftId_"+t.code,"");if(giftRaw.matches("[1-9][0-9]{0,17}")){Product p=db.get(Product.class,Long.valueOf(giftRaw));if(!"MERCH".equals(p.kind))throw new IllegalArgumentException("Quà sinh nhật hạng "+name+" phải là sản phẩm lưu niệm.");gift=p.id;}
            previous=min;t.name=name;t.minSpend=min;t.ticketPct=ticket;t.comboPct=combo;t.benefits=benefits;
            t.birthdayVoucherId=birthday;t.birthdayVouchers=birthdayCount;t.yearlyVoucherId=yearly;t.yearlyVouchers=yearlyCount;t.nextVoucherId=next;t.birthdayGiftId=gift;t.earlyAccessHours=early;log.append(t.code).append('=').append(min).append('/').append(ticket).append('/').append(combo).append(' ');
        }
        db.audit(actorId,"TIER_CONFIG",log.toString().trim());
    }
    private Long template(String raw,String tierName){if(raw==null||!raw.matches("[1-9][0-9]{0,17}"))return null;Voucher v=db.get(Voucher.class,Long.valueOf(raw));if(!"PERSONAL".equals(v.audience))throw new IllegalArgumentException("Hạng "+tierName+": chỉ chọn voucher loại phát vào ví (PERSONAL).");return v.id;}
    private static long number(String raw,String field,long min,long max){String v=raw==null?"":raw.replaceAll("[\\s.,₫đ%]","");if(!v.matches("\\d{1,11}"))throw new IllegalArgumentException(field+" phải là số nguyên.");long n=Long.parseLong(v);if(n<min||n>max)throw new IllegalArgumentException(field+" phải từ "+min+" đến "+max+".");return n;}
}
