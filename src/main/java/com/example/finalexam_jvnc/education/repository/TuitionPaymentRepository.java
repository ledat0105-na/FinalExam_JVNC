package com.example.finalexam_jvnc.education.repository;

import com.example.finalexam_jvnc.education.model.TuitionPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TuitionPaymentRepository extends JpaRepository<TuitionPayment, Long> {
    Optional<TuitionPayment> findByPaymentNumber(String paymentNumber);
    List<TuitionPayment> findByEnrollmentEnrollmentId(Long enrollmentId);
    List<TuitionPayment> findByStatus(String status);
}

