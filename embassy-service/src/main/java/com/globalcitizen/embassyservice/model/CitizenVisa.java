package com.globalcitizen.embassyservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Entity representing a digital visa issued to a citizen
 */
@Entity
@Table(name = "citizen_visas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CitizenVisa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "visa_id", nullable = false, unique = true)
    private String visaId;

    @Column(name = "citizen_id", nullable = false)
    private String citizenId;

    @Column(name = "passport_number", nullable = false)
    private String passportNumber;

    @Column(name = "issuing_country", nullable = false)
    private String issuingCountry;

    @Column(name = "destination_country", nullable = false)
    private String destinationCountry;

    @Column(name = "visa_type", nullable = false)
    private String visaType;

    @Column(name = "purpose", nullable = false)
    private String purpose;

    @Column(name = "date_of_issue", nullable = false)
    private String dateOfIssue;

    @Column(name = "valid_from", nullable = false)
    private String validFrom;

    @Column(name = "valid_until", nullable = false)
    private String validUntil;

    @Column(name = "entries", nullable = false)
    private Integer entries;

    @Column(name = "embassy", nullable = false)
    private String embassy;

    @Column(name = "conditions", columnDefinition = "TEXT")
    private String conditions;

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