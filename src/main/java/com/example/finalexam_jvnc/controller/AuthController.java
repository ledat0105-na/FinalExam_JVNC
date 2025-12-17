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
        // Check if account exists and is locked
        com.example.finalexam_jvnc.model.Account account = accountService.getAccountByUsername(loginDTO.getUsername());
        
        if (account != null && Boolean.TRUE.equals(account.getIsLocked())) {
            redirectAttributes.addFlashAttribute("error", "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên để được hỗ trợ.");
            return "redirect:/login";
        }
        
        if (account != null && !Boolean.TRUE.equals(account.getIsActive())) {
            redirectAttributes.addFlashAttribute("error", "Tài khoản của bạn đã bị vô hiệu hóa. Vui lòng liên hệ quản trị viên để được hỗ trợ.");
            return "redirect:/login";
        }

        // Validate credentials
        boolean isValid = accountService.validateCredentials(
            loginDTO.getUsername(),
            loginDTO.getPassword()
        );

        if (!isValid) {
            redirectAttributes.addFlashAttribute("error", "Tên đăng nhập hoặc mật khẩu không đúng");
            return "redirect:/login";
        }

        // Get user role
        String role = accountService.getUserRole(loginDTO.getUsername());
        
        if (role == null) {
            redirectAttributes.addFlashAttribute("error", "Người dùng chưa được phân quyền");
            return "redirect:/login";
        }

        // Update last login time
        accountService.updateLastLoginAt(loginDTO.getUsername());

        // Set session attributes based on role
        // Note: serverInstanceId will be set automatically by SessionValidationFilter
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

        redirectAttributes.addFlashAttribute("error", "Vai trò không xác định");
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
            redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không khớp");
            return "redirect:/register";
        }

        // Validate password length
        if (password.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu phải có ít nhất 6 ký tự");
            return "redirect:/register";
        }

        try {
            accountService.registerCustomer(username, email, password);
            redirectAttributes.addFlashAttribute("success", "Đăng ký tài khoản thành công! Vui lòng đăng nhập để tiếp tục.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đăng ký thất bại: " + e.getMessage());
            return "redirect:/register";
        }
    }

    // Logout Handler
    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        String username = (String) session.getAttribute("customerUsername");
        if (username == null) {
            username = (String) session.getAttribute("adminUsername");
        }
        if (username == null) {
            username = (String) session.getAttribute("staffUsername");
        }
        session.invalidate();
        if (username != null) {
            redirectAttributes.addFlashAttribute("info", "Tạm biệt " + username + "! Hẹn gặp lại bạn.");
        }
        return "redirect:/login";
    }
}

