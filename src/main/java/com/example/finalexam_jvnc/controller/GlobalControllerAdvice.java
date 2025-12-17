package com.example.finalexam_jvnc.controller;

import com.example.finalexam_jvnc.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private CartService cartService;

    @Autowired
    private com.example.finalexam_jvnc.service.NotificationService notificationService;

    @ModelAttribute
    public void addGlobalAttributes(Model model, HttpSession session) {
        String customerUsername = (String) session.getAttribute("customerUsername");
        if (customerUsername != null) {
            Integer cartItemCount = cartService.getCartItemCount(customerUsername);
            model.addAttribute("navbarCartCount", cartItemCount);

            Long unreadNotificationCount = notificationService.getUnreadCount(customerUsername);
            model.addAttribute("unreadNotificationCount", unreadNotificationCount);
        }
    }
}
