package com.example.finalexam_jvnc.healthcare.repository;

import com.example.finalexam_jvnc.healthcare.model.MedicalPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicalPackageRepository extends JpaRepository<MedicalPackage, Long> {
    Optional<MedicalPackage> findByPackageCode(String packageCode);
    List<MedicalPackage> findByIsActiveTrue();
    List<MedicalPackage> findByCategoryCategoryId(Long categoryId);
}

