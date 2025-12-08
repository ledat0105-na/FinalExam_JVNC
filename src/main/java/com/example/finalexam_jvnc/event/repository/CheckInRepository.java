package com.example.finalexam_jvnc.event.repository;

import com.example.finalexam_jvnc.event.model.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CheckInRepository extends JpaRepository<CheckIn, Long> {
    Optional<CheckIn> findByCheckInCode(String checkInCode);
    List<CheckIn> findByTicketOrderOrderId(Long orderId);
    List<CheckIn> findByStatus(String status);
}

