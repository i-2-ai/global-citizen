package com.globalcitizen.centralauthority.repository;

import com.globalcitizen.centralauthority.model.CountryCertificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for CountryCertificate entity
 */
@Repository
public interface CountryCertificateRepository extends JpaRepository<CountryCertificate, Long> {
    
    /**
     * Find certificate by certificate ID
     * @param certificateId Certificate identifier
     * @return Optional certificate
     */
    Optional<CountryCertificate> findByCertificateId(String certificateId);
    
    /**
     * Find certificate by country code
     * @param countryCode Country code (ISO 3166-1 alpha-2)
     * @return Optional certificate
     */
    Optional<CountryCertificate> findByCountryCode(String countryCode);
    
    /**
     * Find certificates by status
     * @param status Certificate status
     * @return List of certificates
     */
    List<CountryCertificate> findByStatus(CountryCertificate.CertificateStatus status);
    
    /**
     * Find active certificate by country code
     * @param countryCode Country code
     * @return Optional certificate
     */
    @Query("SELECT c FROM CountryCertificate c WHERE c.countryCode = :countryCode AND c.status = 'ACTIVE'")
    Optional<CountryCertificate> findActiveByCountryCode(@Param("countryCode") String countryCode);
    
    /**
     * Find certificates by global key
     * @param globalKeyId Global key ID
     * @return List of certificates
     */
    @Query("SELECT c FROM CountryCertificate c WHERE c.globalKey.id = :globalKeyId")
    List<CountryCertificate> findByGlobalKeyId(@Param("globalKeyId") Long globalKeyId);
    
    /**
     * Count certificates by status
     * @param status Certificate status
     * @return Count of certificates
     */
    long countByStatus(CountryCertificate.CertificateStatus status);
    
    /**
     * Find certificates that are expiring soon
     * @param expiryDate Expiry date
     * @return List of expiring certificates
     */
    @Query("SELECT c FROM CountryCertificate c WHERE c.expiresAt <= :expiryDate AND c.status = 'ACTIVE'")
    List<CountryCertificate> findExpiringCertificates(@Param("expiryDate") java.time.Instant expiryDate);
    
    /**
     * Find certificate by serial number
     * @param serialNumber Certificate serial number
     * @return Optional certificate
     */
    Optional<CountryCertificate> findBySerialNumber(String serialNumber);
    
    /**
     * Find certificate by certificate hash
     * @param certificateHash Certificate hash
     * @return Optional certificate
     */
    Optional<CountryCertificate> findByCertificateHash(String certificateHash);
    
    /**
     * Find certificates by version
     * @param version Certificate version
     * @return List of certificates
     */
    List<CountryCertificate> findByVersion(Integer version);
    
    /**
     * Find certificates by country name
     * @param countryName Country name
     * @return List of certificates
     */
    List<CountryCertificate> findByCountryNameContainingIgnoreCase(String countryName);
    
    /**
     * Count total active certificates
     * @return Count of active certificates
     */
    @Query("SELECT COUNT(c) FROM CountryCertificate c WHERE c.status = 'ACTIVE'")
    long countActiveCertificates();
    
    /**
     * Find certificates created in date range
     * @param startDate Start date
     * @param endDate End date
     * @return List of certificates
     */
    @Query("SELECT c FROM CountryCertificate c WHERE c.createdAt BETWEEN :startDate AND :endDate")
    List<CountryCertificate> findByCreatedAtBetween(@Param("startDate") java.time.Instant startDate, 
                                                   @Param("endDate") java.time.Instant endDate);
} 