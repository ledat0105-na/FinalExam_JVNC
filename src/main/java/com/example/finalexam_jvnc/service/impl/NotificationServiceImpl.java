package com.example.finalexam_jvnc.service.impl;

import com.example.finalexam_jvnc.model.Account;
import com.example.finalexam_jvnc.model.Notification;
import com.example.finalexam_jvnc.repository.AccountRepository;
import com.example.finalexam_jvnc.repository.NotificationRepository;
import com.example.finalexam_jvnc.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Override
    public void createNotification(Account account, String title, String message, Notification.NotificationType type,
            String link) {
        Notification notification = Notification.builder()
                .account(account)
                .title(title)
                .message(message)
                .type(type)
                .link(link)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
    }

    @Override
    public void createNotification(String username, String title, String message, Notification.NotificationType type,
            String link) {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Account not found: " + username));
        createNotification(account, title, message, type, link);
    }

    @Override
    public List<Notification> getUserNotifications(String username) {
        return notificationRepository.findByAccount_UsernameOrderByCreatedAtDesc(username);
    }

    @Override
    public Long getUnreadCount(String username) {
        return notificationRepository.countByAccount_UsernameAndIsReadFalse(username);
    }

    @Override
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        });
    }

    @Override
    public void markAllAsRead(String username) {
        List<Notification> notifications = notificationRepository.findByAccount_UsernameOrderByCreatedAtDesc(username);
        notifications.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(notifications);
    }
}
