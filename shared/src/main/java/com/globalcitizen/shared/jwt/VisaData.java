package com.globalcitizen.shared.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Visa data model for JWT tokens
 * Contains all standard visa information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisaData {
    
    /**
     * Unique visa identifier
     */
    private String visaId;
    
    /**
     * Citizen identifier
     */
    private String citizenId;
    
    /**
     * Passport number
     */
    private String passportNumber;
    
    /**
     * Issuing country code (ISO 3166-1 alpha-2)
     */
    private String issuingCountry;
    
    /**
     * Destination country code (ISO 3166-1 alpha-2)
     */
    private String destinationCountry;
    
    /**
     * Visa type (tourist, business, student, work, etc.)
     */
    private String visaType;
    
    /**
     * Purpose of visit
     */
    private String purpose;
    
    /**
     * Date of issue (YYYY-MM-DD)
     */
    private String dateOfIssue;
    
    /**
     * Valid from date (YYYY-MM-DD)
     */
    private String validFrom;
    
    /**
     * Valid until date (YYYY-MM-DD)
     */
    private String validUntil;
    
    /**
     * Number of entries allowed
     */
    private Integer entries;
    
    /**
     * Issuing embassy/consulate
     */
    private String embassy;
    
    /**
     * Special conditions or restrictions
     */
    private String conditions;
    
    /**
     * Get visa identifier
     * @return Visa identifier
     */
    public String getVisaIdentifier() {
        return issuingCountry + visaId;
    }
    
    /**
     * Check if visa is currently valid
     * @return true if visa is valid
     */
    public boolean isValid() {
        try {
            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.LocalDate validFromDate = java.time.LocalDate.parse(validFrom);
            java.time.LocalDate validUntilDate = java.time.LocalDate.parse(validUntil);
            
            return !today.isBefore(validFromDate) && !today.isAfter(validUntilDate);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get remaining validity days
     * @return Number of days until expiry
     */
    public long getRemainingDays() {
        try {
            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.LocalDate validUntilDate = java.time.LocalDate.parse(validUntil);
            
            return java.time.temporal.ChronoUnit.DAYS.between(today, validUntilDate);
        } catch (Exception e) {
            return -1;
        }
    }
} 