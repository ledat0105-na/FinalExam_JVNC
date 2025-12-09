package com.example.finalexam_jvnc.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import com.example.finalexam_jvnc.service.AccountService;

@Controller
public class HomeController {

    @Autowired
    private AccountService accountService;

    @GetMapping("/")
    public String home(HttpSession session) {
        // Redirect to appropriate dashboard based on role
        String adminUsername = (String) session.getAttribute("adminUsername");
        String staffUsername = (String) session.getAttribute("staffUsername");
        String customerUsername = (String) session.getAttribute("customerUsername");
        
        if (adminUsername != null) {
            return "redirect:/admin/dashboard";
        } else if (staffUsername != null) {
            return "redirect:/staff/dashboard";
        } else if (customerUsername != null) {
            return "redirect:/customer/dashboard";
        }
        return "redirect:/dashboard";
    }

    // Public dashboard - redirect to appropriate dashboard if logged in
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session) {
        String adminUsername = (String) session.getAttribute("adminUsername");
        String staffUsername = (String) session.getAttribute("staffUsername");
        String customerUsername = (String) session.getAttribute("customerUsername");
        
        if (adminUsername != null) {
            return "redirect:/admin/dashboard";
        } else if (staffUsername != null) {
            return "redirect:/staff/dashboard";
        } else if (customerUsername != null) {
            return "redirect:/customer/dashboard";
        }
        
        // Redirect to login if not logged in
        return "redirect:/login";
    }

}

