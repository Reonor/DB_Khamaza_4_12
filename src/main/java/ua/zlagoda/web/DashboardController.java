package ua.zlagoda.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ua.zlagoda.security.SecurityUser;

@Controller
public class DashboardController {

    @GetMapping("/")
    public String dashboard(@AuthenticationPrincipal SecurityUser user, Model model) {
        model.addAttribute("fullName", user.getFullName());
        return "dashboard";
    }
}
