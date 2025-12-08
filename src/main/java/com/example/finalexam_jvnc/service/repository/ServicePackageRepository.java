package com.example.finalexam_jvnc.service.repository;

import com.example.finalexam_jvnc.service.model.ServicePackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServicePackageRepository extends JpaRepository<ServicePackage, Long> {
    Optional<ServicePackage> findByPackageCode(String packageCode);
    List<ServicePackage> findByIsActiveTrue();
    List<ServicePackage> findByCategoryCategoryId(Long categoryId);
}

