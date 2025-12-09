package com.example.finalexam_jvnc.controller;

import com.example.finalexam_jvnc.dto.AdminLoginDTO;
import com.example.finalexam_jvnc.service.AccountService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private AccountService accountService;

    // Unified Login Page
    @GetMapping("/login")
    public String showLoginPage() {
        return "auth/login";
    }

    // Unified Login Handler - redirects based on role
    @PostMapping("/login")
    public String login(@ModelAttribute AdminLoginDTO loginDTO,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {
        // Validate credentials
        boolean isValid = accountService.validateCredentials(
            loginDTO.getUsername(),
            loginDTO.getPassword()
        );

        if (!isValid) {
            redirectAttributes.addFlashAttribute("error", "Invalid username or password");
            return "redirect:/login";
        }

        // Get user role
        String role = accountService.getUserRole(loginDTO.getUsername());
        
        if (role == null) {
            redirectAttributes.addFlashAttribute("error", "User has no assigned role");
            return "redirect:/login";
        }

        // Update last login time
        accountService.updateLastLoginAt(loginDTO.getUsername());

        // Set session attributes based on role
        if ("ADMIN".equals(role)) {
            session.setAttribute("adminUsername", loginDTO.getUsername());
            session.setAttribute("userRole", "ADMIN");
            return "redirect:/admin/dashboard";
        } else if ("STAFF".equals(role)) {
            session.setAttribute("staffUsername", loginDTO.getUsername());
            session.setAttribute("userRole", "STAFF");
            return "redirect:/staff/dashboard";
        } else if ("CUSTOMER".equals(role)) {
            session.setAttribute("customerUsername", loginDTO.getUsername());
            session.setAttribute("userRole", "CUSTOMER");
            return "redirect:/customer/dashboard";
        }

        redirectAttributes.addFlashAttribute("error", "Unknown role");
        return "redirect:/login";
    }

    // Register Page (only for customers)
    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }

    // Register Handler (only creates CUSTOMER accounts)
    @PostMapping("/register")
    public String register(@RequestParam String username,
                          @RequestParam String email,
                          @RequestParam String password,
                          @RequestParam String confirmPassword,
                          RedirectAttributes redirectAttributes) {
        // Validate password confirmation
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Passwords do not match");
            return "redirect:/register";
        }

        // Validate password length
        if (password.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "Password must be at least 6 characters");
            return "redirect:/register";
        }

        try {
            accountService.registerCustomer(username, email, password);
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Registration failed: " + e.getMessage());
            return "redirect:/register";
        }
    }

    // Logout Handler
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}

