package com.example.finalexam_jvnc.controller;

import com.example.finalexam_jvnc.model.Notification;

import com.example.finalexam_jvnc.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/customer/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public String listNotifications(HttpSession session, Model model) {
        String username = (String) session.getAttribute("customerUsername");
        if (username == null) {
            return "redirect:/login";
        }

        List<Notification> notifications = notificationService.getUserNotifications(username);
        model.addAttribute("notifications", notifications);
        return "customer/notifications-list";
    }

    @PostMapping("/{id}/read")
    public String markAsRead(@PathVariable Long id, HttpSession session) {
        String username = (String) session.getAttribute("customerUsername");
        if (username == null) {
            return "redirect:/login";
        }

        notificationService.markAsRead(id);

        // Return to the referer or list page
        return "redirect:/customer/notifications";
    }

    @GetMapping("/{id}/go")
    public String markAsReadAndGo(@PathVariable Long id, HttpSession session) {
        String username = (String) session.getAttribute("customerUsername");
        if (username == null) {
            return "redirect:/login";
        }

        // Find notification to get link
        List<Notification> notifications = notificationService.getUserNotifications(username);
        Notification notification = notifications.stream()
                .filter(n -> n.getNotificationId().equals(id))
                .findFirst()
                .orElse(null);

        if (notification != null) {
            notificationService.markAsRead(id);
            if (notification.getLink() != null && !notification.getLink().isEmpty()) {
                return "redirect:" + notification.getLink();
            }
        }

        return "redirect:/customer/notifications";
    }

    @PostMapping("/read-all")
    public String markAllAsRead(HttpSession session) {
        String username = (String) session.getAttribute("customerUsername");
        if (username == null) {
            return "redirect:/login";
        }

        notificationService.markAllAsRead(username);
        return "redirect:/customer/notifications";
    }
}
