package com.example.finalexam_jvnc.event.repository;

import com.example.finalexam_jvnc.event.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Optional<Ticket> findByTicketCode(String ticketCode);
    List<Ticket> findByIsActiveTrue();
    List<Ticket> findByCategoryCategoryId(Long categoryId);
}

