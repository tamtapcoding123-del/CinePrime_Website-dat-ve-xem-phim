package vn.cineprime.web;

import jakarta.servlet.http.*;
import java.util.regex.Pattern;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.*;
import vn.cineprime.auth.SecurityConfig.SessionUser;

/**
 * Blocks requests while maintenance is ACTIVE. Admin is never blocked. Customers and visitors keep sign-in, their own
 * tickets/orders (read and payment check) and the MoMo IPN, so payments already in flight still settle. Staff follow
 * staffMode: WORK = nothing blocked, CHECKIN = only the check-in board and its APIs, LOCKED = all Staff screens.
 * A blocked request ends in {@link Blocked}, rendered by PageAdvice as the maintenance card (HTML) or a 503 JSON body.
 */
@Configuration
public class MaintenanceGate implements WebMvcConfigurer, HandlerInterceptor {
    /** Thrown from the interceptor; `staff` picks the Staff wording of the card. */
    public static class Blocked extends RuntimeException { public final boolean staff; public Blocked(boolean staff){super("MAINTENANCE",null,false,false);this.staff=staff;} }
    private static final Pattern CUSTOMER_OPEN=Pattern.compile("/(login|logout|error|payments/momo/ipn)|/account|/orders/\\d+(/verify)?|/api/orders/\\d+|/tickets/\\d+/(download|qr\\.png)");
    private static final Pattern STAFF_CHECKIN=Pattern.compile("/staff|/api/staff/(lookup|check-in|showtimes/\\d+/tickets|tickets/\\d+)");
    private final MaintenanceService maintenance;
    public MaintenanceGate(MaintenanceService maintenance){this.maintenance=maintenance;}
    @Override public void addInterceptors(InterceptorRegistry registry){registry.addInterceptor(this).excludePathPatterns("/css/**","/js/**","/images/**","/fonts/**","/media/**","/favicon.ico");}
    @Override public boolean preHandle(HttpServletRequest request,HttpServletResponse response,Object handler){
        String path=request.getServletPath();
        if(path.startsWith("/admin")||path.startsWith("/api/admin")||!maintenance.active())return true;
        Authentication a=SecurityContextHolder.getContext().getAuthentication();String role=a!=null&&a.getPrincipal() instanceof SessionUser u?u.role():"";
        if(role.equals("ADMIN"))return true;
        boolean staffPath=path.startsWith("/staff")||path.startsWith("/api/staff");
        if(staffPath){String mode=maintenance.snapshot().staffMode();if(mode.equals("WORK")||mode.equals("CHECKIN")&&STAFF_CHECKIN.matcher(path).matches())return true;throw new Blocked(true);}
        if(CUSTOMER_OPEN.matcher(path).matches()&&(!path.equals("/account")||request.getMethod().equals("GET")))return true;
        throw new Blocked(false);
    }
}
