package com.example.finalexam_jvnc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    // Public dashboard - trang giới thiệu, không cần đăng nhập
    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

}

