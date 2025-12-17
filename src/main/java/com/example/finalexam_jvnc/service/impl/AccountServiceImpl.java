package com.example.finalexam_jvnc.service.impl;

import com.example.finalexam_jvnc.dto.AccountDTO;
import com.example.finalexam_jvnc.dto.RoleAssignmentDTO;
import com.example.finalexam_jvnc.model.Account;
import com.example.finalexam_jvnc.model.Role;
import com.example.finalexam_jvnc.repository.AccountRepository;
import com.example.finalexam_jvnc.repository.RoleRepository;
import com.example.finalexam_jvnc.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.finalexam_jvnc.repository.AccountProfileRepository;
import com.example.finalexam_jvnc.model.AccountProfile;
import com.example.finalexam_jvnc.dto.AccountProfileDTO;

@Service
@Transactional
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AccountProfileRepository accountProfileRepository;

    @Override
    public List<AccountDTO> getAllAccounts() {
        return accountRepository.findAllWithRoles().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AccountDTO getAccountById(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + accountId));
        return convertToDTO(account);
    }

    @Override
    public AccountDTO lockAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + accountId));
        account.setIsLocked(true);
        account = accountRepository.save(account);
        return convertToDTO(account);
    }

    @Override
    public AccountDTO unlockAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + accountId));
        account.setIsLocked(false);
        account = accountRepository.save(account);
        return convertToDTO(account);
    }

    @Override
    public AccountDTO assignRoles(RoleAssignmentDTO roleAssignmentDTO) {
        Account account = accountRepository.findById(roleAssignmentDTO.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + roleAssignmentDTO.getAccountId()));

        Set<Role> roles = new HashSet<>();
        for (String roleCode : roleAssignmentDTO.getRoleCodes()) {
            Role role = roleRepository.findByRoleCode(roleCode)
                    .orElseThrow(() -> new RuntimeException("Role not found: " + roleCode));
            roles.add(role);
        }

        account.setRoles(roles);
        account = accountRepository.save(account);
        return convertToDTO(account);
    }

    @Override
    public boolean validateAdminCredentials(String username, String password) {
        Account account = accountRepository.findByUsername(username)
                .orElse(null);
        
        if (account == null) {
            return false;
        }

        // Check if account is locked or inactive
        if (Boolean.TRUE.equals(account.getIsLocked()) || !Boolean.TRUE.equals(account.getIsActive())) {
            return false;
        }

        // Check if user has ADMIN role
        boolean isAdmin = account.getRoles().stream()
                .anyMatch(role -> "ADMIN".equals(role.getRoleCode()));

        if (!isAdmin) {
            return false;
        }

        // Validate password
        return passwordEncoder.matches(password, account.getPasswordHash());
    }

    @Override
    public boolean isAdmin(String username) {
        Account account = accountRepository.findByUsername(username)
                .orElse(null);
        
        if (account == null) {
            return false;
        }

        return account.getRoles().stream()
                .anyMatch(role -> "ADMIN".equals(role.getRoleCode()));
    }

    @Override
    public boolean validateStaffCredentials(String username, String password) {
        Account account = accountRepository.findByUsername(username)
                .orElse(null);
        
        if (account == null) {
            return false;
        }

        // Check if account is locked or inactive
        if (Boolean.TRUE.equals(account.getIsLocked()) || !Boolean.TRUE.equals(account.getIsActive())) {
            return false;
        }

        // Check if user has STAFF role
        boolean isStaff = account.getRoles().stream()
                .anyMatch(role -> "STAFF".equals(role.getRoleCode()));

        if (!isStaff) {
            return false;
        }

        // Validate password
        return passwordEncoder.matches(password, account.getPasswordHash());
    }

    @Override
    public boolean isStaff(String username) {
        Account account = accountRepository.findByUsername(username)
                .orElse(null);
        
        if (account == null) {
            return false;
        }

        return account.getRoles().stream()
                .anyMatch(role -> "STAFF".equals(role.getRoleCode()));
    }

    @Override
    public boolean validateCredentials(String username, String password) {
        Account account = accountRepository.findByUsername(username)
                .orElse(null);
        
        if (account == null) {
            return false;
        }

        // Check if account is locked or inactive
        if (Boolean.TRUE.equals(account.getIsLocked()) || !Boolean.TRUE.equals(account.getIsActive())) {
            return false;
        }

        // Validate password
        return passwordEncoder.matches(password, account.getPasswordHash());
    }

    @Override
    public String getUserRole(String username) {
        Account account = accountRepository.findByUsername(username)
                .orElse(null);
        
        if (account == null) {
            return null;
        }

        // Return the first role found (priority: ADMIN > STAFF > CUSTOMER)
        if (account.getRoles().stream().anyMatch(role -> "ADMIN".equals(role.getRoleCode()))) {
            return "ADMIN";
        }
        if (account.getRoles().stream().anyMatch(role -> "STAFF".equals(role.getRoleCode()))) {
            return "STAFF";
        }
        if (account.getRoles().stream().anyMatch(role -> "CUSTOMER".equals(role.getRoleCode()))) {
            return "CUSTOMER";
        }
        
        return null;
    }

    @Override
    @Transactional
    public AccountDTO registerCustomer(String username, String email, String password) {
        // Check if username already exists
        if (accountRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        // Check if email already exists
        if (accountRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Create new account
        Account account = new Account();
        account.setUsername(username);
        account.setEmail(email);
        account.setPasswordHash(passwordEncoder.encode(password));
        account.setIsActive(true);
        account.setIsLocked(false);

        // Assign CUSTOMER role
        Role customerRole = roleRepository.findByRoleCode("CUSTOMER")
                .orElseThrow(() -> new RuntimeException("CUSTOMER role not found"));
        account.setRoles(new HashSet<>(Set.of(customerRole)));

        Account savedAccount = accountRepository.save(account);
        return convertToDTO(savedAccount);
    }

    private AccountDTO convertToDTO(Account account) {
        Set<String> roleCodes = account.getRoles().stream()
                .map(Role::getRoleCode)
                .collect(Collectors.toSet());

        return AccountDTO.builder()
                .accountId(account.getAccountId())
                .username(account.getUsername())
                .email(account.getEmail())
                .isActive(account.getIsActive())
                .isLocked(account.getIsLocked())
                .lastLoginAt(account.getLastLoginAt())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .roleCodes(roleCodes)
                .build();
    }

    @Override
    public void updateLastLoginAt(String username) {
        Account account = accountRepository.findByUsername(username)
                .orElse(null);
        
        if (account != null) {
            account.setLastLoginAt(LocalDateTime.now());
            accountRepository.save(account);
        }
    }

    @Override
    public AccountProfileDTO getProfile(String username) {
        Account account = accountRepository.findByUsername(username).orElse(null);
        if (account == null) return null;

        AccountProfile profile = accountProfileRepository.findByAccount_Username(username).orElse(null);
        if (profile == null) {
            return AccountProfileDTO.builder()
                    .username(account.getUsername())
                    .email(account.getEmail())
                    .build();
        }

        return AccountProfileDTO.builder()
                .fullName(profile.getFullName())
                .phoneNumber(profile.getPhoneNumber())
                .addressLine(profile.getAddressLine())
                .city(profile.getCity())
                .country(profile.getCountry())
                .dateOfBirth(profile.getDateOfBirth())
                .username(account.getUsername())
                .email(account.getEmail())
                .build();
    }

    @Override
    @Transactional
    public void updateProfile(String username, AccountProfileDTO profileDTO) {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Account not found: " + username));

        AccountProfile profile = accountProfileRepository.findByAccount_Username(username).orElse(null);
        if (profile == null) {
            profile = AccountProfile.builder()
                    .account(account)
                    .fullName(profileDTO.getFullName() == null ? account.getUsername() : profileDTO.getFullName())
                    .build();
            profile.setAccount(account);
            profile.setAccountId(account.getAccountId());
        }

        profile.setFullName(profileDTO.getFullName());
        profile.setPhoneNumber(profileDTO.getPhoneNumber());
        profile.setAddressLine(profileDTO.getAddressLine());
        profile.setCity(profileDTO.getCity());
        profile.setCountry(profileDTO.getCountry());
        profile.setDateOfBirth(profileDTO.getDateOfBirth());

        accountProfileRepository.save(profile);
    }

    @Override
    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword) {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + username));

        if (!passwordEncoder.matches(currentPassword, account.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters");
        }

        account.setPasswordHash(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }

    @Override
    @Transactional
    public AccountDTO createAccount(String username, String email, String password, List<String> roleCodes) {
        // Check if username already exists
        if (accountRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        // Check if email already exists
        if (accountRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Validate password length
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }

        // Create new account
        Account account = new Account();
        account.setUsername(username);
        account.setEmail(email);
        account.setPasswordHash(passwordEncoder.encode(password));
        account.setIsActive(true);
        account.setIsLocked(false);

        // Assign roles
        Set<Role> roles = new HashSet<>();
        for (String roleCode : roleCodes) {
            Role role = roleRepository.findByRoleCode(roleCode)
                    .orElseThrow(() -> new RuntimeException("Role not found: " + roleCode));
            roles.add(role);
        }
        account.setRoles(roles);

        Account savedAccount = accountRepository.save(account);
        return convertToDTO(savedAccount);
    }

    @Override
    public Account getAccountByUsername(String username) {
        return accountRepository.findByUsername(username).orElse(null);
    }
}

