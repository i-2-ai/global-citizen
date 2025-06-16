package com.globalcitizen.centralauthority.repository;

import com.globalcitizen.centralauthority.model.MotherKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for MotherKey entity
 */
@Repository
public interface MotherKeyRepository extends JpaRepository<MotherKey, Long> {
    
    /**
     * Find mother key by status
     * @param status Key status
     * @return Optional mother key
     */
    Optional<MotherKey> findByStatus(MotherKey.KeyStatus status);
    
    /**
     * Find mother key by key ID
     * @param keyId Key identifier
     * @return Optional mother key
     */
    Optional<MotherKey> findByKeyId(String keyId);
    
    /**
     * Count mother keys by status
     * @param status Key status
     * @return Count of mother keys
     */
    long countByStatus(MotherKey.KeyStatus status);
    
    /**
     * Check if active mother key exists
     * @return true if active mother key exists
     */
    @Query("SELECT COUNT(m) > 0 FROM MotherKey m WHERE m.status = 'ACTIVE'")
    boolean existsActiveMotherKey();
    
    /**
     * Find mother key by key hash
     * @param keyHash Key hash
     * @return Optional mother key
     */
    Optional<MotherKey> findByKeyHash(String keyHash);
} 