package com.example.finalexam_jvnc.service;

import com.example.finalexam_jvnc.dto.AccountDTO;

import java.util.List;

public interface AdminService {
    List<AccountDTO> getAllAccountsForAdmin();
    AccountDTO getAccountDetails(Long accountId);
    AccountDTO toggleAccountLock(Long accountId);
    AccountDTO updateAccountRoles(Long accountId, List<String> roleCodes);
    List<AccountDTO> getAuditLog();
}

