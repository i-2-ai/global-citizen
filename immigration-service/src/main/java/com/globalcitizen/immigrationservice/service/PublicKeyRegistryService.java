package com.globalcitizen.immigrationservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class PublicKeyRegistryService {
    @Value("${registry.country-endpoints}")
    private Map<String, String> countryEndpoints; // countryCode -> endpoint
    @Value("${registry.embassy-endpoints}")
    private Map<String, String> embassyEndpoints; // embassyName -> endpoint

    private final Map<String, PublicKey> countryKeys = new ConcurrentHashMap<>();
    private final Map<String, PublicKey> embassyKeys = new ConcurrentHashMap<>();
    private final RestTemplate restTemplate = new RestTemplate();

    @PostConstruct
    public void loadAllKeys() {
        countryEndpoints.forEach((code, url) -> {
            try {
                String keyBase64 = fetchKey(url);
                countryKeys.put(code, decodeKey(keyBase64));
                log.info("Loaded country key for {}", code);
            } catch (Exception e) {
                log.warn("Failed to load country key for {}: {}", code, e.getMessage());
            }
        });
        embassyEndpoints.forEach((name, url) -> {
            try {
                String keyBase64 = fetchKey(url);
                embassyKeys.put(name, decodeKey(keyBase64));
                log.info("Loaded embassy key for {}", name);
            } catch (Exception e) {
                log.warn("Failed to load embassy key for {}: {}", name, e.getMessage());
            }
        });
    }

    public PublicKey getCountryKey(String countryCode) {
        return countryKeys.get(countryCode);
    }
    public PublicKey getEmbassyKey(String embassy) {
        return embassyKeys.get(embassy);
    }

    private String fetchKey(String url) {
        Map<?, ?> resp = restTemplate.getForObject(url, Map.class);
        return (String) resp.get("publicKeyBase64");
    }
    private PublicKey decodeKey(String base64) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(base64);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }
} 