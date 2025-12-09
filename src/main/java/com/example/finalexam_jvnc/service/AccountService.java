package com.example.finalexam_jvnc.service;

import com.example.finalexam_jvnc.dto.AccountDTO;
import com.example.finalexam_jvnc.dto.RoleAssignmentDTO;

import java.util.List;

public interface AccountService {
    List<AccountDTO> getAllAccounts();
    AccountDTO getAccountById(Long accountId);
    AccountDTO lockAccount(Long accountId);
    AccountDTO unlockAccount(Long accountId);
    AccountDTO assignRoles(RoleAssignmentDTO roleAssignmentDTO);
    boolean validateAdminCredentials(String username, String password);
    boolean isAdmin(String username);
    boolean validateStaffCredentials(String username, String password);
    boolean isStaff(String username);
    boolean validateCredentials(String username, String password);
    String getUserRole(String username);
    AccountDTO registerCustomer(String username, String email, String password);
    void updateLastLoginAt(String username);
    AccountDTO createAccount(String username, String email, String password, List<String> roleCodes);
}

