package com.globalcitizen.immigrationservice.controller;

import com.globalcitizen.shared.jwt.JwtService;
import com.globalcitizen.shared.jwt.PassportData;
import com.globalcitizen.shared.jwt.VisaData;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.PublicKey;
import java.util.Map;

import com.globalcitizen.immigrationservice.service.PublicKeyRegistryService;

@RestController
@RequestMapping("/api/validate")
@RequiredArgsConstructor
@Slf4j
public class ValidationController {
    private final JwtService jwtService;
    private final PublicKeyRegistryService publicKeyRegistryService;

    @PostMapping("/passport")
    public ResponseEntity<?> validatePassport(@RequestBody Map<String, String> req) {
        String jwt = req.get("jwt");
        String countryCode = req.get("countryCode");
        PublicKey pubKey = publicKeyRegistryService.getCountryKey(countryCode);
        if (pubKey == null) {
            return ResponseEntity.status(400).body(Map.of("error", "Unknown country public key"));
        }
        try {
            Claims claims = jwtService.validateAndParseJwt(jwt, pubKey);
            PassportData data = jwtService.extractPassportData(claims);
            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "passport", data,
                    "claims", claims
            ));
        } catch (Exception e) {
            log.warn("Passport validation failed: {}", e.getMessage());
            return ResponseEntity.ok(Map.of("valid", false, "error", e.getMessage()));
        }
    }

    @PostMapping("/visa")
    public ResponseEntity<?> validateVisa(@RequestBody Map<String, String> req) {
        String jwt = req.get("jwt");
        String embassy = req.get("embassy");
        PublicKey pubKey = publicKeyRegistryService.getEmbassyKey(embassy);
        if (pubKey == null) {
            return ResponseEntity.status(400).body(Map.of("error", "Unknown embassy public key"));
        }
        try {
            Claims claims = jwtService.validateAndParseJwt(jwt, pubKey);
            VisaData data = jwtService.extractVisaData(claims);
            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "visa", data,
                    "claims", claims
            ));
        } catch (Exception e) {
            log.warn("Visa validation failed: {}", e.getMessage());
            return ResponseEntity.ok(Map.of("valid", false, "error", e.getMessage()));
        }
    }
} 