package com.globalcitizen.countryservice.service;

import com.globalcitizen.countryservice.model.CitizenPassport;
import com.globalcitizen.countryservice.repository.CitizenPassportRepository;
import com.globalcitizen.shared.jwt.JwtService;
import com.globalcitizen.shared.jwt.PassportData;
import com.globalcitizen.shared.qr.QRCodeResponse;
import com.globalcitizen.shared.qr.QRCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PassportService {
    private final CitizenPassportRepository passportRepository;
    private final JwtService jwtService;
    private final QRCodeService qrCodeService;

    @Transactional
    public CitizenPassport issuePassport(PassportData data, java.security.PrivateKey signingKey) throws Exception {
        // Generate JWT
        String jwt = jwtService.generatePassportJwt(data, signingKey);
        // Generate QR code
        QRCodeResponse qr = qrCodeService.createQRCodeResponse(jwt, "passport");
        // Save to DB
        CitizenPassport passport = CitizenPassport.builder()
                .citizenId(data.getCitizenId())
                .passportNumber(data.getPassportNumber())
                .countryCode(data.getCountryCode())
                .firstName(data.getFirstName())
                .lastName(data.getLastName())
                .dateOfBirth(data.getDateOfBirth())
                .nationality(data.getNationality())
                .gender(data.getGender())
                .placeOfBirth(data.getPlaceOfBirth())
                .dateOfIssue(data.getDateOfIssue())
                .authority(data.getAuthority())
                .biometricHash(data.getBiometricHash())
                .jwtToken(jwt)
                .qrCodeBase64(qr.getQrCodeBase64())
                .status(CitizenPassport.Status.ACTIVE)
                .build();
        return passportRepository.save(passport);
    }

    public Optional<CitizenPassport> getByCitizenId(String citizenId) {
        return passportRepository.findByCitizenId(citizenId);
    }

    public Optional<CitizenPassport> getByPassportNumber(String passportNumber) {
        return passportRepository.findByPassportNumber(passportNumber);
    }

    public List<CitizenPassport> getAllActive() {
        return passportRepository.findByStatus(CitizenPassport.Status.ACTIVE);
    }

    @Transactional
    public void revokePassport(String passportNumber) {
        passportRepository.findByPassportNumber(passportNumber).ifPresent(passport -> {
            passport.setStatus(CitizenPassport.Status.REVOKED);
            passportRepository.save(passport);
        });
    }
} 