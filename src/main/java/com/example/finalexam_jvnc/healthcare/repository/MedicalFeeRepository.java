package com.example.finalexam_jvnc.healthcare.repository;

import com.example.finalexam_jvnc.healthcare.model.MedicalFee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicalFeeRepository extends JpaRepository<MedicalFee, Long> {
    Optional<MedicalFee> findByFeeNumber(String feeNumber);
    List<MedicalFee> findByMedicalRecordRecordId(Long recordId);
    List<MedicalFee> findByStatus(String status);
}

