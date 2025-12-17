package com.example.finalexam_jvnc.service.impl;

import com.example.finalexam_jvnc.dto.AccountDTO;
import com.example.finalexam_jvnc.dto.RoleAssignmentDTO;
import com.example.finalexam_jvnc.service.AccountService;
import com.example.finalexam_jvnc.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AccountService accountService;

    @Override
    public List<AccountDTO> getAllAccountsForAdmin() {
        return accountService.getAllAccounts();
    }

    @Override
    public AccountDTO getAccountDetails(Long accountId) {
        return accountService.getAccountById(accountId);
    }

    @Override
    public AccountDTO toggleAccountLock(Long accountId) {
        AccountDTO account = accountService.getAccountById(accountId);
        if (Boolean.TRUE.equals(account.getIsLocked())) {
            return accountService.unlockAccount(accountId);
        } else {
            return accountService.lockAccount(accountId);
        }
    }

    @Override
    public AccountDTO updateAccountRoles(Long accountId, List<String> roleCodes) {
        RoleAssignmentDTO roleAssignmentDTO = new RoleAssignmentDTO();
        roleAssignmentDTO.setAccountId(accountId);
        roleAssignmentDTO.setRoleCodes(Set.copyOf(roleCodes));
        return accountService.assignRoles(roleAssignmentDTO);
    }

    @Override
    public List<AccountDTO> getAuditLog() {
        // Return all accounts with audit information (createdAt, updatedAt, lastLoginAt)
        return accountService.getAllAccounts();
    }
}

