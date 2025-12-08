package com.example.finalexam_jvnc.service.repository;

import com.example.finalexam_jvnc.service.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    Optional<Appointment> findByAppointmentNumber(String appointmentNumber);
    List<Appointment> findByStatus(String status);
    List<Appointment> findByCustomerAccountId(Long customerId);
    List<Appointment> findByAppointmentDateBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.customer LEFT JOIN FETCH a.servicePackage ORDER BY a.appointmentDate DESC")
    List<Appointment> findAllWithDetails();
}

