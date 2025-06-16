package com.globalcitizen.centralauthority.service;

import com.globalcitizen.centralauthority.model.CountryCertificate;
import com.globalcitizen.centralauthority.model.GlobalKey;
import com.globalcitizen.centralauthority.repository.CountryCertificateRepository;
import com.globalcitizen.shared.crypto.QuantumResistantCrypto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing country certificates
 * Handles certificate requests and signing operations
 */
@Slf4j
@Service
@Transactional
public class CertificateService {

    @Autowired
    private CountryCertificateRepository certificateRepository;

    @Autowired
    private KeyManagementService keyManagementService;

    @Autowired
    private QuantumResistantCrypto crypto;

    private static final int CERTIFICATE_EXPIRY_YEARS = 3;

    /**
     * Request a signed certificate for a country
     * @param request Certificate request
     * @return Signed certificate
     */
    public CountryCertificate requestCertificate(CertificateRequest request) throws Exception {
        log.info("Processing certificate request for country: {}", request.getCountryCode());

        // Validate request
        validateCertificateRequest(request);

        // Check if country already has an active certificate
        Optional<CountryCertificate> existingCertificate = certificateRepository.findActiveByCountryCode(request.getCountryCode());
        if (existingCertificate.isPresent()) {
            throw new IllegalStateException("Country " + request.getCountryCode() + " already has an active certificate");
        }

        // Get available global key
        GlobalKey globalKey = keyManagementService.getAvailableGlobalKey();

        // Decrypt global key private key for signing
        byte[] encryptedPrivateKeyBytes = crypto.keyToString(globalKey.getEncryptedPrivateKey()).getBytes();
        SecretKey aesKey = crypto.generateAESKey(); // In production, this would be retrieved from secure storage
        byte[] privateKeyBytes = crypto.decryptAES(encryptedPrivateKeyBytes, aesKey);
        PrivateKey globalPrivateKey = crypto.stringToPrivateKey(crypto.keyToString(privateKeyBytes));

        // Create certificate data to sign
        String certificateData = createCertificateData(request, globalKey);
        byte[] signature = crypto.sign(certificateData.getBytes(), globalPrivateKey);

        // Create certificate entity
        String certificateId = "CERT-" + request.getCountryCode() + "-" + 
                             UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String serialNumber = generateSerialNumber();
        String certificateHash = crypto.keyToString(signature).substring(0, 32);
        Instant expiryDate = Instant.now().plus(CERTIFICATE_EXPIRY_YEARS, ChronoUnit.YEARS);

        CountryCertificate certificate = CountryCertificate.builder()
                .certificateId(certificateId)
                .countryCode(request.getCountryCode())
                .countryName(request.getCountryName())
                .countryPublicKey(request.getCountryPublicKey())
                .globalKeySignature(crypto.keyToString(signature))
                .globalKey(globalKey)
                .status(CountryCertificate.CertificateStatus.ACTIVE)
                .expiresAt(expiryDate)
                .version(1)
                .serialNumber(serialNumber)
                .certificateHash(certificateHash)
                .contactEmail(request.getContactEmail())
                .contactPhone(request.getContactPhone())
                .build();

        CountryCertificate savedCertificate = certificateRepository.save(certificate);

        // Increment active countries count for the global key
        globalKey.incrementActiveCountries();
        keyManagementService.saveGlobalKey(globalKey);

        log.info("Certificate issued successfully for country: {} with ID: {}", 
                request.getCountryCode(), savedCertificate.getCertificateId());

        return savedCertificate;
    }

    /**
     * Validate certificate request
     * @param request Certificate request
     */
    private void validateCertificateRequest(CertificateRequest request) {
        if (request.getCountryCode() == null || request.getCountryCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Country code is required");
        }
        if (request.getCountryName() == null || request.getCountryName().trim().isEmpty()) {
            throw new IllegalArgumentException("Country name is required");
        }
        if (request.getCountryPublicKey() == null || request.getCountryPublicKey().trim().isEmpty()) {
            throw new IllegalArgumentException("Country public key is required");
        }
        if (request.getContactEmail() == null || request.getContactEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Contact email is required");
        }

        // Validate country code format (ISO 3166-1 alpha-2)
        if (!request.getCountryCode().matches("^[A-Z]{2}$")) {
            throw new IllegalArgumentException("Invalid country code format. Must be 2 uppercase letters.");
        }

        // Validate email format
        if (!request.getContactEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    /**
     * Create certificate data for signing
     * @param request Certificate request
     * @param globalKey Global key
     * @return Certificate data string
     */
    private String createCertificateData(CertificateRequest request, GlobalKey globalKey) {
        return String.format("%s:%s:%s:%s:%s:%s",
                request.getCountryCode(),
                request.getCountryName(),
                request.getCountryPublicKey(),
                globalKey.getKeyId(),
                Instant.now().toString(),
                request.getContactEmail());
    }

    /**
     * Generate unique serial number
     * @return Serial number
     */
    private String generateSerialNumber() {
        return "SN-" + System.currentTimeMillis() + "-" + 
               UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    /**
     * Get certificate by country code
     * @param countryCode Country code
     * @return Certificate
     */
    public CountryCertificate getCertificateByCountryCode(String countryCode) {
        return certificateRepository.findActiveByCountryCode(countryCode)
                .orElseThrow(() -> new IllegalArgumentException("No active certificate found for country: " + countryCode));
    }

    /**
     * Get certificate by certificate ID
     * @param certificateId Certificate ID
     * @return Certificate
     */
    public CountryCertificate getCertificateById(String certificateId) {
        return certificateRepository.findByCertificateId(certificateId)
                .orElseThrow(() -> new IllegalArgumentException("Certificate not found: " + certificateId));
    }

    /**
     * Get all active certificates
     * @return List of active certificates
     */
    public List<CountryCertificate> getActiveCertificates() {
        return certificateRepository.findByStatus(CountryCertificate.CertificateStatus.ACTIVE);
    }

    /**
     * Revoke certificate
     * @param certificateId Certificate ID
     */
    public void revokeCertificate(String certificateId) {
        log.info("Revoking certificate: {}", certificateId);

        CountryCertificate certificate = getCertificateById(certificateId);
        certificate.revoke();
        certificateRepository.save(certificate);

        // Decrement active countries count for the global key
        GlobalKey globalKey = certificate.getGlobalKey();
        globalKey.decrementActiveCountries();
        keyManagementService.saveGlobalKey(globalKey);

        log.info("Certificate revoked successfully: {}", certificateId);
    }

    /**
     * Renew certificate
     * @param certificateId Certificate ID
     * @return Renewed certificate
     */
    public CountryCertificate renewCertificate(String certificateId) throws Exception {
        log.info("Renewing certificate: {}", certificateId);

        CountryCertificate existingCertificate = getCertificateById(certificateId);
        
        // Create renewal request
        CertificateRequest renewalRequest = CertificateRequest.builder()
                .countryCode(existingCertificate.getCountryCode())
                .countryName(existingCertificate.getCountryName())
                .countryPublicKey(existingCertificate.getCountryPublicKey())
                .contactEmail(existingCertificate.getContactEmail())
                .contactPhone(existingCertificate.getContactPhone())
                .build();

        // Revoke existing certificate
        revokeCertificate(certificateId);

        // Issue new certificate
        CountryCertificate newCertificate = requestCertificate(renewalRequest);
        newCertificate.setVersion(existingCertificate.getVersion() + 1);
        
        CountryCertificate savedCertificate = certificateRepository.save(newCertificate);
        log.info("Certificate renewed successfully: {}", savedCertificate.getCertificateId());

        return savedCertificate;
    }

    /**
     * Verify certificate signature
     * @param certificate Certificate to verify
     * @return true if signature is valid
     */
    public boolean verifyCertificateSignature(CountryCertificate certificate) throws Exception {
        log.debug("Verifying certificate signature for: {}", certificate.getCertificateId());

        // Get global key public key
        PublicKey globalPublicKey = crypto.stringToPublicKey(certificate.getGlobalKey().getPublicKey());

        // Recreate certificate data
        String certificateData = String.format("%s:%s:%s:%s:%s:%s",
                certificate.getCountryCode(),
                certificate.getCountryName(),
                certificate.getCountryPublicKey(),
                certificate.getGlobalKey().getKeyId(),
                certificate.getCreatedAt().toString(),
                certificate.getContactEmail());

        // Verify signature
        byte[] signature = crypto.keyToString(certificate.getGlobalKeySignature()).getBytes();
        boolean isValid = crypto.verify(certificateData.getBytes(), signature, globalPublicKey);

        log.debug("Certificate signature verification result: {}", isValid);
        return isValid;
    }

    /**
     * Get certificate statistics
     * @return CertificateStatistics object
     */
    public CertificateStatistics getCertificateStatistics() {
        long activeCertificates = certificateRepository.countActiveCertificates();
        long totalCertificates = certificateRepository.count();
        long expiringCertificates = certificateRepository.findExpiringCertificates(
                Instant.now().plus(30, ChronoUnit.DAYS)).size();

        return CertificateStatistics.builder()
                .activeCertificates(activeCertificates)
                .totalCertificates(totalCertificates)
                .expiringCertificates(expiringCertificates)
                .build();
    }

    /**
     * Certificate request data class
     */
    public static class CertificateRequest {
        private String countryCode;
        private String countryName;
        private String countryPublicKey;
        private String contactEmail;
        private String contactPhone;

        // Builder pattern
        public static CertificateRequestBuilder builder() {
            return new CertificateRequestBuilder();
        }

        public static class CertificateRequestBuilder {
            private CertificateRequest request = new CertificateRequest();

            public CertificateRequestBuilder countryCode(String countryCode) {
                request.countryCode = countryCode;
                return this;
            }

            public CertificateRequestBuilder countryName(String countryName) {
                request.countryName = countryName;
                return this;
            }

            public CertificateRequestBuilder countryPublicKey(String countryPublicKey) {
                request.countryPublicKey = countryPublicKey;
                return this;
            }

            public CertificateRequestBuilder contactEmail(String contactEmail) {
                request.contactEmail = contactEmail;
                return this;
            }

            public CertificateRequestBuilder contactPhone(String contactPhone) {
                request.contactPhone = contactPhone;
                return this;
            }

            public CertificateRequest build() {
                return request;
            }
        }

        // Getters
        public String getCountryCode() { return countryCode; }
        public String getCountryName() { return countryName; }
        public String getCountryPublicKey() { return countryPublicKey; }
        public String getContactEmail() { return contactEmail; }
        public String getContactPhone() { return contactPhone; }
    }

    /**
     * Certificate statistics data class
     */
    public static class CertificateStatistics {
        private long activeCertificates;
        private long totalCertificates;
        private long expiringCertificates;

        // Builder pattern
        public static CertificateStatisticsBuilder builder() {
            return new CertificateStatisticsBuilder();
        }

        public static class CertificateStatisticsBuilder {
            private CertificateStatistics statistics = new CertificateStatistics();

            public CertificateStatisticsBuilder activeCertificates(long count) {
                statistics.activeCertificates = count;
                return this;
            }

            public CertificateStatisticsBuilder totalCertificates(long count) {
                statistics.totalCertificates = count;
                return this;
            }

            public CertificateStatisticsBuilder expiringCertificates(long count) {
                statistics.expiringCertificates = count;
                return this;
            }

            public CertificateStatistics build() {
                return statistics;
            }
        }

        // Getters
        public long getActiveCertificates() { return activeCertificates; }
        public long getTotalCertificates() { return totalCertificates; }
        public long getExpiringCertificates() { return expiringCertificates; }
    }
} 