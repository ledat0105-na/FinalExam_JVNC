package com.example.finalexam_jvnc.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "AccountProfiles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AccountProfile {
    @Id
    private Long accountId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "accountId")
    private Account account;

    @Column(nullable = false, length = 150)
    private String fullName;

    private String phoneNumber;
    private String addressLine;
    private String city;
    private String country;
    private LocalDate dateOfBirth;
}
