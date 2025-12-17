package com.example.finalexam_jvnc.service;

import com.example.finalexam_jvnc.model.Account;
import com.example.finalexam_jvnc.model.Notification;

import java.util.List;

public interface NotificationService {
    void createNotification(Account account, String title, String message, Notification.NotificationType type,
            String link);

    void createNotification(String username, String title, String message, Notification.NotificationType type,
            String link);

    List<Notification> getUserNotifications(String username);

    Long getUnreadCount(String username);

    void markAsRead(Long notificationId);

    void markAllAsRead(String username);
}
