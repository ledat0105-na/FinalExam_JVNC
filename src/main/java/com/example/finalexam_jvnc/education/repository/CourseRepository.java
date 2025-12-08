package com.example.finalexam_jvnc.education.repository;

import com.example.finalexam_jvnc.education.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByCourseCode(String courseCode);
    List<Course> findByIsActiveTrue();
    List<Course> findByCategoryCategoryId(Long categoryId);
}

