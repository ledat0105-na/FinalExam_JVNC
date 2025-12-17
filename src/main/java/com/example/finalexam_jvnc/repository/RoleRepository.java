package com.example.finalexam_jvnc.repository;

import com.example.finalexam_jvnc.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByRoleCode(String roleCode);
}

