package com.example.finalexam_jvnc.service.repository;

import com.example.finalexam_jvnc.service.model.ServiceReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceReceiptRepository extends JpaRepository<ServiceReceipt, Long> {
    Optional<ServiceReceipt> findByReceiptNumber(String receiptNumber);
    List<ServiceReceipt> findByAppointmentAppointmentId(Long appointmentId);
}

