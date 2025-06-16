package com.globalcitizen.embassyservice.controller;

import com.globalcitizen.embassyservice.model.CitizenVisa;
import com.globalcitizen.embassyservice.service.EmbassyKeyService;
import com.globalcitizen.embassyservice.service.VisaService;
import com.globalcitizen.shared.jwt.VisaData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/visas")
@RequiredArgsConstructor
public class VisaController {
    private final VisaService visaService;
    private final EmbassyKeyService embassyKeyService;

    @PostMapping("/issue")
    public ResponseEntity<CitizenVisa> issueVisa(@RequestBody VisaData data) throws Exception {
        if (embassyKeyService.getPrivateKey() == null) {
            return ResponseEntity.status(500).body(null);
        }
        CitizenVisa visa = visaService.issueVisa(data, embassyKeyService.getPrivateKey());
        return ResponseEntity.ok(visa);
    }

    @GetMapping("/id/{visaId}")
    public ResponseEntity<CitizenVisa> getByVisaId(@PathVariable String visaId) {
        Optional<CitizenVisa> visa = visaService.getByVisaId(visaId);
        return visa.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/citizen/{citizenId}")
    public ResponseEntity<List<CitizenVisa>> getByCitizenId(@PathVariable String citizenId) {
        return ResponseEntity.ok(visaService.getByCitizenId(citizenId));
    }

    @GetMapping("/passport/{passportNumber}")
    public ResponseEntity<List<CitizenVisa>> getByPassportNumber(@PathVariable String passportNumber) {
        return ResponseEntity.ok(visaService.getByPassportNumber(passportNumber));
    }

    @GetMapping("/active")
    public ResponseEntity<List<CitizenVisa>> getAllActive() {
        return ResponseEntity.ok(visaService.getAllActive());
    }

    @PostMapping("/revoke/{visaId}")
    public ResponseEntity<Void> revokeVisa(@PathVariable String visaId) {
        visaService.revokeVisa(visaId);
        return ResponseEntity.ok().build();
    }
} 