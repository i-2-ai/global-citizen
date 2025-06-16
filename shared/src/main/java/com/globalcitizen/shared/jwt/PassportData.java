package com.globalcitizen.shared.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Passport data model for JWT tokens
 * Contains all standard passport information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PassportData {
    
    /**
     * Unique citizen identifier
     */
    private String citizenId;
    
    /**
     * Country code (ISO 3166-1 alpha-2)
     */
    private String countryCode;
    
    /**
     * Passport number
     */
    private String passportNumber;
    
    /**
     * First name
     */
    private String firstName;
    
    /**
     * Last name
     */
    private String lastName;
    
    /**
     * Date of birth (YYYY-MM-DD)
     */
    private String dateOfBirth;
    
    /**
     * Nationality
     */
    private String nationality;
    
    /**
     * Gender (M/F/X)
     */
    private String gender;
    
    /**
     * Place of birth
     */
    private String placeOfBirth;
    
    /**
     * Date of issue (YYYY-MM-DD)
     */
    private String dateOfIssue;
    
    /**
     * Issuing authority
     */
    private String authority;
    
    /**
     * Biometric data hash (fingerprint, face, etc.)
     */
    private String biometricHash;
    
    /**
     * Get full name
     * @return Full name
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    /**
     * Get passport identifier
     * @return Passport identifier
     */
    public String getPassportIdentifier() {
        return countryCode + passportNumber;
    }
} 