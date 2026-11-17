package vn.cineprime.member;

import java.time.Instant;
import java.util.*;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.cineprime.admin.AdminPage;
import vn.cineprime.domain.Store;
import static vn.cineprime.domain.Domain.*;

/** Read models for membership: Admin member list/detail, tier configuration, and the customer's own standing. Never writes. */
@Service @Transactional(readOnly=true)
public class MemberReads {
    private final Store db;private final TierService tiers;
    public MemberReads(Store db,TierService tiers){this.db=db;this.tiers=tiers;}
    public static String reason(String code){return switch(code){case "UPGRADE"->"Lên hạng tự động";case "ANNUAL_REVIEW"->"Xét hạng định kỳ";case "ADMIN"->"Admin điều chỉnh";default->code;};}
    public Map<String,Object> tierView(Tier t){
        Map<String,Object> v=new LinkedHashMap<>();v.put("code",t.code);v.put("name",t.name);v.put("color",t.color);v.put("rank",t.rankOrder);v.put("ticketPct",t.ticketPct);v.put("comboPct",t.comboPct);v.put("minSpend",t.minSpend);v.put("minSpendText",db.cash(t.minSpend));v.put("benefitsText",t.benefits);
        v.put("benefits",Arrays.stream(t.benefits.split("\n")).map(String::trim).filter(s->!s.isEmpty()).toList());
        v.put("birthdayVoucherId",Objects.toString(t.birthdayVoucherId,""));v.put("birthdayVouchers",t.birthdayVouchers);v.put("yearlyVoucherId",Objects.toString(t.yearlyVoucherId,""));v.put("yearlyVouchers",t.yearlyVouchers);
        v.put("nextVoucherId",Objects.toString(t.nextVoucherId,""));v.put("birthdayGiftId",Objects.toString(t.birthdayGiftId,""));v.put("earlyAccessHours",t.earlyAccessHours);
        // Automatic benefits in words, shown to customers next to the free-text list.
        List<String> auto=new ArrayList<>();
        if(t.birthdayVoucherId!=null&&t.birthdayVouchers>0)auto.add(t.birthdayVouchers+" × "+db.get(Voucher.class,t.birthdayVoucherId).description+" (tháng sinh nhật)");
        if(t.yearlyVoucherId!=null&&t.yearlyVouchers>0)auto.add(t.yearlyVouchers+" × "+db.get(Voucher.class,t.yearlyVoucherId).description+" (mỗi năm)");
        if(t.nextVoucherId!=null)auto.add("Sau mỗi đơn: "+db.get(Voucher.class,t.nextVoucherId).description);
        if(t.birthdayGiftId!=null)auto.add("Quà sinh nhật: "+db.get(Product.class,t.birthdayGiftId).name+" (nhận tại quầy)");
        if(t.earlyAccessHours>0)auto.add("Đặt vé sớm "+t.earlyAccessHours+" giờ trước khi mở bán công khai");
        v.put("automatic",auto);return v;
    }
    public static String source(String code){return switch(code){case "BIRTHDAY"->"Sinh nhật";case "YEARLY"->"Quà năm";case "NEXT"->"Lần mua sau";case "REDEEM"->"Đổi điểm";case "CAMPAIGN"->"Chương trình";case "ADMIN"->"CinePrime tặng";default->code;};}
    /** One customer's wallet, newest first: usable copies, then used/expired/revoked ones for the record. */
    public List<Map<String,Object>> wallet(Long userId){
        Instant now=tiers.now();
        return db.list(UserVoucher.class,"where e.userId=?1 order by e.issuedAt desc,e.id desc",userId).stream().map(uv->{Voucher v=db.get(Voucher.class,uv.voucherId);Map<String,Object> m=new LinkedHashMap<>();
            boolean expired=!now.isBefore(uv.expiresAt);String state="USED".equals(uv.status)?"Đã dùng":"REVOKED".equals(uv.status)?"Đã thu hồi":expired?"Hết hạn":"Dùng được";
            m.put("id",uv.id);m.put("code",uv.code);m.put("description",v.description);m.put("source",source(uv.source));m.put("expires",db.date(uv.expiresAt));m.put("state",state);m.put("usable",state.equals("Dùng được"));m.put("issued",db.date(uv.issuedAt));m.put("revocable","ISSUED".equals(uv.status));return m;}).toList();
    }
    /** Codes the customer can type on this order right now (for the voucher field's suggestions). */
    public List<Map<String,Object>> usableCodes(Long userId){return wallet(userId).stream().filter(m->Boolean.TRUE.equals(m.get("usable"))).toList();}
    /** Templates the customer may buy with points; affordable/allowed flags drive the buttons, the server re-checks. */
    public List<Map<String,Object>> catalog(User u){
        Instant now=tiers.now();long balance=balance(u.id);Tier mine=tiers.effective(u);
        return db.list(Voucher.class,"where e.audience='PERSONAL' and e.pointsCost>0 and e.active=true and e.startsAt<=?1 and e.endsAt>?1 order by e.pointsCost,e.id",now).stream().map(v->{Map<String,Object> m=new LinkedHashMap<>();
            boolean allowed=v.minTier==null||mine.rankOrder>=tiers.tier(v.minTier).rankOrder;m.put("id",v.id);m.put("description",v.description);m.put("cost",v.pointsCost);m.put("validDays",v.validDays);m.put("allowed",allowed);m.put("affordable",balance>=v.pointsCost);
            m.put("tierNote",v.minTier==null?"":"Hạng "+tiers.tier(v.minTier).name+" trở lên");return m;}).toList();
    }
    public List<Map<String,Object>> gifts(Long userId){
        Instant now=tiers.now();
        return db.list(GiftGrant.class,"where e.userId=?1 order by e.issuedAt desc,e.id desc",userId).stream().map(g->{Map<String,Object> m=new LinkedHashMap<>();Product p=db.get(Product.class,g.productId);
            m.put("id",g.id);m.put("product",p.name);m.put("note",Objects.toString(g.note,""));m.put("expires",db.date(g.expiresAt));m.put("handed",db.date(g.handedAt));
            m.put("state","HANDED_OVER".equals(g.status)?"Đã nhận":"REVOKED".equals(g.status)?"Đã thu hồi":now.isBefore(g.expiresAt)?"Chờ nhận tại quầy":"Hết hạn nhận");m.put("waiting","ISSUED".equals(g.status)&&now.isBefore(g.expiresAt));
            m.put("cinema",g.cinemaId==null?"":db.get(Cinema.class,g.cinemaId).name);return m;}).toList();
    }
    public List<Map<String,Object>> personalTemplates(){return db.list(Voucher.class,"where e.audience='PERSONAL' order by e.code").stream().map(v->Map.<String,Object>of("id",v.id,"label",v.code+" · "+v.description+(v.active?"":" (đang tắt)"),"code",v.code+(v.active?"":" (tắt)"),"description",v.description)).toList();}
    public List<Map<String,Object>> merch(){return db.list(Product.class,"where e.kind='MERCH' order by e.name,e.id").stream().map(p->Map.<String,Object>of("id",p.id,"label",p.name+" · tồn "+(p.stock==null?"∞":p.stock)+(p.active?"":" · không bán lẻ"),"name",p.name,"stock",p.stock==null?"∞":p.stock.toString(),"active",p.active,"low",p.stock!=null&&p.stock<=5)).toList();}
    /** Benefit dashboard: wallet copies per template and gifts waiting/handed, for the Ưu đãi thành viên tab. */
    public Map<String,Object> offers(String giftState,int requested){
        Map<String,Object> model=new LinkedHashMap<>();Instant now=tiers.now();
        Map<Long,long[]> counts=new HashMap<>();db.query("select e.voucherId,e.status,count(e) from "+db.entityName(UserVoucher.class)+" e group by e.voucherId,e.status",Object[].class).forEach(r->{long[] c=counts.computeIfAbsent((Long)r[0],k->new long[3]);long n=((Number)r[2]).longValue();switch((String)r[1]){case "USED"->c[1]+=n;case "REVOKED"->c[2]+=n;default->c[0]+=n;}});
        model.put("templates",db.list(Voucher.class,"where e.audience='PERSONAL' order by e.code").stream().map(v->{long[] c=counts.getOrDefault(v.id,new long[3]);Map<String,Object> m=new LinkedHashMap<>();m.put("id",v.id);m.put("code",v.code);m.put("description",v.description);m.put("scope",vn.cineprime.admin.AdminPresentation.text(v.scope));m.put("points",v.pointsCost==0?"—":v.pointsCost+" điểm");m.put("open",c[0]);m.put("used",c[1]);m.put("revoked",c[2]);m.put("active",v.active);return m;}).toList());
        String from=" from "+db.entityName(GiftGrant.class)+" g, "+db.entityName(User.class)+" u, "+db.entityName(Product.class)+" p where g.userId=u.id and g.productId=p.id";Map<String,Object> args=new HashMap<>();
        if(giftState.equals("WAITING")){from+=" and g.status='ISSUED' and g.expiresAt>:now";args.put("now",now);}else if(Set.of("HANDED_OVER","REVOKED").contains(giftState)){from+=" and g.status=:state";args.put("state",giftState);}
        long total=query("select count(g)"+from,Long.class,args).getSingleResult();int page=AdminPage.number(requested,total);
        var rows=query("select g,u.name,u.email,p.name"+from+" order by g.issuedAt desc,g.id desc",Object[].class,args).setFirstResult(page*AdminPage.SIZE).setMaxResults(AdminPage.SIZE).getResultList().stream().map(r->{GiftGrant g=(GiftGrant)r[0];Map<String,Object> m=new LinkedHashMap<>();m.put("id",g.id);m.put("userId",g.userId);m.put("customer",r[1]);m.put("email",r[2]);m.put("product",r[3]);m.put("reason",switch(g.reason){case "BIRTHDAY"->"Sinh nhật";case "CAMPAIGN"->"Chương trình";default->"Admin tặng";});
            m.put("state","HANDED_OVER".equals(g.status)?"Đã trao · "+db.date(g.handedAt):"REVOKED".equals(g.status)?"Đã thu hồi":now.isBefore(g.expiresAt)?"Chờ nhận · hạn "+db.date(g.expiresAt):"Hết hạn nhận");m.put("revocable","ISSUED".equals(g.status));return m;}).toList();
        model.putAll(new AdminPage<>(rows,total,page,page>0,(page+1L)*AdminPage.SIZE<total).model());
        model.put("merch",merch());model.put("personalTemplates",personalTemplates());model.put("tierOptions",tierViews());return model;
    }
    /** Upcoming showtimes with their member access, for the Suất hội viên tab. */
    public Map<String,Object> shows(java.time.LocalDate day,Long cinema,int requested){
        Instant from=day.atStartOfDay(TierService.ZONE).toInstant(),until=day.plusDays(1).atStartOfDay(TierService.ZONE).toInstant();
        String q=" from "+db.entityName(Showtime.class)+" s, "+db.entityName(Movie.class)+" m, "+db.entityName(Room.class)+" r, "+db.entityName(Cinema.class)+" c where s.movieId=m.id and s.roomId=r.id and r.cinemaId=c.id and s.status<>'CANCELLED' and s.startsAt>=:from and s.startsAt<:until";
        Map<String,Object> args=new HashMap<>(Map.of("from",from,"until",until));if(cinema!=null){q+=" and c.id=:cinema";args.put("cinema",cinema);}
        long total=query("select count(s)"+q,Long.class,args).getSingleResult();int page=AdminPage.number(requested,total);
        var rows=query("select s,m.title,r.name,c.name"+q+" order by s.startsAt,s.id",Object[].class,args).setFirstResult(page*AdminPage.SIZE).setMaxResults(AdminPage.SIZE).getResultList().stream().map(r->{Showtime s=(Showtime)r[0];Map<String,Object> m=new LinkedHashMap<>();
            m.put("id",s.id);m.put("movie",r[1]);m.put("place",r[3]+" · "+r[2]);m.put("when",db.date(s.startsAt));m.put("status",vn.cineprime.admin.AdminPresentation.text(s.status));m.put("minTier",Objects.toString(s.minTier,""));m.put("tierName",s.minTier==null?"":tiers.tier(s.minTier).name);m.put("publicSaleAt",s.publicSaleAt==null?"":db.local(s.publicSaleAt));m.put("saleDate",s.publicSaleAt==null?"":db.local(s.publicSaleAt).substring(0,10));m.put("saleTime",s.publicSaleAt==null?"":db.local(s.publicSaleAt).substring(11,16));m.put("saleText",db.date(s.publicSaleAt));
            m.put("sold",db.count(Ticket.class,"where e.showtimeId=?1 and e.status in ('VALID','USED')",s.id));return m;}).toList();
        Map<String,Object> model=new HashMap<>(new AdminPage<>(rows,total,page,page>0,(page+1L)*AdminPage.SIZE<total).model());model.put("tierOptions",tierViews());model.put("memberOnly",query("select count(s)"+q+" and (s.minTier is not null or s.publicSaleAt>:from)",Long.class,args).getSingleResult());return model;
    }
    public List<Map<String,Object>> tierViews(){return tiers.tiers().stream().map(this::tierView).toList();}
    /** One strip shared by the four Thành viên screens: the tier ladder with customer counts, plus what is waiting elsewhere. */
    public Map<String,Object> overview(){
        Instant now=tiers.now();Map<String,Object> m=new LinkedHashMap<>();List<Map<String,Object>> ladder=counts();
        m.put("ladder",ladder);m.put("customers",ladder.stream().mapToLong(t->(Long)t.get("count")).sum());
        m.put("waitingGifts",db.count(GiftGrant.class,"where e.status='ISSUED' and e.expiresAt>?1",now));
        m.put("openVouchers",db.count(UserVoucher.class,"where e.status='ISSUED' and e.expiresAt>?1",now));
        m.put("memberShows",db.count(Showtime.class,"where e.status<>'CANCELLED' and e.startsAt>?1 and (e.minTier is not null or e.publicSaleAt>?1)",now));
        m.put("merchLow",db.count(Product.class,"where e.kind='MERCH' and e.stock is not null and e.stock<=5"));return m;
    }
    public long balance(Long userId){return db.query("select coalesce(sum(p.points),0) from "+db.entityName(Point.class)+" p where p.userId=?1",Long.class,userId).getFirst();}
    /** Tier, qualifying spend and the way to the next tier, as the next refresh would see them. */
    public Map<String,Object> standing(User u){
        int y=tiers.year();long year=tiers.spend(u.id,y),last=tiers.spend(u.id,y-1),qualifying=Math.max(year,last);
        Tier current=tiers.effective(u);Tier next=tiers.tiers().stream().filter(t->t.rankOrder>current.rankOrder).findFirst().orElse(null);
        Map<String,Object> v=new LinkedHashMap<>();v.put("tier",tierView(current));v.put("pending",!current.code.equals(u.tierCode));v.put("storedName",tiers.tier(u.tierCode).name);
        v.put("year",y);v.put("lastYear",y-1);v.put("spendYear",db.cash(year));v.put("spendLast",db.cash(last));v.put("qualifying",db.cash(qualifying));v.put("next",next==null?null:tierView(next));
        v.put("remaining",next==null?"":db.cash(Math.max(0,next.minSpend-qualifying)));
        long span=next==null?1:next.minSpend-current.minSpend;v.put("progress",next==null?100:(int)Math.max(0,Math.min(100,(qualifying-current.minSpend)*100/Math.max(1,span))));
        // Spend of year Y keeps the tier through 31/12 of Y+1; an Admin-assigned tier without that spend lasts until this year's end.
        v.put("keepUntil",current.rankOrder==1?"":"31/12/"+(year>=current.minSpend?y+1:y));v.put("balance",balance(u.id));v.put("since",db.date(u.tierSince));return v;
    }
    private <T> TypedQuery<T> query(String hql,Class<T> type,Map<String,Object> args){var q=db.entityManager().createQuery(hql,type);args.forEach(q::setParameter);return q;}
    public Map<String,Object> members(String q,String tier,int requested){
        String from=" from "+db.entityName(User.class)+" u where u.role='CUSTOMER'";Map<String,Object> args=new HashMap<>();
        if(TierService.CODES.contains(tier)){from+=" and u.tierCode=:tier";args.put("tier",tier);}
        if(!q.isBlank()){from+=" and (lower(u.name) like :q escape '!' or lower(u.email) like :q escape '!' or coalesce(u.phone,'') like :q escape '!')";args.put("q","%"+q.trim().toLowerCase(Locale.ROOT).replace("!","!!").replace("%","!%").replace("_","!_")+"%");}
        long total=query("select count(u)"+from,Long.class,args).getSingleResult();int page=AdminPage.number(requested,total);
        List<User> users=query("select u"+from+" order by u.id desc",User.class,args).setFirstResult(page*AdminPage.SIZE).setMaxResults(AdminPage.SIZE).getResultList();
        int y=tiers.year();
        List<Map<String,Object>> rows=users.stream().map(u->{Tier t=tiers.effective(u);Map<String,Object> v=new LinkedHashMap<>();v.put("id",u.id);v.put("name",u.name);v.put("email",u.email);v.put("phone",Objects.toString(u.phone,""));v.put("enabled",u.enabled);
            v.put("tier",tierView(t));v.put("pending",!t.code.equals(u.tierCode));v.put("storedName",tiers.tier(u.tierCode).name);v.put("spendYear",db.cash(tiers.spend(u.id,y)));v.put("spendLast",db.cash(tiers.spend(u.id,y-1)));v.put("balance",balance(u.id));
            v.put("paidOrders",db.count(Order.class,"where e.userId=?1 and e.status='PAID'",u.id));v.put("since",u.tierSince==null?"—":db.date(u.tierSince));
            v.put("wallet",db.count(UserVoucher.class,"where e.userId=?1 and e.status='ISSUED' and e.expiresAt>?2",u.id,tiers.now()));v.put("gifts",db.count(GiftGrant.class,"where e.userId=?1 and e.status='ISSUED' and e.expiresAt>?2",u.id,tiers.now()));
            Tier next=tiers.tiers().stream().filter(x->x.rankOrder>t.rankOrder).findFirst().orElse(null);long spent=Math.max(tiers.spend(u.id,y),tiers.spend(u.id,y-1));
            v.put("nextName",next==null?"":next.name);v.put("progress",next==null?100:(int)Math.max(0,Math.min(100,(spent-t.minSpend)*100/Math.max(1,next.minSpend-t.minSpend))));v.put("remaining",next==null?"":db.cash(Math.max(0,next.minSpend-spent)));return v;}).toList();
        Map<String,Object> model=new HashMap<>(new AdminPage<>(rows,total,page,page>0,(page+1L)*AdminPage.SIZE<total).model());
        model.put("tierCounts",counts());model.put("year",y);return model;
    }
    /** Customers per cached tier, in rank order (includes tiers with nobody). */
    public List<Map<String,Object>> counts(){
        Map<String,Long> n=new HashMap<>();db.query("select u.tierCode,count(u) from "+db.entityName(User.class)+" u where u.role='CUSTOMER' group by u.tierCode",Object[].class).forEach(r->n.put((String)r[0],((Number)r[1]).longValue()));
        return tiers.tiers().stream().map(t->{Map<String,Object> v=tierView(t);v.put("count",n.getOrDefault(t.code,0L));return v;}).toList();
    }
    public Map<String,Object> member(Long id){
        User u=db.get(User.class,id);if(!"CUSTOMER".equals(u.role))throw new IllegalArgumentException("Tài khoản không phải khách hàng.");
        Map<String,Object> model=new LinkedHashMap<>();Map<String,Object> who=new LinkedHashMap<>();who.put("id",u.id);who.put("name",u.name);who.put("email",u.email);who.put("phone",Objects.toString(u.phone,"—"));who.put("enabled",u.enabled);who.put("tierCode",u.tierCode);
        model.put("member",who);model.put("standing",standing(u));
        Map<String,String> names=new HashMap<>();tiers.tiers().forEach(t->names.put(t.code,t.name));
        model.put("history",db.list(TierChange.class,"where e.userId=?1 order by e.createdAt desc,e.id desc",id).stream().limit(20).map(c->{Map<String,Object> v=new LinkedHashMap<>();v.put("when",db.date(c.createdAt));v.put("from",names.getOrDefault(c.fromCode,c.fromCode));v.put("to",names.getOrDefault(c.toCode,c.toCode));v.put("reason",reason(c.reason));v.put("spend",db.cash(c.spend));v.put("note",Objects.toString(c.note,""));v.put("actor",c.actorId==null?"Hệ thống":db.get(User.class,c.actorId).name);return v;}).toList());
        model.put("orders",db.query("select o from "+db.entityName(Order.class)+" o where o.userId=?1 order by o.createdAt desc,o.id desc",Order.class,id).stream().limit(10).map(o->{Map<String,Object> v=new LinkedHashMap<>();v.put("id",o.id);v.put("code",o.code);v.put("movie",o.movieTitle);v.put("when",db.date(o.paidAt!=null?o.paidAt:o.createdAt));v.put("status",vn.cineprime.admin.AdminPresentation.text(o.status));v.put("channel","COUNTER".equals(o.channel)?"Quầy":"Online");v.put("total",db.cash(o.total));v.put("tierDiscount",o.tierDiscount==0?"—":"−"+db.cash(o.tierDiscount));return v;}).toList());
        model.put("points",db.query("select p from "+db.entityName(Point.class)+" p where p.userId=?1 order by p.createdAt desc,p.id desc",Point.class,id).stream().limit(10).map(p->Map.<String,Object>of("when",db.date(p.createdAt),"points",(p.points>0?"+":"")+p.points,"kind",switch(p.kind){case "EARN"->"Tích điểm";case "REVERSAL"->"Hoàn điểm (hủy đơn)";case "REDEEM"->"Đổi điểm";default->"Điều chỉnh";},"order",p.orderId!=null?db.get(Order.class,p.orderId).code:Objects.toString(p.note,""))).toList());
        model.put("tiers",tierViews());model.put("wallet",wallet(id));model.put("gifts",gifts(id));model.put("personalTemplates",personalTemplates());model.put("merch",merch());
        who.put("birthDate",u.birthDate==null?"":u.birthDate.toString());who.put("birthText",u.birthDate==null?"Chưa khai báo":String.format("%02d/%02d/%d",u.birthDate.getDayOfMonth(),u.birthDate.getMonthValue(),u.birthDate.getYear()));return model;
    }
}
