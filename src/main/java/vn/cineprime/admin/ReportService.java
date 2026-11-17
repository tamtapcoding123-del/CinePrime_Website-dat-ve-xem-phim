package vn.cineprime.admin;

import jakarta.servlet.http.HttpSession;
import java.math.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import vn.cineprime.domain.Store;
import vn.cineprime.domain.MovieLifecycle;
import static vn.cineprime.domain.Domain.*;

/** Financial reads never invoke booking/payment mutations. Every download uses its rendered snapshot. */
@Service
public class ReportService {
    public static final ZoneId ZONE=ZoneId.of("Asia/Ho_Chi_Minh");
    private static final String CACHE=ReportService.class.getName()+".snapshots";
    private final Store db; private final Clock clock; private final vn.cineprime.staff.CounterService counter;
    public ReportService(Store db,Clock clock,vn.cineprime.staff.CounterService counter){this.db=db;this.clock=clock;this.counter=counter;}
    public record Range(String period,LocalDate from,LocalDate until,Instant end,LocalDate previousFrom,Instant previousEnd,boolean future){}
    public record Snapshot(String id,Instant createdAt,Map<String,Object> model,byte[] csv){
        public Snapshot {model=Collections.unmodifiableMap(new LinkedHashMap<>(model));csv=csv.clone();}
        @Override public byte[] csv(){return csv.clone();}
    }
    public static Range range(String period,LocalDate date,Instant now){
        if(!Set.of("day","week","month","year").contains(period))throw new IllegalArgumentException("Kỳ báo cáo không hợp lệ.");
        LocalDate from=switch(period){case "day"->date;case "week"->date.minusDays(date.getDayOfWeek().getValue()-1);case "year"->date.withDayOfYear(1);default->date.withDayOfMonth(1);};
        LocalDate until=shift(from,period,1),previous=shift(from,period,-1);
        Instant start=from.atStartOfDay(ZONE).toInstant(),finish=until.atStartOfDay(ZONE).toInstant();
        boolean future=!now.isAfter(start);Instant end=now.isBefore(finish)?now:finish;
        LocalDateTime progress=now.atZone(ZONE).toLocalDateTime();
        LocalDateTime previousProgress=switch(period){case "day"->progress.minusDays(1);case "week"->progress.minusWeeks(1);case "year"->progress.minusYears(1);default->progress.minusMonths(1);};
        Instant previousEnd=now.isBefore(finish)?previousProgress.atZone(ZONE).toInstant():start;
        if(previousEnd.isBefore(previous.atStartOfDay(ZONE).toInstant()))previousEnd=previous.atStartOfDay(ZONE).toInstant();
        return new Range(period,from,until,end,previous,previousEnd,future);
    }
    private static LocalDate shift(LocalDate d,String period,int n){return switch(period){case "day"->d.plusDays(n);case "week"->d.plusWeeks(n);case "year"->d.plusYears(n);default->d.plusMonths(n);};}
    private static final String SOLD="(o.status='PAID' or (o.status='CANCELLED' and o.cancelledAt is not null)) and o.paidAt>=?1 and o.paidAt<?2",VOIDED="o.status='CANCELLED' and o.cancelledAt is not null and o.cancelledAt>=?1 and o.cancelledAt<?2";
    private static Object[] args(Instant from,Instant end,Long cinemaId){return cinemaId==null?new Object[]{from,end}:new Object[]{from,end,cinemaId};}
    private List<Object[]> orders(String condition,String order,Instant from,Instant end,Long cinemaId){
        return db.query("select o,s.movieId,r.cinemaId,coalesce(u.email,'') from %s o left join %s u on u.id=o.userId, %s s, %s r where o.showtimeId=s.id and s.roomId=r.id and ".formatted(db.entityName(Order.class),db.entityName(User.class),db.entityName(Showtime.class),db.entityName(Room.class))+condition+(cinemaId==null?"":" and r.cinemaId=?3")+" order by "+order,Object[].class,args(from,end,cinemaId));
    }
    private long seats(String condition,Instant from,Instant end,Long cinemaId){
        return db.query("select count(l) from %s l, %s o, %s s, %s r where l.orderId=o.id and o.showtimeId=s.id and s.roomId=r.id and ".formatted(db.entityName(OrderSeat.class),db.entityName(Order.class),db.entityName(Showtime.class),db.entityName(Room.class))+condition+(cinemaId==null?"":" and r.cinemaId=?3"),Long.class,args(from,end,cinemaId)).getFirst();
    }
    /**
     * A period's books. Every sale counts at paidAt (even if it was cancelled later) and every counter cancellation counts as a refund at cancelledAt.
     * Net revenue = sales − refunds, so a closed period never changes afterwards and always equals money in − money out of the same period.
     */
    private record Book(List<Object[]> sales,List<Object[]> refunds,long soldTickets,long voidTickets){
        static final Book EMPTY=new Book(List.of(),List.of(),0,0);
        long gross(){return total(sales);} long refunded(){return total(refunds);} long revenue(){return gross()-refunded();}
        int orders(){return sales.size()-refunds.size();} long tickets(){return soldTickets-voidTickets;}
    }
    private Book book(Instant from,Instant end,Long cinemaId){return new Book(orders(SOLD,"o.paidAt,o.id",from,end,cinemaId),orders(VOIDED,"o.cancelledAt,o.id",from,end,cinemaId),seats(SOLD,from,end,cinemaId),seats(VOIDED,from,end,cinemaId));}
    private static boolean counter(Object[] row){return "COUNTER".equals(((Order)row[0]).channel);}
    private static long channel(List<Object[]> rows,boolean counter){long sum=0;for(Object[] row:rows)if(counter(row)==counter)sum=Math.addExact(sum,((Order)row[0]).total);return sum;}
    private static int count(List<Object[]> rows,boolean counter){int n=0;for(Object[] row:rows)if(counter(row)==counter)n++;return n;}
    private String signed(long value){return (value>0?"+":"")+db.cash(value);}
    @Transactional(readOnly=true,isolation=Isolation.REPEATABLE_READ)
    public Snapshot create(String period,LocalDate date,Long cinemaId){
        Instant now=clock.instant();if(date==null)date=now.atZone(ZONE).toLocalDate();Range range=range(period,date,now);
        String cinema=cinemaId==null?"Tất cả rạp":db.get(Cinema.class,cinemaId).name;
        Instant start=range.from().atStartOfDay(ZONE).toInstant();
        Book book=range.future()?Book.EMPTY:book(start,range.end(),cinemaId),old=range.future()?Book.EMPTY:book(range.previousFrom().atStartOfDay(ZONE).toInstant(),range.previousEnd(),cinemaId);
        long revenue=book.revenue(),ticketCount=book.tickets();
        String id=UUID.randomUUID().toString();Map<String,Object> model=new LinkedHashMap<>();
        model.put("reportId",id);model.put("period",period);model.put("reportDate",date.toString());model.put("cinemaId",cinemaId==null?"":cinemaId.toString());model.put("cinemaLabel",cinema);model.put("generatedAt",stamp(now));model.put("range",range.from()+" → "+range.until().minusDays(1));
        model.put("cutoff",range.future()?"Chưa bắt đầu":stamp(range.end()));model.put("comparisonRange",range.future()?"Không so sánh kỳ chưa bắt đầu":"từ 00:00 · "+range.previousFrom().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))+" đến trước "+stamp(range.previousEnd()));
        long counterRevenue=channel(book.sales(),true)-channel(book.refunds(),true),onlineRevenue=revenue-counterRevenue;
        int counterOrders=count(book.sales(),true)-count(book.refunds(),true),onlineOrders=book.orders()-counterOrders;
        model.put("counterRevenue",db.cash(counterRevenue));model.put("onlineRevenue",db.cash(onlineRevenue));model.put("counterOrders",counterOrders);model.put("onlineOrders",onlineOrders);
        model.put("grossSales",db.cash(book.gross()));model.put("refundTotal",db.cash(book.refunded()));model.put("refundTotalValue",book.refunded());model.put("soldOrders",book.sales().size());model.put("cancelledOrders",book.refunds().size());model.put("soldTickets",book.soldTickets());model.put("voidTickets",book.voidTickets());
        model.put("hasActivity",!book.sales().isEmpty()||!book.refunds().isEmpty());
        model.put("cashflow",cashflow(range,cinemaId,onlineRevenue,onlineOrders,revenue));
        model.put("revenue",db.cash(revenue));model.put("revenueValue",revenue);model.put("ticketCount",ticketCount);model.put("orderCount",book.orders());
        model.put("revenueChange",change(revenue,old.revenue(),range.future()));model.put("ticketChange",change(ticketCount,old.tickets(),range.future()));model.put("orderChange",change(book.orders(),old.orders(),range.future()));
        model.put("chart",chart(range,now,book));model.put("topMovies",top(book));model.put("occupancy",occupancy(range,cinemaId));
        model.put("admission",admission(range,cinemaId,now));model.put("shifts",shifts(range,cinemaId));
        model.put("customerCount",db.count(User.class,"where e.role='CUSTOMER'"));model.put("nowCount",db.count(Movie.class,"where e.active=true and "+MovieLifecycle.expression("e","?1")+"='NOW_SHOWING'",MovieLifecycle.today(clock)));model.put("comingCount",db.count(Movie.class,"where e.active=true and "+MovieLifecycle.expression("e","?1")+"='COMING_SOON'",MovieLifecycle.today(clock)));
        // CSV lists sales (+) and cancellations (−) in time order; the "Doanh thu VND" column sums exactly to the dashboard's net revenue.
        record Line(Instant at,long id,Object[] row,boolean refund){}
        List<Line> lines=new ArrayList<>();for(Object[] row:book.sales())lines.add(new Line(((Order)row[0]).paidAt,((Order)row[0]).id,row,false));for(Object[] row:book.refunds())lines.add(new Line(((Order)row[0]).cancelledAt,((Order)row[0]).id,row,true));
        lines.sort(Comparator.comparing(Line::at).thenComparingLong(Line::id).thenComparing(Line::refund));
        StringBuilder csv=new StringBuilder("﻿Mã báo cáo,Kỳ,Rạp,Mốc chốt,Loại,Mã đơn,Ghi nhận lúc,Phim,Rạp trên đơn,Phòng,Email,Tạm tính VND,Giảm VND,Doanh thu VND,Kênh\r\n");
        for(Line line:lines){Order o=(Order)line.row()[0];int sign=line.refund()?-1:1;csv.append(csvRow(id,range.from()+" → "+range.until().minusDays(1),cinema,range.end(),line.refund()?"Hủy · hoàn tiền":"Bán",o.code,stamp(line.at()),o.movieTitle,o.cinemaName,o.roomName,line.row()[3],sign*o.subtotal,sign*o.discount,sign*o.total,"COUNTER".equals(o.channel)?"Quầy":"Online"));}
        return new Snapshot(id,now,model,csv.toString().getBytes(StandardCharsets.UTF_8));
    }
    /**
     * Money in and out during the closed part of the period: online (paid orders), counter cash taken, counter cash refunded, and late payments held for reconciliation (not revenue).
     * The ledger re-computes revenue from payment rows only (MoMo/voucher successes + cash taken − cash refunded); it must equal the order-based revenue to the đồng.
     */
    private Map<String,Object> cashflow(Range range,Long cinemaId,long onlineIn,long onlineCount,long revenue){
        long[] cashIn={0,0},refund={0,0},held={0,0},online={0,0};
        if(!range.future()){
            Instant start=range.from().atStartOfDay(ZONE).toInstant(),end=range.end();
            String base=" from %s p, %s o, %s s, %s r where p.orderId=o.id and o.showtimeId=s.id and s.roomId=r.id".formatted(db.entityName(Payment.class),db.entityName(Order.class),db.entityName(Showtime.class),db.entityName(Room.class))+(cinemaId==null?"":" and r.cinemaId=?3");
            Object[] args=args(start,end,cinemaId);
            cashIn=sum("select count(p),coalesce(sum(p.amount),0)"+base+" and p.provider='CASH' and p.status in ('SUCCEEDED','REFUNDED') and p.createdAt>=?1 and p.createdAt<?2",args);
            refund=sum("select count(p),coalesce(sum(p.amount),0)"+base+" and p.provider='CASH' and p.status='REFUNDED' and p.updatedAt>=?1 and p.updatedAt<?2",args);
            held=sum("select count(p),coalesce(sum(p.amount),0)"+base+" and p.reconciliation=true and coalesce(p.updatedAt,p.createdAt)>=?1 and coalesce(p.updatedAt,p.createdAt)<?2",args);
            online=sum("select count(p),coalesce(sum(p.amount),0)"+base+" and p.provider<>'CASH' and p.status='SUCCEEDED' and p.reconciliation=false and p.updatedAt>=?1 and p.updatedAt<?2",args);
        }
        long in=Math.addExact(onlineIn,cashIn[1]),net=in-refund[1],ledger=Math.addExact(online[1],cashIn[1])-refund[1];
        Map<String,Object> flow=new LinkedHashMap<>();
        flow.put("onlineIn",db.cash(onlineIn));flow.put("onlineCount",onlineCount);flow.put("cashIn",db.cash(cashIn[1]));flow.put("cashCount",cashIn[0]);
        flow.put("refund",db.cash(refund[1]));flow.put("refundCount",refund[0]);flow.put("refundValue",refund[1]);flow.put("held",db.cash(held[1]));flow.put("heldCount",held[0]);flow.put("heldValue",held[1]);
        flow.put("in",db.cash(in));flow.put("inValue",in);flow.put("net",db.cash(net));flow.put("netValue",net);flow.put("cashNet",db.cash(cashIn[1]-refund[1]));flow.put("cashNetValue",cashIn[1]-refund[1]);
        flow.put("ledger",db.cash(ledger));flow.put("ledgerValue",ledger);flow.put("onlineLedger",db.cash(online[1]));flow.put("matched",ledger==revenue&&net==revenue);flow.put("gap",signed(ledger-revenue));
        int onlineShare=in==0?0:(int)Math.round(onlineIn*100.0/in);flow.put("onlineShare",onlineShare);flow.put("cashShare",in==0?0:100-onlineShare);
        return flow;
    }
    private long[] sum(String hql,Object[] args){Object[] row=db.query(hql,Object[].class,args).getFirst();return new long[]{((Number)row[0]).longValue(),((Number)row[1]).longValue()};}
    private static String stamp(Instant time){return DateTimeFormatter.ofPattern("HH:mm:ss · dd/MM/yyyy").withZone(ZONE).format(time);}
    private static long total(List<Object[]> rows){long total=0;for(Object[] row:rows)total=Math.addExact(total,((Order)row[0]).total);return total;}
    private static String change(long current,long previous,boolean future){if(future)return "Chưa bắt đầu";if(previous==0)return "— · Kỳ trước bằng 0";BigDecimal percent=BigDecimal.valueOf(current).subtract(BigDecimal.valueOf(previous)).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(Math.abs(previous)),1,RoundingMode.HALF_UP);return (percent.signum()>0?"↑ +":percent.signum()<0?"↓ ":"↔ ")+percent.toPlainString()+"% so cùng kỳ";}
    private static int bucket(Range range,Instant at){ZonedDateTime t=at.atZone(ZONE);return range.period().equals("day")?t.getHour():range.period().equals("year")?t.getMonthValue()-1:(int)java.time.temporal.ChronoUnit.DAYS.between(range.from(),t.toLocalDate());}
    private List<Map<String,Object>> chart(Range range,Instant now,Book book){
        int count=range.period().equals("day")?24:range.period().equals("year")?12:(int)java.time.temporal.ChronoUnit.DAYS.between(range.from(),range.until());long[] sums=new long[count],counter=new long[count];
        for(Object[] row:book.sales()){Order o=(Order)row[0];int b=bucket(range,o.paidAt);sums[b]=Math.addExact(sums[b],o.total);if(counter(row))counter[b]=Math.addExact(counter[b],o.total);}
        for(Object[] row:book.refunds()){Order o=(Order)row[0];int b=bucket(range,o.cancelledAt);sums[b]=Math.subtractExact(sums[b],o.total);if(counter(row))counter[b]=Math.subtractExact(counter[b],o.total);}
        long max=Math.max(0,Arrays.stream(sums).max().orElse(0));List<Map<String,Object>> points=new ArrayList<>();
        for(int i=0;i<count;i++){Instant start=range.period().equals("day")?range.from().atStartOfDay(ZONE).plusHours(i).toInstant():range.period().equals("year")?range.from().plusMonths(i).atStartOfDay(ZONE).toInstant():range.from().plusDays(i).atStartOfDay(ZONE).toInstant();boolean future=!start.isBefore(now);int height=max==0||sums[i]<=0?0:BigDecimal.valueOf(sums[i]).multiply(BigDecimal.valueOf(150)).divide(BigDecimal.valueOf(max),0,RoundingMode.HALF_UP).intValue();String label=range.period().equals("day")?String.format("%02dh",i):range.period().equals("year")?"T"+(i+1):range.from().plusDays(i).format(DateTimeFormatter.ofPattern("dd/MM"));int counterHeight=sums[i]<=0||counter[i]<=0?0:(int)Math.round(height*(double)Math.min(counter[i],sums[i])/sums[i]);
            Map<String,Object> point=new LinkedHashMap<>();point.put("label",label);point.put("value",sums[i]);point.put("amount",future?"Chưa tới":db.cash(sums[i]));point.put("onlineAmount",future?"—":db.cash(sums[i]-counter[i]));point.put("counterAmount",future?"—":db.cash(counter[i]));point.put("future",future);point.put("x",i*32+12);point.put("y",170-height);point.put("height",height);point.put("counterY",170-counterHeight);point.put("counterHeight",counterHeight);point.put("labelX",i*32+21);point.put("labelY",i%2==0?192:207);points.add(Collections.unmodifiableMap(point));}
        return List.copyOf(points);
    }
    private List<Map<String,Object>> top(Book book){
        Map<Long,Long> amounts=new HashMap<>();Map<Long,String> names=new HashMap<>();
        for(Object[] row:book.sales()){Order o=(Order)row[0];Long movieId=(Long)row[1];amounts.merge(movieId,o.total,Math::addExact);names.putIfAbsent(movieId,o.movieTitle);}
        for(Object[] row:book.refunds()){Order o=(Order)row[0];Long movieId=(Long)row[1];amounts.merge(movieId,-o.total,Math::addExact);names.putIfAbsent(movieId,o.movieTitle);}
        return amounts.entrySet().stream().sorted(Map.Entry.<Long,Long>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey())).map(e->Map.<String,Object>of("title",names.get(e.getKey())+" · #"+e.getKey(),"amount",db.cash(e.getValue()),"value",e.getValue())).toList();
    }
    private Object[] showArgs(Range range,Long cinemaId,Object... extra){List<Object> list=new ArrayList<>(List.of(range.from().atStartOfDay(ZONE).toInstant(),range.until().atStartOfDay(ZONE).toInstant()));if(cinemaId!=null)list.add(cinemaId);list.addAll(List.of(extra));return list.toArray();}
    private static String showScope(Long cinemaId){return "s.startsAt>=?1 and s.startsAt<?2 and s.status<>'CANCELLED'"+(cinemaId==null?"":" and r.cinemaId=?3");}
    private List<Map<String,Object>> occupancy(Range range,Long cinemaId){
        Object[] args=showArgs(range,cinemaId);String scope=showScope(cinemaId);
        Map<Long,Long> capacities=new HashMap<>(),sold=new HashMap<>(),used=new HashMap<>();
        db.query("select s.id,count(i) from %s s, %s r, %s i, %s seat where s.roomId=r.id and i.showtimeId=s.id and i.seatId=seat.id and seat.active=true and ".formatted(db.entityName(Showtime.class),db.entityName(Room.class),db.entityName(Inventory.class),db.entityName(Seat.class))+scope+" group by s.id",Object[].class,args).forEach(row->capacities.put((Long)row[0],(Long)row[1]));
        db.query("select s.id,t.status,count(t) from %s s, %s r, %s t where s.roomId=r.id and t.showtimeId=s.id and t.status in ('VALID','USED') and ".formatted(db.entityName(Showtime.class),db.entityName(Room.class),db.entityName(Ticket.class))+scope+" group by s.id,t.status",Object[].class,args).forEach(row->{sold.merge((Long)row[0],(Long)row[2],Long::sum);if("USED".equals(row[1]))used.put((Long)row[0],(Long)row[2]);});
        return db.query("select s,m.title,r.name,c.name from %s s, %s r, %s c, %s m where s.roomId=r.id and r.cinemaId=c.id and s.movieId=m.id and ".formatted(db.entityName(Showtime.class),db.entityName(Room.class),db.entityName(Cinema.class),db.entityName(Movie.class))+scope+" order by s.startsAt,s.id",Object[].class,args).stream().map(row->{Showtime s=(Showtime)row[0];long n=sold.getOrDefault(s.id,0L),capacity=capacities.getOrDefault(s.id,0L),in=used.getOrDefault(s.id,0L);return Map.<String,Object>of("id",s.id,"movieTitle",row[1],"roomName",row[2],"cinemaName",row[3],"when",db.date(s.startsAt),"occupancy",n+" / "+capacity+" ("+(capacity==0?"—":n*100/capacity+"%")+")","admitted",in+" / "+n+" ("+rate(in,n)+")");}).toList();
    }
    /** Same formula as the Staff check-in screen: issued = VALID + USED, rate = USED / issued (one decimal). */
    static String rate(long used,long issued){return issued==0?"—":String.format(Locale.ROOT,"%.1f%%",used*100.0/issued);}
    /** Check-in by show date in the whole period (like occupancy): admitted, still able to enter, no-shows once the window (start + 30 min) closed, voided tickets. */
    private Map<String,Object> admission(Range range,Long cinemaId,Instant now){
        String from=" from %s t, %s s, %s r where t.showtimeId=s.id and s.roomId=r.id and ".formatted(db.entityName(Ticket.class),db.entityName(Showtime.class),db.entityName(Room.class))+showScope(cinemaId);
        Map<String,Long> counts=new HashMap<>();db.query("select t.status,count(t)"+from+" group by t.status",Object[].class,showArgs(range,cinemaId)).forEach(row->counts.put((String)row[0],(Long)row[1]));
        long noShow=db.query("select count(t)"+from+" and t.status='VALID' and s.startsAt<?"+(cinemaId==null?3:4),Long.class,showArgs(range,cinemaId,now.minusSeconds(1800))).getFirst();
        long valid=counts.getOrDefault("VALID",0L),used=counts.getOrDefault("USED",0L),issued=valid+used;
        Map<String,Object> v=new LinkedHashMap<>();v.put("issued",issued);v.put("used",used);v.put("waiting",valid-noShow);v.put("noShow",noShow);v.put("voided",counts.getOrDefault("VOID",0L));v.put("rate",rate(used,issued));v.put("percent",issued==0?0:(int)(used*100/issued));
        return Collections.unmodifiableMap(v);
    }
    /** Counter shifts opened in the closed part of the period (by the shift's own cinema), using the same figures as the Staff shift slip. */
    private Map<String,Object> shifts(Range range,Long cinemaId){
        List<Shift> list=range.future()?List.of():db.list(Shift.class,"where e.openedAt>=?1 and e.openedAt<?2"+(cinemaId==null?"":" and e.cinemaId=?3")+" order by e.openedAt,e.id",args(range.from().atStartOfDay(ZONE).toInstant(),range.end(),cinemaId));
        long open=0,opening=0,cashIn=0,refunded=0,expected=0,counted=0,closedExpected=0,surplus=0,shortage=0,mismatched=0,broken=0,orders=0,tickets=0,cancelled=0;List<Map<String,Object>> attention=new ArrayList<>();
        for(Shift s:list){var f=counter.figures(s);opening+=f.opening();cashIn+=f.cashIn();refunded+=f.refunded();expected+=f.expected();orders+=f.orders();tickets+=f.tickets();cancelled+=f.cancelled();if(!f.intact())broken++;
            if(s.status.equals("OPEN"))open++;else if(f.counted()!=null){counted+=f.counted();closedExpected+=f.expected();long d=f.difference();if(d>0)surplus+=d;else shortage-=d;if(d!=0)mismatched++;}
            if(attention.size()<5&&(!f.intact()||(f.difference()!=null&&f.difference()!=0))){User u=db.get(User.class,s.staffId);attention.add(Map.of("id",s.id,"label","#"+s.id+" · "+u.name+" · "+db.get(Cinema.class,s.cinemaId).name,"detail",(f.intact()?"":"Số chốt khác giao dịch · ")+"Dự kiến "+db.cash(f.expected())+" · Đếm "+(f.counted()==null?"—":db.cash(f.counted()))+(f.difference()==null?"":" · "+signed(f.difference()))+(s.note==null?"":" · "+s.note)));}
        }
        Map<String,Object> v=new LinkedHashMap<>();v.put("count",list.size());v.put("open",open);v.put("closed",list.size()-open);v.put("opening",db.cash(opening));v.put("cashIn",db.cash(cashIn));v.put("refunded",db.cash(refunded));v.put("expected",db.cash(expected));
        v.put("counted",db.cash(counted));v.put("closedExpected",db.cash(closedExpected));v.put("surplus",db.cash(surplus));v.put("shortage",db.cash(shortage));v.put("difference",signed(surplus-shortage));v.put("differenceValue",surplus-shortage);v.put("mismatched",mismatched);v.put("broken",broken);
        v.put("orders",orders);v.put("tickets",tickets);v.put("cancelled",cancelled);v.put("attention",List.copyOf(attention));v.put("balanced",mismatched==0&&broken==0);v.put("day",range.period().equals("day")?range.from().toString():"");
        return Collections.unmodifiableMap(v);
    }
    public void remember(HttpSession session,Snapshot snapshot){synchronized(session){var cache=cache(session);cache.entrySet().removeIf(e->expired(e.getValue()));cache.put(snapshot.id(),snapshot);while(cache.size()>2)cache.remove(cache.keySet().iterator().next());}}
    public Snapshot recall(HttpSession session,String id){synchronized(session){var cache=cache(session);Snapshot snapshot=cache.get(id);if(snapshot==null||expired(snapshot)){cache.remove(id);throw new IllegalArgumentException("Báo cáo đã hết hạn, vui lòng làm mới báo cáo.");}return snapshot;}}
    @SuppressWarnings("unchecked") public AdminPage<Map<String,Object>> section(Snapshot snapshot,String section,int page){
        if(!Set.of("chart","topMovies","occupancy").contains(section))throw new IllegalArgumentException("Phần báo cáo không hợp lệ.");
        return AdminPage.slice((List<Map<String,Object>>)snapshot.model().get(section),page);
    }
    private boolean expired(Snapshot snapshot){return !clock.instant().isBefore(snapshot.createdAt().plusSeconds(900));}
    @SuppressWarnings("unchecked") private LinkedHashMap<String,Snapshot> cache(HttpSession session){Object value=session.getAttribute(CACHE);if(value instanceof LinkedHashMap<?,?>)return (LinkedHashMap<String,Snapshot>)value;var cache=new LinkedHashMap<String,Snapshot>();session.setAttribute(CACHE,cache);return cache;}
    public static String csvCell(Object value){String text=Objects.toString(value,"");if(!(value instanceof Number)&&text.matches("^[=+@\\-\\t\\r].*"))text="'"+text;return "\""+text.replace("\"","\"\"")+"\"";}
    public static String csvRow(Object... cells){return String.join(",",Arrays.stream(cells).map(ReportService::csvCell).toList())+"\r\n";}
}
