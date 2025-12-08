package com.example.finalexam_jvnc.iot.repository;

import com.example.finalexam_jvnc.iot.model.MaintenancePackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaintenancePackageRepository extends JpaRepository<MaintenancePackage, Long> {
    Optional<MaintenancePackage> findByPackageCode(String packageCode);
    List<MaintenancePackage> findByIsActiveTrue();
    List<MaintenancePackage> findByCategoryCategoryId(Long categoryId);
}

