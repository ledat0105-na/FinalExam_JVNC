package com.example.finalexam_jvnc.controller;

import com.example.finalexam_jvnc.service.AccountService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    private AccountService accountService;

    // Customer Dashboard - yêu cầu đăng nhập
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        String customerUsername = (String) session.getAttribute("customerUsername");
        if (customerUsername == null) {
            if (session != null) {
                session.invalidate();
            }
            return "redirect:/dashboard";
        }
        model.addAttribute("customerUsername", customerUsername);
        return "customer/dashboard-customer";
    }
}

