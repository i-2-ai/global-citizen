package com.globalcitizen.countryservice.controller;

import com.globalcitizen.countryservice.service.CountryKeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/country-key")
@RequiredArgsConstructor
public class CountryKeyController {
    private final CountryKeyService countryKeyService;

    @GetMapping("/public")
    public ResponseEntity<Map<String, String>> getPublicKey() {
        return ResponseEntity.ok(Map.of(
                "publicKeyBase64", countryKeyService.getPublicKeyBase64()
        ));
    }
} 