package com.example.finalexam_jvnc.education.repository;

import com.example.finalexam_jvnc.education.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    Optional<Enrollment> findByEnrollmentNumber(String enrollmentNumber);
    List<Enrollment> findByStatus(String status);
    List<Enrollment> findByStudentAccountId(Long studentId);
    List<Enrollment> findByCourseCourseId(Long courseId);
    
    @Query("SELECT e FROM Enrollment e LEFT JOIN FETCH e.student LEFT JOIN FETCH e.course ORDER BY e.enrollmentDate DESC")
    List<Enrollment> findAllWithDetails();
}

