package com.example.finalexam_jvnc.repository;

import com.example.finalexam_jvnc.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByAccount_UsernameOrderByCreatedAtDesc(String username);

    Long countByAccount_UsernameAndIsReadFalse(String username);
}
