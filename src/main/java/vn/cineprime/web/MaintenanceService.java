package vn.cineprime.web;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.cineprime.domain.Store;
import static vn.cineprime.domain.Domain.*;

/**
 * Website maintenance (18/11/2026). One row, edited by Admin. Phases: OFF; SCHEDULED (enabled, start in the future: notice strip,
 * booking still works); ACTIVE (enabled, start reached: customer pages show the maintenance card, Staff per staffMode).
 * ACTIVE lasts past the expected end until an Admin turns it off. The row is cached briefly because every request reads it;
 * saving clears the cache (single app instance).
 */
@Service
public class MaintenanceService {
    public static final ZoneId ZONE=ZoneId.of("Asia/Ho_Chi_Minh");
    public static final Set<String> STAFF_MODES=Set.of("WORK","CHECKIN","LOCKED");
    private static final DateTimeFormatter WHEN=DateTimeFormatter.ofPattern("HH:mm · dd/MM/yyyy"),DAY=DateTimeFormatter.ofPattern("dd/MM/yyyy"),HOUR=DateTimeFormatter.ofPattern("HH:mm"),SHORT=DateTimeFormatter.ofPattern("HH:mm dd/MM");
    private static final long CACHE_NANOS=3_000_000_000L;
    /** Immutable copy of the row, so the cache never hands out a managed entity. */
    public record Snapshot(boolean enabled,String title,String message,List<String> affected,Instant startsAt,Instant expectedEndAt,String staffMode,String contactPhone,String contactEmail,Instant updatedAt){}
    private final Store db;private final Clock clock;
    private volatile Snapshot cached;private volatile long cachedAt;
    public MaintenanceService(Store db,Clock clock){this.db=db;this.clock=clock;}

    public Snapshot snapshot(){Snapshot s=cached;if(s!=null&&System.nanoTime()-cachedAt<CACHE_NANOS)return s;s=db.first(Maintenance.class,"order by e.id").map(MaintenanceService::copy).orElse(new Snapshot(false,"","",List.of(),null,null,"WORK","","",null));cached=s;cachedAt=System.nanoTime();return s;}
    private static Snapshot copy(Maintenance m){return new Snapshot(m.enabled,Objects.toString(m.title,""),Objects.toString(m.message,""),lines(m.affected),m.startsAt,m.expectedEndAt,STAFF_MODES.contains(m.staffMode)?m.staffMode:"CHECKIN",Objects.toString(m.contactPhone,""),Objects.toString(m.contactEmail,""),m.updatedAt);}
    static List<String> lines(String text){return Objects.toString(text,"").lines().map(String::strip).filter(l->!l.isEmpty()).toList();}
    public void forget(){cached=null;}

    public String phase(){return phase(snapshot());}
    String phase(Snapshot s){if(!s.enabled())return "OFF";return s.startsAt()!=null&&s.startsAt().isAfter(clock.instant())?"SCHEDULED":"ACTIVE";}
    public boolean active(){return phase().equals("ACTIVE");}

    /** Everything the card, strips and admin page show, already formatted in Vietnamese. */
    public Map<String,Object> view(){return view(snapshot(),phase(snapshot()));}
    /** Admin preview: the saved texts shown as if maintenance had started now. */
    public Map<String,Object> preview(){Snapshot s=snapshot();return view(s.startsAt()==null||s.startsAt().isAfter(clock.instant())?new Snapshot(true,s.title(),s.message(),s.affected(),clock.instant(),s.expectedEndAt()!=null&&s.expectedEndAt().isAfter(clock.instant())?s.expectedEndAt():null,s.staffMode(),s.contactPhone(),s.contactEmail(),s.updatedAt()):s,"ACTIVE");}
    Map<String,Object> view(Snapshot s,String phase){
        Instant now=clock.instant();Map<String,Object> v=new HashMap<>();
        v.put("phase",phase);v.put("active",phase.equals("ACTIVE"));v.put("scheduled",phase.equals("SCHEDULED"));v.put("title",s.title());v.put("message",s.message());v.put("affected",s.affected());
        v.put("startText",s.startsAt()==null?"":WHEN.format(s.startsAt().atZone(ZONE)));v.put("endText",s.expectedEndAt()==null?"":WHEN.format(s.expectedEndAt().atZone(ZONE)));
        v.put("startHour",s.startsAt()==null?"":HOUR.format(s.startsAt().atZone(ZONE)));v.put("startDay",s.startsAt()==null?"":DAY.format(s.startsAt().atZone(ZONE)));
        v.put("endHour",s.expectedEndAt()==null?"":HOUR.format(s.expectedEndAt().atZone(ZONE)));v.put("endDay",s.expectedEndAt()==null?"":DAY.format(s.expectedEndAt().atZone(ZONE)));
        v.put("startShort",s.startsAt()==null?"":SHORT.format(s.startsAt().atZone(ZONE)));v.put("endShort",s.expectedEndAt()==null?"":SHORT.format(s.expectedEndAt().atZone(ZONE)));
        boolean overdue=phase.equals("ACTIVE")&&s.expectedEndAt()!=null&&!s.expectedEndAt().isAfter(now);v.put("overdue",overdue);v.put("hasEnd",s.expectedEndAt()!=null);
        v.put("remainingText",s.expectedEndAt()==null||overdue?"":duration(Duration.between(now,s.expectedEndAt())));
        v.put("startsInText",phase.equals("SCHEDULED")?duration(Duration.between(now,s.startsAt())):"");
        int progress=0;if(phase.equals("ACTIVE")&&s.startsAt()!=null&&s.expectedEndAt()!=null){long total=Duration.between(s.startsAt(),s.expectedEndAt()).toSeconds(),done=Duration.between(s.startsAt(),now).toSeconds();progress=total<=0?100:(int)Math.max(0,Math.min(100,done*100/total));}
        v.put("progress",progress);v.put("showProgress",phase.equals("ACTIVE")&&s.startsAt()!=null&&s.expectedEndAt()!=null);
        v.put("staffMode",s.staffMode());v.put("staffModeLabel",staffLabel(s.staffMode()));v.put("contactPhone",s.contactPhone());v.put("contactPhoneHref",s.contactPhone().replaceAll("[^0-9+]",""));v.put("contactEmail",s.contactEmail());v.put("hasContact",!s.contactPhone().isEmpty()||!s.contactEmail().isEmpty());
        v.put("updatedText",s.updatedAt()==null?"":WHEN.format(s.updatedAt().atZone(ZONE)));
        v.put("retryAfter",s.expectedEndAt()==null||overdue?600:Math.max(60,Duration.between(now,s.expectedEndAt()).toSeconds()));
        return v;
    }
    public static String staffLabel(String mode){return switch(mode){case "WORK"->"Nhân viên làm việc bình thường";case "LOCKED"->"Khóa cả màn Staff";default->"Nhân viên chỉ soát vé";};}
    /** "2 giờ 15 phút", "45 phút", "dưới 1 phút", "1 ngày 3 giờ". */
    static String duration(Duration d){long m=Math.max(0,(d.toSeconds()+59)/60);if(m<1)return "dưới 1 phút";long days=m/1440,h=m%1440/60,min=m%60;StringBuilder b=new StringBuilder();if(days>0)b.append(days).append(" ngày ");if(h>0)b.append(h).append(" giờ ");if(min>0&&days==0)b.append(min).append(" phút");return b.toString().strip();}

    /** Values for the admin form: date (yyyy-MM-dd) and 24-hour HH:mm text, as the other admin screens. */
    public Map<String,Object> form(){Snapshot s=snapshot();Map<String,Object> f=new HashMap<>();f.put("title",s.title());f.put("message",s.message());f.put("affected",String.join("\n",s.affected()));f.put("staffMode",s.staffMode());f.put("contactPhone",s.contactPhone());f.put("contactEmail",s.contactEmail());
        boolean scheduled=s.startsAt()!=null&&s.startsAt().isAfter(clock.instant());f.put("startMode",scheduled?"schedule":"now");LocalDateTime start=(scheduled?s.startsAt():clock.instant().plus(Duration.ofHours(1))).atZone(ZONE).toLocalDateTime();f.put("startDate",start.toLocalDate().toString());f.put("startTime",HOUR.format(start));
        f.put("endDate",s.expectedEndAt()==null?"":s.expectedEndAt().atZone(ZONE).toLocalDate().toString());f.put("endTime",s.expectedEndAt()==null?"":HOUR.format(s.expectedEndAt().atZone(ZONE)));return f;}

    /**
     * Saves texts, window, staff mode and contact. action "enable" turns maintenance on, "save" keeps the current on/off.
     * Start "now" records the moment it actually starts (kept while it stays on); "schedule" needs a future time unless unchanged.
     */
    @Transactional public String save(Long actor,Map<String,String> values,boolean enable){
        Maintenance m=db.first(Maintenance.class,"order by e.id").orElseGet(Maintenance::new);Instant now=clock.instant();boolean wasOn=m.enabled,on=enable||m.enabled;
        String title=values.getOrDefault("title","").strip(),message=values.getOrDefault("message","").strip(),affected=String.join("\n",lines(values.getOrDefault("affected","")));
        if(title.isEmpty())throw new IllegalArgumentException("Nhập tiêu đề hiển thị cho khách.");if(title.length()>160)throw new IllegalArgumentException("Tiêu đề tối đa 160 ký tự.");
        if(message.isEmpty())throw new IllegalArgumentException("Nhập nội dung thông báo cho khách.");if(message.length()>2000)throw new IllegalArgumentException("Nội dung tối đa 2.000 ký tự.");
        if(affected.length()>2000)throw new IllegalArgumentException("Danh sách chức năng tạm ngừng tối đa 2.000 ký tự.");if(lines(affected).size()>12)throw new IllegalArgumentException("Tối đa 12 dòng chức năng tạm ngừng.");
        String mode=values.getOrDefault("staffMode","CHECKIN");if(!STAFF_MODES.contains(mode))throw new IllegalArgumentException("Chọn cách áp dụng cho nhân viên.");
        String phone=values.getOrDefault("contactPhone","").strip(),email=values.getOrDefault("contactEmail","").strip();
        if(!phone.isEmpty()&&!phone.matches("[0-9 +().-]{6,40}"))throw new IllegalArgumentException("Hotline chỉ gồm số, khoảng trắng và + ( ) . - (6–40 ký tự).");
        if(!email.isEmpty()&&(email.length()>160||!email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")))throw new IllegalArgumentException("Email liên hệ không hợp lệ.");
        Instant start;
        if("schedule".equals(values.get("startMode"))){start=moment(values.get("startDate"),values.get("startTime"),"bắt đầu");if(start==null)throw new IllegalArgumentException("Chọn ngày và giờ bắt đầu bảo trì.");if(!start.isAfter(now)&&!(wasOn&&start.equals(m.startsAt)))throw new IllegalArgumentException("Giờ bắt đầu hẹn trước phải sau thời điểm hiện tại; muốn bảo trì ngay hãy chọn \"Ngay khi bật\".");}
        else start=on?(wasOn&&m.startsAt!=null&&!m.startsAt.isAfter(now)?m.startsAt:now):null;
        Instant end=moment(values.get("endDate"),values.get("endTime"),"dự kiến hoàn tất");
        if(end!=null&&!end.isAfter(start==null?now:start))throw new IllegalArgumentException("Giờ dự kiến hoàn tất phải sau giờ bắt đầu.");
        if(end!=null&&on&&!end.isAfter(now)&&!end.equals(m.expectedEndAt))throw new IllegalArgumentException("Giờ dự kiến hoàn tất đã qua. Chọn giờ muộn hơn hoặc để trống.");
        m.enabled=on;m.title=title;m.message=message;m.affected=affected.isEmpty()?null:affected;m.startsAt=start;m.expectedEndAt=end;m.staffMode=mode;m.contactPhone=phone.isEmpty()?null:phone;m.contactEmail=email.isEmpty()?null:email;m.updatedAt=now;m.updatedBy=actor;db.save(m);forget();
        String phase=!on?"OFF":start!=null&&start.isAfter(now)?"SCHEDULED":"ACTIVE";
        db.audit(actor,!wasOn&&on?"MAINTENANCE_ON":"MAINTENANCE_SAVE",switch(phase){case "OFF"->"Đang tắt";case "SCHEDULED"->"Hẹn "+WHEN.format(start.atZone(ZONE));default->"Đang bảo trì";}+" · "+staffLabel(mode));
        return phase;
    }
    @Transactional public boolean turnOff(Long actor){Maintenance m=db.first(Maintenance.class,"order by e.id").orElse(null);if(m==null||!m.enabled)return false;m.enabled=false;m.updatedAt=clock.instant();m.updatedBy=actor;db.save(m);forget();db.audit(actor,"MAINTENANCE_OFF","Mở lại website");return true;}
    private static Instant moment(String date,String time,String what){
        date=Objects.toString(date,"").strip();time=Objects.toString(time,"").strip();if(date.isEmpty()&&time.isEmpty())return null;
        if(date.isEmpty()||time.isEmpty())throw new IllegalArgumentException("Nhập đủ ngày và giờ "+what+".");
        if(!time.matches("(?:[01][0-9]|2[0-3]):[0-5][0-9]"))throw new IllegalArgumentException("Giờ "+what+" phải có dạng HH:mm (24 giờ).");
        try{return LocalDate.parse(date).atTime(LocalTime.parse(time)).atZone(ZONE).toInstant();}catch(java.time.format.DateTimeParseException e){throw new IllegalArgumentException("Ngày "+what+" không hợp lệ.");}
    }
}
