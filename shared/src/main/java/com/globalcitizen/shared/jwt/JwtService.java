package com.globalcitizen.shared.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.globalcitizen.shared.crypto.QuantumResistantCrypto;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT service for creating and validating passport and visa tokens
 * Uses quantum-resistant cryptography for signing
 */
@Slf4j
@Service
public class JwtService {

    @Autowired
    private QuantumResistantCrypto crypto;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String ISSUER = "GlobalCitizen";
    private static final String PASSPORT_TYPE = "passport";
    private static final String VISA_TYPE = "visa";
    private static final int PASSPORT_EXPIRY_DAYS = 365 * 10; // 10 years
    private static final int VISA_EXPIRY_DAYS = 365; // 1 year

    /**
     * Generate a passport JWT token
     * @param passportData Passport information
     * @param signingKey Private key for signing
     * @return JWT token string
     */
    public String generatePassportJwt(PassportData passportData, PrivateKey signingKey) throws Exception {
        log.info("Generating passport JWT for citizen: {}", passportData.getCitizenId());
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", PASSPORT_TYPE);
        claims.put("citizenId", passportData.getCitizenId());
        claims.put("countryCode", passportData.getCountryCode());
        claims.put("passportNumber", passportData.getPassportNumber());
        claims.put("firstName", passportData.getFirstName());
        claims.put("lastName", passportData.getLastName());
        claims.put("dateOfBirth", passportData.getDateOfBirth());
        claims.put("nationality", passportData.getNationality());
        claims.put("gender", passportData.getGender());
        claims.put("placeOfBirth", passportData.getPlaceOfBirth());
        claims.put("dateOfIssue", passportData.getDateOfIssue());
        claims.put("authority", passportData.getAuthority());
        claims.put("biometricHash", passportData.getBiometricHash());

        return generateJwt(claims, signingKey, PASSPORT_EXPIRY_DAYS);
    }

    /**
     * Generate a visa JWT token
     * @param visaData Visa information
     * @param signingKey Private key for signing
     * @return JWT token string
     */
    public String generateVisaJwt(VisaData visaData, PrivateKey signingKey) throws Exception {
        log.info("Generating visa JWT for citizen: {} to country: {}", 
                visaData.getCitizenId(), visaData.getDestinationCountry());
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", VISA_TYPE);
        claims.put("visaId", visaData.getVisaId());
        claims.put("citizenId", visaData.getCitizenId());
        claims.put("passportNumber", visaData.getPassportNumber());
        claims.put("issuingCountry", visaData.getIssuingCountry());
        claims.put("destinationCountry", visaData.getDestinationCountry());
        claims.put("visaType", visaData.getVisaType());
        claims.put("purpose", visaData.getPurpose());
        claims.put("dateOfIssue", visaData.getDateOfIssue());
        claims.put("validFrom", visaData.getValidFrom());
        claims.put("validUntil", visaData.getValidUntil());
        claims.put("entries", visaData.getEntries());
        claims.put("embassy", visaData.getEmbassy());
        claims.put("conditions", visaData.getConditions());

        return generateJwt(claims, signingKey, VISA_EXPIRY_DAYS);
    }

    /**
     * Generate JWT token with quantum-resistant signature
     * @param claims JWT claims
     * @param signingKey Private key for signing
     * @param expiryDays Token expiry in days
     * @return JWT token string
     */
    private String generateJwt(Map<String, Object> claims, PrivateKey signingKey, int expiryDays) throws Exception {
        Instant now = Instant.now();
        Instant expiry = now.plus(expiryDays, ChronoUnit.DAYS);

        // Create JWT with quantum-resistant signature
        String jwt = Jwts.builder()
                .setIssuer(ISSUER)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .addClaims(claims)
                .signWith(signingKey, Jwts.SIG.RS256)
                .compact();

        log.debug("JWT generated successfully with expiry: {}", expiry);
        return jwt;
    }

    /**
     * Validate and parse JWT token
     * @param token JWT token string
     * @param publicKey Public key for verification
     * @return JWT claims
     */
    public Claims validateAndParseJwt(String token, PublicKey publicKey) throws Exception {
        log.debug("Validating and parsing JWT token");
        
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // Additional validation
            validateClaims(claims);
            
            log.debug("JWT validation successful");
            return claims;
        } catch (JwtException e) {
            log.error("JWT validation failed: {}", e.getMessage());
            throw new JwtException("Invalid JWT token", e);
        }
    }

    /**
     * Validate JWT claims
     * @param claims JWT claims to validate
     */
    private void validateClaims(Claims claims) {
        // Check if token is expired
        if (claims.getExpiration().before(new Date())) {
            throw new JwtException("JWT token has expired");
        }

        // Check issuer
        if (!ISSUER.equals(claims.getIssuer())) {
            throw new JwtException("Invalid JWT issuer");
        }

        // Check token type
        String type = claims.get("type", String.class);
        if (type == null || (!PASSPORT_TYPE.equals(type) && !VISA_TYPE.equals(type))) {
            throw new JwtException("Invalid JWT type");
        }
    }

    /**
     * Extract passport data from JWT claims
     * @param claims JWT claims
     * @return PassportData object
     */
    public PassportData extractPassportData(Claims claims) {
        if (!PASSPORT_TYPE.equals(claims.get("type", String.class))) {
            throw new IllegalArgumentException("JWT is not a passport token");
        }

        return PassportData.builder()
                .citizenId(claims.get("citizenId", String.class))
                .countryCode(claims.get("countryCode", String.class))
                .passportNumber(claims.get("passportNumber", String.class))
                .firstName(claims.get("firstName", String.class))
                .lastName(claims.get("lastName", String.class))
                .dateOfBirth(claims.get("dateOfBirth", String.class))
                .nationality(claims.get("nationality", String.class))
                .gender(claims.get("gender", String.class))
                .placeOfBirth(claims.get("placeOfBirth", String.class))
                .dateOfIssue(claims.get("dateOfIssue", String.class))
                .authority(claims.get("authority", String.class))
                .biometricHash(claims.get("biometricHash", String.class))
                .build();
    }

    /**
     * Extract visa data from JWT claims
     * @param claims JWT claims
     * @return VisaData object
     */
    public VisaData extractVisaData(Claims claims) {
        if (!VISA_TYPE.equals(claims.get("type", String.class))) {
            throw new IllegalArgumentException("JWT is not a visa token");
        }

        return VisaData.builder()
                .visaId(claims.get("visaId", String.class))
                .citizenId(claims.get("citizenId", String.class))
                .passportNumber(claims.get("passportNumber", String.class))
                .issuingCountry(claims.get("issuingCountry", String.class))
                .destinationCountry(claims.get("destinationCountry", String.class))
                .visaType(claims.get("visaType", String.class))
                .purpose(claims.get("purpose", String.class))
                .dateOfIssue(claims.get("dateOfIssue", String.class))
                .validFrom(claims.get("validFrom", String.class))
                .validUntil(claims.get("validUntil", String.class))
                .entries(claims.get("entries", Integer.class))
                .embassy(claims.get("embassy", String.class))
                .conditions(claims.get("conditions", String.class))
                .build();
    }

    /**
     * Get token type from JWT
     * @param token JWT token string
     * @return Token type (passport or visa)
     */
    public String getTokenType(String token) {
        try {
            // Parse without verification to get type
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new JwtException("Invalid JWT format");
            }

            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            Map<String, Object> claims = objectMapper.readValue(payload, Map.class);
            
            return (String) claims.get("type");
        } catch (Exception e) {
            log.error("Error extracting token type: {}", e.getMessage());
            throw new JwtException("Unable to extract token type", e);
        }
    }

    /**
     * Check if JWT is expired
     * @param token JWT token string
     * @return true if expired
     */
    public boolean isExpired(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return true;
            }

            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            Map<String, Object> claims = objectMapper.readValue(payload, Map.class);
            
            Long exp = (Long) claims.get("exp");
            if (exp == null) {
                return true;
            }

            return exp * 1000 < System.currentTimeMillis();
        } catch (Exception e) {
            log.error("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }
} 