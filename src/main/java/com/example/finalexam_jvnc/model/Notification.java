package com.example.finalexam_jvnc.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AccountId", nullable = false)
    private Account account;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    private NotificationType type; // SUCCESS, INFO, WARNING, ERROR

    private Boolean isRead;

    private String link; // Optional link to redirect user

    private LocalDateTime createdAt;

    public enum NotificationType {
        SUCCESS, INFO, WARNING, ERROR
    }
}
