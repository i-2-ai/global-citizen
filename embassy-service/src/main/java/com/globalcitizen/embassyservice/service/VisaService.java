package com.globalcitizen.embassyservice.service;

import com.globalcitizen.embassyservice.model.CitizenVisa;
import com.globalcitizen.embassyservice.repository.CitizenVisaRepository;
import com.globalcitizen.shared.jwt.JwtService;
import com.globalcitizen.shared.jwt.VisaData;
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
public class VisaService {
    private final CitizenVisaRepository visaRepository;
    private final JwtService jwtService;
    private final QRCodeService qrCodeService;

    @Transactional
    public CitizenVisa issueVisa(VisaData data, java.security.PrivateKey signingKey) throws Exception {
        // Generate JWT
        String jwt = jwtService.generateVisaJwt(data, signingKey);
        // Generate QR code
        QRCodeResponse qr = qrCodeService.createQRCodeResponse(jwt, "visa");
        // Save to DB
        CitizenVisa visa = CitizenVisa.builder()
                .visaId(data.getVisaId())
                .citizenId(data.getCitizenId())
                .passportNumber(data.getPassportNumber())
                .issuingCountry(data.getIssuingCountry())
                .destinationCountry(data.getDestinationCountry())
                .visaType(data.getVisaType())
                .purpose(data.getPurpose())
                .dateOfIssue(data.getDateOfIssue())
                .validFrom(data.getValidFrom())
                .validUntil(data.getValidUntil())
                .entries(data.getEntries())
                .embassy(data.getEmbassy())
                .conditions(data.getConditions())
                .jwtToken(jwt)
                .qrCodeBase64(qr.getQrCodeBase64())
                .status(CitizenVisa.Status.ACTIVE)
                .build();
        return visaRepository.save(visa);
    }

    public Optional<CitizenVisa> getByVisaId(String visaId) {
        return visaRepository.findByVisaId(visaId);
    }

    public List<CitizenVisa> getByCitizenId(String citizenId) {
        return visaRepository.findByCitizenId(citizenId);
    }

    public List<CitizenVisa> getByPassportNumber(String passportNumber) {
        return visaRepository.findByPassportNumber(passportNumber);
    }

    public List<CitizenVisa> getAllActive() {
        return visaRepository.findByStatus(CitizenVisa.Status.ACTIVE);
    }

    @Transactional
    public void revokeVisa(String visaId) {
        visaRepository.findByVisaId(visaId).ifPresent(visa -> {
            visa.setStatus(CitizenVisa.Status.REVOKED);
            visaRepository.save(visa);
        });
    }
} 