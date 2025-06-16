package com.globalcitizen.centralauthority.repository;

import com.globalcitizen.centralauthority.model.GlobalKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for GlobalKey entity
 */
@Repository
public interface GlobalKeyRepository extends JpaRepository<GlobalKey, Long> {
    
    /**
     * Find global key by key number
     * @param keyNumber Global key number (1-7)
     * @return Optional global key
     */
    Optional<GlobalKey> findByKeyNumber(Integer keyNumber);
    
    /**
     * Find global key by key ID
     * @param keyId Key identifier
     * @return Optional global key
     */
    Optional<GlobalKey> findByKeyId(String keyId);
    
    /**
     * Find global keys by status
     * @param status Key status
     * @return List of global keys
     */
    List<GlobalKey> findByStatus(GlobalKey.KeyStatus status);
    
    /**
     * Find active global keys that can accept more countries
     * @param status Key status
     * @return List of available global keys
     */
    @Query("SELECT g FROM GlobalKey g WHERE g.status = :status AND g.activeCountriesCount < g.maxCountries")
    List<GlobalKey> findByStatusAndActiveCountriesCountLessThanMaxCountries(@Param("status") GlobalKey.KeyStatus status);
    
    /**
     * Count global keys by status
     * @param status Key status
     * @return Count of global keys
     */
    long countByStatus(GlobalKey.KeyStatus status);
    
    /**
     * Sum active countries count across all global keys
     * @return Total number of active countries
     */
    @Query("SELECT COALESCE(SUM(g.activeCountriesCount), 0) FROM GlobalKey g")
    long sumActiveCountriesCount();
    
    /**
     * Find global keys that are expiring soon
     * @param days Number of days to check
     * @return List of expiring global keys
     */
    @Query("SELECT g FROM GlobalKey g WHERE g.expiresAt <= :expiryDate AND g.status = 'ACTIVE'")
    List<GlobalKey> findExpiringKeys(@Param("expiryDate") java.time.Instant expiryDate);
    
    /**
     * Find global key by key hash
     * @param keyHash Key hash
     * @return Optional global key
     */
    Optional<GlobalKey> findByKeyHash(String keyHash);
    
    /**
     * Find global keys by version
     * @param version Key version
     * @return List of global keys
     */
    List<GlobalKey> findByVersion(Integer version);
    
    /**
     * Find global keys with highest active countries count
     * @param limit Maximum number of results
     * @return List of global keys
     */
    @Query("SELECT g FROM GlobalKey g WHERE g.status = 'ACTIVE' ORDER BY g.activeCountriesCount DESC")
    List<GlobalKey> findTopByActiveCountriesCountOrderByActiveCountriesCountDesc(int limit);
} 