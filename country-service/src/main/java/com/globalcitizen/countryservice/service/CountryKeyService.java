package com.globalcitizen.countryservice.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
@Slf4j
public class CountryKeyService {
    @Value("${country.private-key-path}")
    private String privateKeyPath;
    @Value("${country.public-key-path}")
    private String publicKeyPath;

    @Getter
    private PrivateKey privateKey;
    @Getter
    private PublicKey publicKey;
    @Getter
    private String publicKeyBase64;

    @PostConstruct
    public void loadKeys() {
        try {
            // Load private key
            byte[] privBytes = Files.readAllBytes(Paths.get(privateKeyPath));
            String privPem = new String(privBytes).replaceAll("-----.*-----", "").replaceAll("\s", "");
            byte[] privDecoded = Base64.getDecoder().decode(privPem);
            PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(privDecoded);
            privateKey = KeyFactory.getInstance("RSA").generatePrivate(privSpec);

            // Load public key
            byte[] pubBytes = Files.readAllBytes(Paths.get(publicKeyPath));
            String pubPem = new String(pubBytes).replaceAll("-----.*-----", "").replaceAll("\s", "");
            byte[] pubDecoded = Base64.getDecoder().decode(pubPem);
            X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(pubDecoded);
            publicKey = KeyFactory.getInstance("RSA").generatePublic(pubSpec);
            publicKeyBase64 = Base64.getEncoder().encodeToString(pubDecoded);

            log.info("Country keys loaded successfully");
        } catch (Exception e) {
            log.error("Failed to load country keys: {}", e.getMessage());
        }
    }
} 