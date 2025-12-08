package com.example.finalexam_jvnc.controller;

import com.example.finalexam_jvnc.service.AccountService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/staff")
public class StaffController {

    @Autowired
    private AccountService accountService;

    // Staff Login - kept for backward compatibility, but redirects to unified login
    @GetMapping("/login")
    public String showLoginPage() {
        return "redirect:/login";
    }

    // Staff Dashboard
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        String staffUsername = (String) session.getAttribute("staffUsername");
        if (staffUsername == null || !accountService.isStaff(staffUsername)) {
            return "redirect:/login";
        }
        model.addAttribute("staffUsername", staffUsername);
        return "staff/dashboard-staff";
    }
}

