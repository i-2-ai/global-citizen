package com.globalcitizen.countryservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Entity representing a digital passport issued to a citizen
 */
@Entity
@Table(name = "citizen_passports")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CitizenPassport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "citizen_id", nullable = false, unique = true)
    private String citizenId;

    @Column(name = "passport_number", nullable = false, unique = true)
    private String passportNumber;

    @Column(name = "country_code", nullable = false)
    private String countryCode;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "date_of_birth", nullable = false)
    private String dateOfBirth;

    @Column(name = "nationality", nullable = false)
    private String nationality;

    @Column(name = "gender", nullable = false)
    private String gender;

    @Column(name = "place_of_birth", nullable = false)
    private String placeOfBirth;

    @Column(name = "date_of_issue", nullable = false)
    private String dateOfIssue;

    @Column(name = "authority", nullable = false)
    private String authority;

    @Column(name = "biometric_hash", nullable = false)
    private String biometricHash;

    @Column(name = "jwt_token", columnDefinition = "TEXT", nullable = false)
    private String jwtToken;

    @Column(name = "qr_code_base64", columnDefinition = "TEXT", nullable = false)
    private String qrCodeBase64;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status {
        ACTIVE,
        REVOKED,
        EXPIRED
    }
} 