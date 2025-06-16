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
 * Global Key entity - one of 7 intermediary keys signed by the mother key
 * Used for signing country root certificates
 */
@Entity
@Table(name = "global_keys")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalKey {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Global key number (1-7)
     */
    @Column(name = "key_number", unique = true, nullable = false)
    private Integer keyNumber;
    
    /**
     * Unique identifier for the global key
     */
    @Column(name = "key_id", unique = true, nullable = false)
    private String keyId;
    
    /**
     * Encrypted private key (AES-256-GCM encrypted)
     */
    @Column(name = "encrypted_private_key", columnDefinition = "TEXT", nullable = false)
    private String encryptedPrivateKey;
    
    /**
     * Public key (Base64 encoded)
     */
    @Column(name = "public_key", columnDefinition = "TEXT", nullable = false)
    private String publicKey;
    
    /**
     * Digital signature by mother key
     */
    @Column(name = "mother_key_signature", columnDefinition = "TEXT", nullable = false)
    private String motherKeySignature;
    
    /**
     * Key algorithm (e.g., "Kyber1024")
     */
    @Column(name = "algorithm", nullable = false)
    private String algorithm;
    
    /**
     * Key size in bits
     */
    @Column(name = "key_size", nullable = false)
    private Integer keySize;
    
    /**
     * Key status (ACTIVE, INACTIVE, COMPROMISED)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private KeyStatus status;
    
    /**
     * Date when key was created
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    /**
     * Date when key was last updated
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    /**
     * Date when key expires
     */
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
    
    /**
     * Version of the key
     */
    @Column(name = "version", nullable = false)
    private Integer version;
    
    /**
     * Description or notes about the key
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    /**
     * Hash of the key for integrity verification
     */
    @Column(name = "key_hash", nullable = false)
    private String keyHash;
    
    /**
     * Number of countries currently using this key
     */
    @Column(name = "active_countries_count", nullable = false)
    @Builder.Default
    private Integer activeCountriesCount = 0;
    
    /**
     * Maximum number of countries this key can serve
     */
    @Column(name = "max_countries", nullable = false)
    @Builder.Default
    private Integer maxCountries = 50;
    
    /**
     * Key status enumeration
     */
    public enum KeyStatus {
        ACTIVE,
        INACTIVE,
        COMPROMISED,
        EXPIRED
    }
    
    /**
     * Check if the key is active
     * @return true if key is active
     */
    public boolean isActive() {
        return KeyStatus.ACTIVE.equals(status) && !isExpired();
    }
    
    /**
     * Check if the key is expired
     * @return true if key is expired
     */
    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }
    
    /**
     * Check if the key can accept more countries
     * @return true if key can accept more countries
     */
    public boolean canAcceptCountries() {
        return isActive() && activeCountriesCount < maxCountries;
    }
    
    /**
     * Increment active countries count
     */
    public void incrementActiveCountries() {
        this.activeCountriesCount++;
    }
    
    /**
     * Decrement active countries count
     */
    public void decrementActiveCountries() {
        if (this.activeCountriesCount > 0) {
            this.activeCountriesCount--;
        }
    }
} 