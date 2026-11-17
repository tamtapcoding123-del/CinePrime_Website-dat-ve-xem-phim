package vn.cineprime.admin;

import java.nio.charset.StandardCharsets;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.cineprime.domain.Store;
import static vn.cineprime.domain.Domain.*;

/** Cinema-wide prices viewed from a room. Layout versions never version money. */
@Service @Transactional(readOnly=true)
public class RoomPrices {
    private final Store db;private final AdminService admin;private final vn.cineprime.booking.BookingService booking;private final java.time.Clock clock;
    public RoomPrices(Store db,AdminService admin,vn.cineprime.booking.BookingService booking,java.time.Clock clock){this.db=db;this.admin=admin;this.booking=booking;this.clock=clock;}
    public static String version(Price p){try{return HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest((p.id+"|"+p.cinemaId+"|"+p.seatType+"|"+p.dayType+"|"+p.fromMinute+"|"+p.toMinute+"|"+p.amount).getBytes(StandardCharsets.UTF_8)));}catch(java.security.NoSuchAlgorithmException e){throw new IllegalStateException(e);}}
    private Map<String,Object> row(Price p){return Map.of("id",p.id,"seatType",p.seatType,"dayType",p.dayType,"fromMinute",AdminPresentation.time(p.fromMinute),"toMinute",AdminPresentation.time(p.toMinute),"amount",Long.toString(p.amount),"amountText",db.cash(p.amount),"version",version(p));}
    public Map<String,Object> page(Long roomId,String type,String day,int requested){
        Room room=db.get(Room.class,roomId);String where=" where p.cinemaId=:cinema";Map<String,Object> args=new HashMap<>(Map.of("cinema",room.cinemaId));
        if(!type.isBlank()){where+=" and p.seatType=:type";args.put("type",type);}if(!day.isBlank()){where+=" and p.dayType=:day";args.put("day",day);}
        String from=" from "+db.entityName(Price.class)+" p"+where;var count=db.entityManager().createQuery("select count(p)"+from,Long.class);args.forEach(count::setParameter);long total=count.getSingleResult();int page=AdminPage.number(requested,total);
        var query=db.entityManager().createQuery("select p"+from+" order by p.dayType,p.seatType,p.fromMinute,p.id",Price.class);args.forEach(query::setParameter);
        var data=new LinkedHashMap<>(new AdminPage<>(query.setFirstResult(page*5).setMaxResults(5).getResultList().stream().map(this::row).toList(),total,page,page>0,(page+1L)*5<total).model());data.put("cinemaId",room.cinemaId);return data;
    }
    /** Every price of the room's cinema at once (the price grid shows them all; no paging). */
    public Map<String,Object> all(Long roomId){Room room=db.get(Room.class,roomId);var rows=db.list(Price.class,"where e.cinemaId=?1 order by e.seatType,e.dayType,e.fromMinute,e.id",room.cinemaId).stream().map(this::row).toList();
        var inRoom=db.query("select distinct s.type from "+db.entityName(Seat.class)+" s where s.roomId=?1 and s.active=true",String.class,roomId);return Map.of("rows",rows,"roomTypes",inRoom,"cinemaId",room.cinemaId,"cinemaName",db.get(Cinema.class,room.cinemaId).name);}
    // Grid edits (16/11/2026). Each one changes several windows in one transaction, so the day never shows an overlap or a hole half-way.
    private Price locked(Room room,Long id,String version){Price p=db.lock(Price.class,id);if(!p.cinemaId.equals(room.cinemaId))throw new IllegalArgumentException("Giá không thuộc rạp của phòng này.");if(!version(p).equals(version))throw new org.springframework.dao.OptimisticLockingFailureException("Giá đã thay đổi. Đọc lại trước khi lưu.");return p;}
    private Price put(Long actor,Long cinema,Long id,String type,String day,int from,int to,long amount){if(amount<=0||from<0||to>1440||from>=to)throw new IllegalArgumentException("Giá hoặc khung giờ không hợp lệ.");Price p=id==null?new Price():db.lock(Price.class,id);p.cinemaId=cinema;p.seatType=type;p.dayType=day;p.fromMinute=from;p.toMinute=to;p.amount=amount;db.save(p);db.audit(actor,"SAVE_prices",String.valueOf(p.id));return p;}
    private void drop(Long actor,Price p){db.delete(p);db.audit(actor,"DISABLE_prices",p.id.toString());}
    /** After a multi-window edit: no overlaps, and every open future showtime of the cinema still finds a price for each seat. */
    private void settle(Long cinema){db.entityManager().flush();Map<String,List<Price>> groups=new HashMap<>();for(Price p:db.list(Price.class,"where e.cinemaId=?1 order by e.fromMinute",cinema))groups.computeIfAbsent(p.seatType+p.dayType,k->new ArrayList<>()).add(p);for(var g:groups.values())for(int i=1;i<g.size();i++)if(g.get(i).fromMinute<g.get(i-1).toMinute)throw new IllegalArgumentException("Trùng khung giá.");
        for(Showtime s:db.list(Showtime.class,"where e.status='OPEN' and e.startsAt>?1",clock.instant()))if(db.get(Room.class,s.roomId).cinemaId.equals(cinema))for(Seat seat:db.list(Seat.class,"where e.roomId=?1 and e.active=true",s.roomId))booking.price(s,seat);}
    private Room room(Long roomId){Room room=db.lockRead(Room.class,roomId);db.lock(Cinema.class,room.cinemaId);return room;}
    private int minute(String v){if(v==null||!v.matches("([01][0-9]|2[0-3]):[0-5][0-9]"))throw new IllegalArgumentException("Giờ phải có dạng HH:mm.");return Integer.parseInt(v.substring(0,2))*60+Integer.parseInt(v.substring(3));}
    /** Split one window in two at a time of day; both halves keep the amount. */
    @Transactional public Map<String,Object> split(Long actor,Long roomId,Long id,Map<String,String> in){Room room=room(roomId);Price p=locked(room,id,in.get("priceVersion"));int at=minute(in.get("at"));if(at<=p.fromMinute||at>=p.toMinute)throw new IllegalArgumentException("Giờ chia phải nằm trong khung "+AdminPresentation.time(p.fromMinute)+"–"+AdminPresentation.time(p.toMinute)+".");int end=p.toMinute;
        put(actor,room.cinemaId,p.id,p.seatType,p.dayType,p.fromMinute,at,p.amount);put(actor,room.cinemaId,null,p.seatType,p.dayType,at,end,p.amount);settle(room.cinemaId);return Map.of("message","Đã chia khung giờ lúc "+AdminPresentation.time(at)+".");}
    /** Move the boundary between a window and the one right after it. */
    @Transactional public Map<String,Object> boundary(Long actor,Long roomId,Long id,Map<String,String> in){Room room=room(roomId);Price p=locked(room,id,in.get("priceVersion"));int at=minute(in.get("at"));Price next=db.first(Price.class,"where e.cinemaId=?1 and e.seatType=?2 and e.dayType=?3 and e.fromMinute=?4",p.cinemaId,p.seatType,p.dayType,p.toMinute).orElseThrow(()->new IllegalArgumentException("Không có khung giờ liền sau để dời mốc."));db.lock(Price.class,next.id);
        if(at<=p.fromMinute||at>=next.toMinute)throw new IllegalArgumentException("Mốc giờ phải nằm giữa "+AdminPresentation.time(p.fromMinute)+" và "+AdminPresentation.time(next.toMinute)+".");int pFrom=p.fromMinute,nTo=next.toMinute;
        put(actor,room.cinemaId,p.id,p.seatType,p.dayType,pFrom,at,p.amount);put(actor,room.cinemaId,next.id,next.seatType,next.dayType,at,nTo,next.amount);settle(room.cinemaId);
        return Map.of("message","Đã dời mốc giờ sang "+AdminPresentation.time(at)+".");}
    /** Remove a window; the window before it (or else after it) grows to cover its hours, so the day stays covered. */
    @Transactional public Map<String,Object> remove(Long actor,Long roomId,Long id,Map<String,String> in){Room room=room(roomId);Price p=locked(room,id,in.get("priceVersion"));
        Optional<Price> before=db.first(Price.class,"where e.cinemaId=?1 and e.seatType=?2 and e.dayType=?3 and e.toMinute=?4",p.cinemaId,p.seatType,p.dayType,p.fromMinute),after=db.first(Price.class,"where e.cinemaId=?1 and e.seatType=?2 and e.dayType=?3 and e.fromMinute=?4",p.cinemaId,p.seatType,p.dayType,p.toMinute);
        if(before.isEmpty()&&after.isEmpty())throw new IllegalArgumentException("Đây là khung giá duy nhất của ngày này. Sửa số tiền thay vì xóa.");int from=p.fromMinute,to=p.toMinute;drop(actor,p);db.entityManager().flush();
        if(before.isPresent()){Price b=db.lock(Price.class,before.get().id);put(actor,room.cinemaId,b.id,b.seatType,b.dayType,b.fromMinute,to,b.amount);}else{Price a=db.lock(Price.class,after.get().id);put(actor,room.cinemaId,a.id,a.seatType,a.dayType,from,a.toMinute,a.amount);}settle(room.cinemaId);
        return Map.of("message","Đã gộp khung giờ.");}
    /** Replace every Thứ 7, CN window with a copy of the Thứ 2–6 windows (all seat types of the cinema). */
    @Transactional public Map<String,Object> copyWeekdays(Long actor,Long roomId){Room room=room(roomId);var weekday=db.list(Price.class,"where e.cinemaId=?1 and e.dayType='WEEKDAY' order by e.seatType,e.fromMinute",room.cinemaId);if(weekday.isEmpty())throw new IllegalArgumentException("Chưa có giá Thứ 2–6 để chép.");
        for(Price old:db.list(Price.class,"where e.cinemaId=?1 and e.dayType='WEEKEND'",room.cinemaId))drop(actor,old);db.entityManager().flush();
        for(Price w:weekday)put(actor,room.cinemaId,null,w.seatType,"WEEKEND",w.fromMinute,w.toMinute,w.amount);settle(room.cinemaId);return Map.of("message","Đã chép "+weekday.size()+" khung giá Thứ 2–6 sang Thứ 7, CN.");}
    @Transactional public Map<String,Object> save(Long actor,Long roomId,Long priceId,Map<String,String> input){
        Room room=db.lockRead(Room.class,roomId);db.lock(Cinema.class,room.cinemaId);
        if(priceId!=null){Price price=db.lock(Price.class,priceId);if(!price.cinemaId.equals(room.cinemaId))throw new IllegalArgumentException("Giá không thuộc rạp của phòng này.");if(!version(price).equals(input.get("priceVersion")))throw new org.springframework.dao.OptimisticLockingFailureException("Giá đã thay đổi. Đọc lại trước khi lưu.");}
        Map<String,String> fields=new HashMap<>();for(String key:List.of("seatType","dayType","fromMinute","toMinute","amount","priceVersion"))if(input.containsKey(key))fields.put(key,input.get(key));fields.put("cinemaId",room.cinemaId.toString());
        Price saved=(Price)admin.save(actor,"prices",priceId,AdminPresentation.normalize(fields));return Map.of("message","Đã lưu giá của rạp.","row",row(saved));
    }
}
