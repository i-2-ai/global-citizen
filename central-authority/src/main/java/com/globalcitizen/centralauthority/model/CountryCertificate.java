package com.globalcitizen.centralauthority.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Country Certificate entity - signed by a global key
 * Contains country's public key and metadata
 */
@Entity
@Table(name = "country_certificates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CountryCertificate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Unique certificate identifier
     */
    @Column(name = "certificate_id", unique = true, nullable = false)
    private String certificateId;
    
    /**
     * Country code (ISO 3166-1 alpha-2)
     */
    @Column(name = "country_code", nullable = false)
    private String countryCode;
    
    /**
     * Country name
     */
    @Column(name = "country_name", nullable = false)
    private String countryName;
    
    /**
     * Country's public key (Base64 encoded)
     */
    @Column(name = "country_public_key", columnDefinition = "TEXT", nullable = false)
    private String countryPublicKey;
    
    /**
     * Digital signature by global key
     */
    @Column(name = "global_key_signature", columnDefinition = "TEXT", nullable = false)
    private String globalKeySignature;
    
    /**
     * Global key that signed this certificate
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "global_key_id", nullable = false)
    private GlobalKey globalKey;
    
    /**
     * Certificate status (ACTIVE, INACTIVE, REVOKED)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CertificateStatus status;
    
    /**
     * Date when certificate was created
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    /**
     * Date when certificate was last updated
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    /**
     * Date when certificate expires
     */
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
    
    /**
     * Version of the certificate
     */
    @Column(name = "version", nullable = false)
    private Integer version;
    
    /**
     * Certificate serial number
     */
    @Column(name = "serial_number", unique = true, nullable = false)
    private String serialNumber;
    
    /**
     * Certificate hash for integrity verification
     */
    @Column(name = "certificate_hash", nullable = false)
    private String certificateHash;
    
    /**
     * Contact information for the country
     */
    @Column(name = "contact_email")
    private String contactEmail;
    
    /**
     * Contact phone number
     */
    @Column(name = "contact_phone")
    private String contactPhone;
    
    /**
     * Certificate status enumeration
     */
    public enum CertificateStatus {
        ACTIVE,
        INACTIVE,
        REVOKED,
        EXPIRED
    }
    
    /**
     * Check if the certificate is active
     * @return true if certificate is active
     */
    public boolean isActive() {
        return CertificateStatus.ACTIVE.equals(status) && !isExpired();
    }
    
    /**
     * Check if the certificate is expired
     * @return true if certificate is expired
     */
    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }
    
    /**
     * Get days until expiry
     * @return Number of days until expiry
     */
    public long getDaysUntilExpiry() {
        if (expiresAt == null) {
            return -1;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(Instant.now(), expiresAt);
    }
    
    /**
     * Revoke the certificate
     */
    public void revoke() {
        this.status = CertificateStatus.REVOKED;
        this.updatedAt = Instant.now();
    }
} 