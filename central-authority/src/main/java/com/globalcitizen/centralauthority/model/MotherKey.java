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
 * Mother Key entity - the root of trust for the entire system
 * This key is stored in the most secure location and used only for signing global keys
 */
@Entity
@Table(name = "mother_keys")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MotherKey {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Unique identifier for the mother key
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
     * Date when key expires (null for mother key as it should never expire)
     */
    @Column(name = "expires_at")
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
        return KeyStatus.ACTIVE.equals(status);
    }
    
    /**
     * Check if the key is expired
     * @return true if key is expired
     */
    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }
} 