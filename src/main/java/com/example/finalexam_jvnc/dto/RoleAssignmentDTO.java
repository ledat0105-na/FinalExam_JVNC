package com.example.finalexam_jvnc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleAssignmentDTO {
    private Long accountId;
    private Set<String> roleCodes; 
}

