package com.globalcitizen.centralauthority.service;

import com.globalcitizen.centralauthority.model.GlobalKey;
import com.globalcitizen.centralauthority.model.MotherKey;
import com.globalcitizen.centralauthority.repository.GlobalKeyRepository;
import com.globalcitizen.centralauthority.repository.MotherKeyRepository;
import com.globalcitizen.shared.crypto.QuantumResistantCrypto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing mother key and global keys
 * Handles the most critical cryptographic operations in the system
 */
@Slf4j
@Service
@Transactional
public class KeyManagementService {

    @Autowired
    private MotherKeyRepository motherKeyRepository;

    @Autowired
    private GlobalKeyRepository globalKeyRepository;

    @Autowired
    private QuantumResistantCrypto crypto;

    private static final int GLOBAL_KEY_EXPIRY_YEARS = 5;
    private static final int MAX_GLOBAL_KEYS = 7;

    /**
     * Generate and store the mother key
     * This is the most critical operation in the system
     * @return MotherKey entity
     */
    public MotherKey generateMotherKey() throws Exception {
        log.info("Generating mother key - the root of trust for the entire system");
        
        // Check if mother key already exists
        Optional<MotherKey> existingKey = motherKeyRepository.findByStatus(MotherKey.KeyStatus.ACTIVE);
        if (existingKey.isPresent()) {
            throw new IllegalStateException("Mother key already exists and is active");
        }

        // Generate quantum-resistant key pair
        KeyPair keyPair = crypto.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        // Generate AES key for encrypting private key
        SecretKey aesKey = crypto.generateAESKey();
        
        // Encrypt private key
        byte[] privateKeyBytes = privateKey.getEncoded();
        byte[] encryptedPrivateKey = crypto.encryptAES(privateKeyBytes, aesKey);

        // Create mother key entity
        String keyId = "MOTHER-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String keyHash = crypto.keyToString(publicKey).substring(0, 32);

        MotherKey motherKey = MotherKey.builder()
                .keyId(keyId)
                .encryptedPrivateKey(crypto.keyToString(encryptedPrivateKey))
                .publicKey(crypto.keyToString(publicKey))
                .algorithm(privateKey.getAlgorithm())
                .keySize(1024)
                .status(MotherKey.KeyStatus.ACTIVE)
                .version(1)
                .description("Mother key - root of trust for Global Citizen system")
                .keyHash(keyHash)
                .build();

        MotherKey savedKey = motherKeyRepository.save(motherKey);
        log.info("Mother key generated successfully with ID: {}", savedKey.getKeyId());
        
        return savedKey;
    }

    /**
     * Generate all 7 global keys signed by the mother key
     * @return List of generated global keys
     */
    public List<GlobalKey> generateGlobalKeys() throws Exception {
        log.info("Generating all 7 global keys signed by mother key");
        
        // Get active mother key
        MotherKey motherKey = motherKeyRepository.findByStatus(MotherKey.KeyStatus.ACTIVE)
                .orElseThrow(() -> new IllegalStateException("No active mother key found"));

        // Check if global keys already exist
        List<GlobalKey> existingKeys = globalKeyRepository.findByStatus(GlobalKey.KeyStatus.ACTIVE);
        if (!existingKeys.isEmpty()) {
            throw new IllegalStateException("Global keys already exist");
        }

        // Decrypt mother key private key (in production, this would be done in HSM)
        byte[] encryptedPrivateKeyBytes = crypto.keyToString(motherKey.getEncryptedPrivateKey()).getBytes();
        SecretKey aesKey = crypto.generateAESKey(); // In production, this would be retrieved from secure storage
        byte[] privateKeyBytes = crypto.decryptAES(encryptedPrivateKeyBytes, aesKey);
        PrivateKey motherPrivateKey = crypto.stringToPrivateKey(crypto.keyToString(privateKeyBytes));

        List<GlobalKey> globalKeys = new java.util.ArrayList<>();

        for (int i = 1; i <= MAX_GLOBAL_KEYS; i++) {
            GlobalKey globalKey = generateGlobalKey(i, motherPrivateKey);
            globalKeys.add(globalKey);
        }

        log.info("Successfully generated {} global keys", globalKeys.size());
        return globalKeys;
    }

    /**
     * Generate a single global key
     * @param keyNumber Global key number (1-7)
     * @param motherPrivateKey Mother key private key for signing
     * @return Generated global key
     */
    private GlobalKey generateGlobalKey(int keyNumber, PrivateKey motherPrivateKey) throws Exception {
        log.debug("Generating global key number: {}", keyNumber);

        // Generate quantum-resistant key pair for global key
        KeyPair keyPair = crypto.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        // Encrypt private key
        SecretKey aesKey = crypto.generateAESKey();
        byte[] privateKeyBytes = privateKey.getEncoded();
        byte[] encryptedPrivateKey = crypto.encryptAES(privateKeyBytes, aesKey);

        // Create data to sign (public key + metadata)
        String dataToSign = publicKey.getAlgorithm() + ":" + crypto.keyToString(publicKey) + ":" + keyNumber;
        byte[] signature = crypto.sign(dataToSign.getBytes(), motherPrivateKey);

        // Create global key entity
        String keyId = "GLOBAL-" + String.format("%02d", keyNumber) + "-" + 
                      UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String keyHash = crypto.keyToString(publicKey).substring(0, 32);
        Instant expiryDate = Instant.now().plus(GLOBAL_KEY_EXPIRY_YEARS, ChronoUnit.YEARS);

        GlobalKey globalKey = GlobalKey.builder()
                .keyNumber(keyNumber)
                .keyId(keyId)
                .encryptedPrivateKey(crypto.keyToString(encryptedPrivateKey))
                .publicKey(crypto.keyToString(publicKey))
                .motherKeySignature(crypto.keyToString(signature))
                .algorithm(privateKey.getAlgorithm())
                .keySize(1024)
                .status(GlobalKey.KeyStatus.ACTIVE)
                .expiresAt(expiryDate)
                .version(1)
                .description("Global key " + keyNumber + " for country certificate signing")
                .keyHash(keyHash)
                .activeCountriesCount(0)
                .maxCountries(50)
                .build();

        GlobalKey savedKey = globalKeyRepository.save(globalKey);
        log.debug("Global key {} generated successfully with ID: {}", keyNumber, savedKey.getKeyId());
        
        return savedKey;
    }

    /**
     * Get active mother key
     * @return Active mother key
     */
    public MotherKey getActiveMotherKey() {
        return motherKeyRepository.findByStatus(MotherKey.KeyStatus.ACTIVE)
                .orElseThrow(() -> new IllegalStateException("No active mother key found"));
    }

    /**
     * Get all active global keys
     * @return List of active global keys
     */
    public List<GlobalKey> getActiveGlobalKeys() {
        return globalKeyRepository.findByStatus(GlobalKey.KeyStatus.ACTIVE);
    }

    /**
     * Get global key by number
     * @param keyNumber Global key number (1-7)
     * @return Global key
     */
    public GlobalKey getGlobalKeyByNumber(int keyNumber) {
        return globalKeyRepository.findByKeyNumber(keyNumber)
                .orElseThrow(() -> new IllegalArgumentException("Global key " + keyNumber + " not found"));
    }

    /**
     * Get available global key for country assignment
     * @return Available global key
     */
    public GlobalKey getAvailableGlobalKey() {
        return globalKeyRepository.findByStatusAndActiveCountriesCountLessThanMaxCountries(GlobalKey.KeyStatus.ACTIVE)
                .stream()
                .filter(GlobalKey::canAcceptCountries)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No available global keys for country assignment"));
    }

    /**
     * Rotate global key (create new version)
     * @param keyNumber Global key number to rotate
     * @return New global key
     */
    public GlobalKey rotateGlobalKey(int keyNumber) throws Exception {
        log.info("Rotating global key number: {}", keyNumber);

        // Get existing key
        GlobalKey existingKey = getGlobalKeyByNumber(keyNumber);
        
        // Deactivate existing key
        existingKey.setStatus(GlobalKey.KeyStatus.INACTIVE);
        globalKeyRepository.save(existingKey);

        // Get mother key for signing
        MotherKey motherKey = getActiveMotherKey();
        byte[] encryptedPrivateKeyBytes = crypto.keyToString(motherKey.getEncryptedPrivateKey()).getBytes();
        SecretKey aesKey = crypto.generateAESKey();
        byte[] privateKeyBytes = crypto.decryptAES(encryptedPrivateKeyBytes, aesKey);
        PrivateKey motherPrivateKey = crypto.stringToPrivateKey(crypto.keyToString(privateKeyBytes));

        // Generate new key
        GlobalKey newKey = generateGlobalKey(keyNumber, motherPrivateKey);
        newKey.setVersion(existingKey.getVersion() + 1);
        
        GlobalKey savedKey = globalKeyRepository.save(newKey);
        log.info("Global key {} rotated successfully", keyNumber);
        
        return savedKey;
    }

    /**
     * Revoke global key
     * @param keyNumber Global key number to revoke
     */
    public void revokeGlobalKey(int keyNumber) {
        log.info("Revoking global key number: {}", keyNumber);
        
        GlobalKey globalKey = getGlobalKeyByNumber(keyNumber);
        globalKey.setStatus(GlobalKey.KeyStatus.COMPROMISED);
        globalKeyRepository.save(globalKey);
        
        log.info("Global key {} revoked successfully", keyNumber);
    }

    /**
     * Get key statistics
     * @return KeyStatistics object
     */
    public KeyStatistics getKeyStatistics() {
        long activeMotherKeys = motherKeyRepository.countByStatus(MotherKey.KeyStatus.ACTIVE);
        long activeGlobalKeys = globalKeyRepository.countByStatus(GlobalKey.KeyStatus.ACTIVE);
        long totalCountries = globalKeyRepository.sumActiveCountriesCount();
        
        return KeyStatistics.builder()
                .activeMotherKeys(activeMotherKeys)
                .activeGlobalKeys(activeGlobalKeys)
                .totalCountries(totalCountries)
                .build();
    }

    /**
     * Key statistics data class
     */
    public static class KeyStatistics {
        private long activeMotherKeys;
        private long activeGlobalKeys;
        private long totalCountries;

        // Builder pattern
        public static KeyStatisticsBuilder builder() {
            return new KeyStatisticsBuilder();
        }

        public static class KeyStatisticsBuilder {
            private KeyStatistics statistics = new KeyStatistics();

            public KeyStatisticsBuilder activeMotherKeys(long count) {
                statistics.activeMotherKeys = count;
                return this;
            }

            public KeyStatisticsBuilder activeGlobalKeys(long count) {
                statistics.activeGlobalKeys = count;
                return this;
            }

            public KeyStatisticsBuilder totalCountries(long count) {
                statistics.totalCountries = count;
                return this;
            }

            public KeyStatistics build() {
                return statistics;
            }
        }

        // Getters
        public long getActiveMotherKeys() { return activeMotherKeys; }
        public long getActiveGlobalKeys() { return activeGlobalKeys; }
        public long getTotalCountries() { return totalCountries; }
    }

    /**
     * Save or update a global key
     * @param globalKey GlobalKey entity
     * @return Saved global key
     */
    public GlobalKey saveGlobalKey(GlobalKey globalKey) {
        return globalKeyRepository.save(globalKey);
    }
} 