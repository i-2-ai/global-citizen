package com.globalcitizen.embassyservice.controller;

import com.globalcitizen.embassyservice.service.EmbassyKeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/embassy-key")
@RequiredArgsConstructor
public class EmbassyKeyController {
    private final EmbassyKeyService embassyKeyService;

    @GetMapping("/public")
    public ResponseEntity<Map<String, String>> getPublicKey() {
        return ResponseEntity.ok(Map.of(
                "publicKeyBase64", embassyKeyService.getPublicKeyBase64()
        ));
    }
} 