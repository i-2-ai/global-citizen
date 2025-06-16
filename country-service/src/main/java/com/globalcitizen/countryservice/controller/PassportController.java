package com.globalcitizen.countryservice.controller;

import com.globalcitizen.countryservice.model.CitizenPassport;
import com.globalcitizen.countryservice.service.CountryKeyService;
import com.globalcitizen.countryservice.service.PassportService;
import com.globalcitizen.shared.jwt.PassportData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/passports")
@RequiredArgsConstructor
public class PassportController {
    private final PassportService passportService;
    private final CountryKeyService countryKeyService;

    @PostMapping("/issue")
    public ResponseEntity<CitizenPassport> issuePassport(@RequestBody PassportData data) throws Exception {
        if (countryKeyService.getPrivateKey() == null) {
            return ResponseEntity.status(500).body(null);
        }
        CitizenPassport passport = passportService.issuePassport(data, countryKeyService.getPrivateKey());
        return ResponseEntity.ok(passport);
    }

    @GetMapping("/citizen/{citizenId}")
    public ResponseEntity<CitizenPassport> getByCitizenId(@PathVariable String citizenId) {
        Optional<CitizenPassport> passport = passportService.getByCitizenId(citizenId);
        return passport.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/number/{passportNumber}")
    public ResponseEntity<CitizenPassport> getByPassportNumber(@PathVariable String passportNumber) {
        Optional<CitizenPassport> passport = passportService.getByPassportNumber(passportNumber);
        return passport.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/active")
    public ResponseEntity<List<CitizenPassport>> getAllActive() {
        return ResponseEntity.ok(passportService.getAllActive());
    }

    @PostMapping("/revoke/{passportNumber}")
    public ResponseEntity<Void> revokePassport(@PathVariable String passportNumber) {
        passportService.revokePassport(passportNumber);
        return ResponseEntity.ok().build();
    }
} 