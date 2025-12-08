package com.example.finalexam_jvnc.healthcare.repository;

import com.example.finalexam_jvnc.healthcare.model.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    Optional<MedicalRecord> findByRecordNumber(String recordNumber);
    List<MedicalRecord> findByStatus(String status);
    List<MedicalRecord> findByPatientAccountId(Long patientId);
    List<MedicalRecord> findByPackagePackageId(Long packageId);
    
    @Query("SELECT m FROM MedicalRecord m LEFT JOIN FETCH m.patient LEFT JOIN FETCH m.package ORDER BY m.recordDate DESC")
    List<MedicalRecord> findAllWithDetails();
}

