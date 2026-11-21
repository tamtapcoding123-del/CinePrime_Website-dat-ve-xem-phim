package vn.cineprime.web;

import jakarta.servlet.http.*;
import java.time.*;
import java.util.*;
import org.springframework.dao.DataAccessException;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import vn.cineprime.auth.SecurityConfig.SessionUser;
import vn.cineprime.domain.Store;
import static vn.cineprime.domain.Domain.*;

@ControllerAdvice
public class PageAdvice {
    private final Store db;private final Clock clock;private final MaintenanceService maintenance;public PageAdvice(Store db,Clock clock,MaintenanceService maintenance){this.db=db;this.clock=clock;this.maintenance=maintenance;}
    @ModelAttribute public void common(Model model,Authentication authentication,HttpServletRequest request){
        // JSON responses do not render the shared page chrome.
        String path=request.getServletPath();
        if(path.startsWith("/api/")||path.equals("/admin/operations")||path.equals("/admin/media")||path.startsWith("/admin/layout")&&Objects.toString(request.getContentType(),"").startsWith("application/json")||path.endsWith(".csv")||path.equals("/catalog/showtimes")||path.equals("/payments/momo/ipn"))return;
        model.addAttribute("title","CinePrime · Hơn cả một buổi xem phim");model.addAttribute("currentPath",request.getServletPath());model.addAttribute("currentUrl",request.getServletPath()+(request.getQueryString()==null?"":"?"+request.getQueryString()));
        boolean operations=path.startsWith("/admin")||path.startsWith("/staff");
        if(!operations)model.addAttribute("siteNotices",db.views(db.list(Content.class,"where e.kind='NOTICE' and e.active=true order by e.id desc").stream().limit(1).toList()));
        if(authentication!=null&&authentication.getPrincipal() instanceof SessionUser p){User u=db.get(User.class,p.id());model.addAttribute("viewer",db.view(u));if(!operations){model.addAttribute("viewerInitials",initials(u.name));model.addAttribute("favoriteIds",new HashSet<>(db.query("select f.movieId from "+db.entityName(Favorite.class)+" f where f.userId=?1",Long.class,u.id)));}}
        if(!operations){for(String[] k:new String[][]{{"ABOUT","footerAbout"},{"POLICY","footerPolicies"},{"CONTACT","footerContacts"}})model.addAttribute(k[1],db.views(db.list(Content.class,"where e.kind=?1 and e.active=true order by e.id",k[0]).stream().limit(4).toList()));}
        CsrfToken token=(CsrfToken)request.getAttribute(CsrfToken.class.getName());if(token!=null){model.addAttribute("csrfName",token.getParameterName());model.addAttribute("csrfToken",token.getToken());model.addAttribute("csrfHeader",token.getHeaderName());}
        // Maintenance strip: notice before the window (everyone), and while it runs on the pages that stay open (admin, own tickets, staff check-in).
        if(!maintenance.phase().equals("OFF"))model.addAttribute("maintenance",maintenance.view());
        if(operations)return;
        model.addAttribute("cinemaOptions",db.views(db.list(Cinema.class,"where e.active=true order by e.city,e.name")));
        String requestedDay=request.getParameter("date");LocalDate today=LocalDate.now(clock.withZone(ZoneId.of("Asia/Ho_Chi_Minh")));
        model.addAttribute("days",java.util.stream.IntStream.range(0,7).mapToObj(i->{LocalDate day=today.plusDays(i);var link=org.springframework.web.util.UriComponentsBuilder.fromPath(request.getServletPath().equals("/")?"/showtimes":request.getServletPath());for(String key:List.of("movieId","cinemaId")){String value=request.getParameter(key);if(value!=null)link.queryParam(key,value);}link.queryParam("date",day);return Map.of("value",day.toString(),"href",link.build().encode().toUriString(),"selected",day.toString().equals(requestedDay==null?today.toString():requestedDay),"label",i==0?"Hôm nay":day.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM")),"weekday",i==0?"Hôm nay":weekday(day),"day",day.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM")));}).toList());
    }
    /** "Nguyễn Văn An" → "NA": first and last word, upper-cased, for the header avatar. */
    static String initials(String name){String[] w=Objects.toString(name,"").trim().split("\\s+");if(w[0].isEmpty())return "?";String s=w[0].substring(0,1)+(w.length>1?w[w.length-1].substring(0,1):"");return s.toUpperCase(Locale.forLanguageTag("vi"));}
    static String weekday(LocalDate day){return day.getDayOfWeek()==DayOfWeek.SUNDAY?"Chủ nhật":"Thứ "+(day.getDayOfWeek().getValue()+1);}
    @ExceptionHandler({IllegalArgumentException.class,DataAccessException.class,AccessDeniedException.class})
    public Object failure(Exception exception,HttpServletRequest request,HttpServletResponse response){
        int code=exception instanceof AccessDeniedException?403:exception instanceof DataAccessException?409:400;
        String message=exception instanceof DataAccessException?"Dữ liệu bị trùng, đang được cập nhật hoặc còn liên kết. Hãy tải lại và kiểm tra thông tin.":exception.getMessage();
        if(request.getServletPath().startsWith("/api/")||request.getServletPath().equals("/admin/operations")||request.getServletPath().equals("/admin/media")||request.getServletPath().startsWith("/admin/layout")&&java.util.Objects.toString(request.getContentType(),"").startsWith("application/json")||request.getServletPath().equals("/payments/momo/ipn"))return ResponseEntity.status(code).body(Map.of("message",message,"code",exception instanceof vn.cineprime.staff.CheckInService.StaffProblem staff?staff.code():exception instanceof AccessDeniedException?"FORBIDDEN":"INVALID_REQUEST"));
        boolean operations=request.getServletPath().startsWith("/admin")||request.getServletPath().startsWith("/staff");response.setStatus(code);ModelAndView view=new ModelAndView(operations?"operations-error":"error");view.addObject("message",message);view.addObject("title","Không thể hoàn tất thao tác");if(operations){view.addObject("operationsRole",request.getServletPath().startsWith("/staff")?"Soát vé":"Quản trị");CsrfToken token=(CsrfToken)request.getAttribute(CsrfToken.class.getName());if(token!=null){view.addObject("csrfName",token.getParameterName());view.addObject("csrfToken",token.getToken());view.addObject("csrfHeader",token.getHeaderName());}}return view;
    }
    /** Maintenance window is running: 503 with Retry-After; JSON for API calls, otherwise the maintenance card. */
    @ExceptionHandler(MaintenanceGate.Blocked.class)
    public Object maintenance(MaintenanceGate.Blocked blocked,HttpServletRequest request,HttpServletResponse response,Authentication authentication){
        Map<String,Object> m=maintenance.view();response.setHeader(HttpHeaders.RETRY_AFTER,m.get("retryAfter").toString());response.setHeader(HttpHeaders.CACHE_CONTROL,"no-store");
        String message=m.get("title")+". "+(blocked.staff?"Chức năng này tạm dừng trong thời gian bảo trì.":"Vui lòng quay lại sau"+(Boolean.TRUE.equals(m.get("hasEnd"))&&!Boolean.TRUE.equals(m.get("overdue"))?" "+m.get("endText")+".":"."));
        String path=request.getServletPath();if(path.startsWith("/api/")||request.getMethod().equals("POST")&&Objects.toString(request.getHeader(HttpHeaders.ACCEPT),"").contains("application/json"))return ResponseEntity.status(503).header(HttpHeaders.RETRY_AFTER,m.get("retryAfter").toString()).body(Map.of("code","MAINTENANCE","message",message));
        response.setStatus(503);ModelAndView view=new ModelAndView("maintenance");view.addObject("maintenance",m);view.addObject("staffView",blocked.staff);view.addObject("staffMode",m.get("staffMode"));view.addObject("title",m.get("title")+" · CinePrime");
        if(authentication!=null&&authentication.getPrincipal() instanceof SessionUser p)view.addObject("viewer",db.view(db.get(User.class,p.id())));
        CsrfToken token=(CsrfToken)request.getAttribute(CsrfToken.class.getName());if(token!=null){view.addObject("csrfName",token.getParameterName());view.addObject("csrfToken",token.getToken());}
        return view;
    }
    @ExceptionHandler(org.springframework.web.client.RestClientException.class)
    public ModelAndView paymentUnavailable(HttpServletResponse response){response.setStatus(503);ModelAndView view=new ModelAndView("error");view.addObject("message","Chưa nhận được phản hồi MoMo. Nếu đã thanh toán, hãy mở đơn và Kiểm tra thanh toán; không cần đặt lại ghế.");return view;}
    @ExceptionHandler(org.springframework.web.multipart.MultipartException.class)
    public ResponseEntity<Map<String,Object>> uploadTooLarge(){return ResponseEntity.status(413).body(Map.of("message","File vượt giới hạn hoặc không đọc được. Ảnh tối đa 5 MB; video MP4 tối đa 100 MB."));}
}
