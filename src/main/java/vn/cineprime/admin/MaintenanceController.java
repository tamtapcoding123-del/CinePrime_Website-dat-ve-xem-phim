package vn.cineprime.admin;

import java.util.*;
import jakarta.servlet.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.cineprime.auth.SecurityConfig.SessionUser;
import vn.cineprime.web.MaintenanceService;

/** Hệ thống → Bảo trì website: texts, window, staff mode and contact shown on the maintenance card; turn on, schedule, turn off. */
@Controller @RequestMapping("/admin/maintenance")
public class MaintenanceController {
    private final MaintenanceService maintenance;private final AdminController navigation;
    public MaintenanceController(MaintenanceService maintenance,AdminController navigation){this.maintenance=maintenance;this.navigation=navigation;}
    @ModelAttribute public void nav(Model model,HttpServletRequest request){navigation.nav(model,request);model.addAttribute("title","Bảo trì website · CinePrime Admin");model.addAttribute("resourceLabel","Bảo trì website");}

    @GetMapping public String page(Model model){if(!model.containsAttribute("form"))model.addAttribute("form",maintenance.form());model.addAttribute("state",maintenance.view());return "admin/maintenance";}
    /** The customer card exactly as it will look, from the saved texts (opens in a new tab). */
    @GetMapping("/preview") public String preview(Model model){model.addAttribute("maintenance",maintenance.preview());model.addAttribute("staffView",false);model.addAttribute("previewMode",true);model.addAttribute("title","Xem trước trang bảo trì · CinePrime");return "maintenance";}
    @PostMapping public String save(@AuthenticationPrincipal SessionUser user,@RequestParam Map<String,String> values,RedirectAttributes redirect,Model model,HttpServletResponse response){
        boolean enable="enable".equals(values.get("action"));
        try{String phase=maintenance.save(user.id(),values,enable);redirect.addFlashAttribute("notice",switch(phase){case "ACTIVE"->enable?"Đã bật bảo trì. Khách đang thấy trang bảo trì.":"Đã lưu. Trang bảo trì cập nhật ngay cho khách.";case "SCHEDULED"->"Đã hẹn giờ bảo trì. Khách thấy dải thông báo cho tới giờ bắt đầu.";default->"Đã lưu nội dung. Bảo trì vẫn đang tắt.";});return "redirect:/admin/maintenance";}
        catch(IllegalArgumentException e){response.setStatus(400);model.addAttribute("form",new HashMap<String,Object>(values));model.addAttribute("actionError",e.getMessage());model.addAttribute("state",maintenance.view());return "admin/maintenance";}
    }
    @PostMapping("/off") public String off(@AuthenticationPrincipal SessionUser user,RedirectAttributes redirect){redirect.addFlashAttribute("notice",maintenance.turnOff(user.id())?"Đã tắt bảo trì. Website hoạt động lại bình thường.":"Bảo trì đang tắt.");return "redirect:/admin/maintenance";}
}
