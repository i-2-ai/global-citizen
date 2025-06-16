package com.globalcitizen.shared.crypto;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Quantum-resistant cryptography implementation using CRYSTALS-Kyber
 * This provides post-quantum security for the global passport system
 */
@Slf4j
@Component
public class QuantumResistantCrypto {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final String ALGORITHM = "Kyber";
    private static final String KEY_PAIR_ALGORITHM = "Kyber1024";
    private static final String SIGNATURE_ALGORITHM = "Dilithium5";
    private static final int KEY_SIZE = 1024;
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    /**
     * Generate a quantum-resistant key pair
     * @return KeyPair containing public and private keys
     */
    public KeyPair generateKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException {
        log.info("Generating quantum-resistant key pair using {}", KEY_PAIR_ALGORITHM);
        
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_PAIR_ALGORITHM, "BC");
        keyPairGenerator.initialize(KEY_SIZE, new SecureRandom());
        
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        log.info("Successfully generated quantum-resistant key pair");
        
        return keyPair;
    }

    /**
     * Sign data using quantum-resistant signature algorithm
     * @param data Data to sign
     * @param privateKey Private key for signing
     * @return Digital signature
     */
    public byte[] sign(byte[] data, PrivateKey privateKey) throws Exception {
        log.debug("Signing data with quantum-resistant signature algorithm");
        
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM, "BC");
        signature.initSign(privateKey);
        signature.update(data);
        
        byte[] signedData = signature.sign();
        log.debug("Data signed successfully");
        
        return signedData;
    }

    /**
     * Verify signature using quantum-resistant signature algorithm
     * @param data Original data
     * @param signature Digital signature
     * @param publicKey Public key for verification
     * @return true if signature is valid
     */
    public boolean verify(byte[] data, byte[] signature, PublicKey publicKey) throws Exception {
        log.debug("Verifying signature with quantum-resistant algorithm");
        
        Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM, "BC");
        sig.initVerify(publicKey);
        sig.update(data);
        
        boolean isValid = sig.verify(signature);
        log.debug("Signature verification result: {}", isValid);
        
        return isValid;
    }

    /**
     * Encrypt data using quantum-resistant encryption
     * @param data Data to encrypt
     * @param publicKey Public key for encryption
     * @return Encrypted data
     */
    public byte[] encrypt(byte[] data, PublicKey publicKey) throws Exception {
        log.debug("Encrypting data with quantum-resistant algorithm");
        
        Cipher cipher = Cipher.getInstance(ALGORITHM, "BC");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        
        byte[] encryptedData = cipher.doFinal(data);
        log.debug("Data encrypted successfully");
        
        return encryptedData;
    }

    /**
     * Decrypt data using quantum-resistant encryption
     * @param encryptedData Encrypted data
     * @param privateKey Private key for decryption
     * @return Decrypted data
     */
    public byte[] decrypt(byte[] encryptedData, PrivateKey privateKey) throws Exception {
        log.debug("Decrypting data with quantum-resistant algorithm");
        
        Cipher cipher = Cipher.getInstance(ALGORITHM, "BC");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        
        byte[] decryptedData = cipher.doFinal(encryptedData);
        log.debug("Data decrypted successfully");
        
        return decryptedData;
    }

    /**
     * Generate a symmetric key for AES encryption
     * @return SecretKey for AES operations
     */
    public SecretKey generateAESKey() throws NoSuchAlgorithmException {
        log.debug("Generating AES key for symmetric encryption");
        
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256);
        
        SecretKey secretKey = keyGenerator.generateKey();
        log.debug("AES key generated successfully");
        
        return secretKey;
    }

    /**
     * Encrypt data using AES-GCM for secure storage
     * @param data Data to encrypt
     * @param secretKey Secret key for encryption
     * @return Encrypted data with IV
     */
    public byte[] encryptAES(byte[] data, SecretKey secretKey) throws Exception {
        log.debug("Encrypting data with AES-GCM");
        
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        
        byte[] iv = cipher.getIV();
        byte[] encryptedData = cipher.doFinal(data);
        
        // Combine IV and encrypted data
        byte[] result = new byte[iv.length + encryptedData.length];
        System.arraycopy(iv, 0, result, 0, iv.length);
        System.arraycopy(encryptedData, 0, result, iv.length, encryptedData.length);
        
        log.debug("Data encrypted with AES-GCM successfully");
        return result;
    }

    /**
     * Decrypt data using AES-GCM
     * @param encryptedData Encrypted data with IV
     * @param secretKey Secret key for decryption
     * @return Decrypted data
     */
    public byte[] decryptAES(byte[] encryptedData, SecretKey secretKey) throws Exception {
        log.debug("Decrypting data with AES-GCM");
        
        // Extract IV and encrypted data
        byte[] iv = new byte[GCM_IV_LENGTH];
        byte[] data = new byte[encryptedData.length - GCM_IV_LENGTH];
        
        System.arraycopy(encryptedData, 0, iv, 0, GCM_IV_LENGTH);
        System.arraycopy(encryptedData, GCM_IV_LENGTH, data, 0, data.length);
        
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
        
        byte[] decryptedData = cipher.doFinal(data);
        log.debug("Data decrypted with AES-GCM successfully");
        
        return decryptedData;
    }

    /**
     * Convert key to Base64 string
     * @param key Key to convert
     * @return Base64 encoded string
     */
    public String keyToString(Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    /**
     * Convert Base64 string to PublicKey
     * @param keyString Base64 encoded public key
     * @return PublicKey object
     */
    public PublicKey stringToPublicKey(String keyString) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(keyString);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_PAIR_ALGORITHM, "BC");
        return keyFactory.generatePublic(keySpec);
    }

    /**
     * Convert Base64 string to PrivateKey
     * @param keyString Base64 encoded private key
     * @return PrivateKey object
     */
    public PrivateKey stringToPrivateKey(String keyString) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(keyString);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_PAIR_ALGORITHM, "BC");
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * Generate a secure random number
     * @return SecureRandom instance
     */
    public SecureRandom getSecureRandom() {
        return new SecureRandom();
    }
} 