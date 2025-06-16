package com.globalcitizen.e2e;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Test data constants for end-to-end tests
 */
public class TestData {
    
    // Citizen Information
    public static final Citizen JOHN_DOE = Citizen.builder()
            .citizenId("CIT-001-2024")
            .passportNumber("P123456789")
            .firstName("John")
            .lastName("Doe")
            .dateOfBirth(LocalDate.of(1990, 1, 1))
            .nationality("American")
            .countryCode("US")
            .build();
    
    public static final Citizen JANE_SMITH = Citizen.builder()
            .citizenId("CIT-002-2024")
            .passportNumber("P987654321")
            .firstName("Jane")
            .lastName("Smith")
            .dateOfBirth(LocalDate.of(1985, 5, 15))
            .nationality("British")
            .countryCode("UK")
            .build();
    
    public static final Citizen MARIA_GARCIA = Citizen.builder()
            .citizenId("CIT-003-2024")
            .passportNumber("P456789123")
            .firstName("Maria")
            .lastName("Garcia")
            .dateOfBirth(LocalDate.of(1992, 12, 20))
            .nationality("Spanish")
            .countryCode("ES")
            .build();
    
    // Country Information
    public static final Country UNITED_STATES = Country.builder()
            .countryCode("US")
            .countryName("United States")
            .authority("Department of State")
            .build();
    
    public static final Country UNITED_KINGDOM = Country.builder()
            .countryCode("UK")
            .countryName("United Kingdom")
            .authority("Home Office")
            .build();
    
    public static final Country CANADA = Country.builder()
            .countryCode("CA")
            .countryName("Canada")
            .authority("Immigration, Refugees and Citizenship Canada")
            .build();
    
    public static final Country FRANCE = Country.builder()
            .countryCode("FR")
            .countryName("France")
            .authority("Ministry of the Interior")
            .build();
    
    public static final Country GERMANY = Country.builder()
            .countryCode("DE")
            .countryName("Germany")
            .authority("Federal Foreign Office")
            .build();
    
    // Embassy Information
    public static final Embassy US_EMBASSY_LONDON = Embassy.builder()
            .embassyId("US-EMB-LON-2024-001")
            .embassyName("US Embassy - London")
            .embassyLocation("London, UK")
            .countryCode("US")
            .build();
    
    public static final Embassy US_EMBASSY_PARIS = Embassy.builder()
            .embassyId("US-EMB-PAR-2024-001")
            .embassyName("US Embassy - Paris")
            .embassyLocation("Paris, France")
            .countryCode("US")
            .build();
    
    public static final Embassy UK_EMBASSY_WASHINGTON = Embassy.builder()
            .embassyId("UK-EMB-WAS-2024-001")
            .embassyName("UK Embassy - Washington")
            .embassyLocation("Washington, DC, USA")
            .countryCode("UK")
            .build();
    
    public static final Embassy FR_EMBASSY_NEW_YORK = Embassy.builder()
            .embassyId("FR-EMB-NYC-2024-001")
            .embassyName("French Embassy - New York")
            .embassyLocation("New York, USA")
            .countryCode("FR")
            .build();
    
    // Visa Types
    public static final String VISA_TYPE_TOURIST = "Tourist";
    public static final String VISA_TYPE_BUSINESS = "Business";
    public static final String VISA_TYPE_STUDENT = "Student";
    public static final String VISA_TYPE_WORK = "Work";
    public static final String VISA_TYPE_TRANSIT = "Transit";
    
    // Test Keys (Sample data - in real implementation these would be generated)
    public static final String SAMPLE_MOTHER_KEY_ID = "MOTHER-KEY-2024-001";
    public static final String SAMPLE_GLOBAL_KEY_ID = "GLOBAL-KEY-2024-001";
    public static final String SAMPLE_COUNTRY_KEY_ID = "US-COUNTRY-KEY-2024-001";
    public static final String SAMPLE_EMBASSY_KEY_ID = "US-EMB-LON-2024-001";
    
    // Sample JWT Tokens (for testing)
    public static final String SAMPLE_PASSPORT_JWT = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjaXRpemVuSWQiOiJDSVQtMDAxLTIwMjQiLCJwYXNzcG9ydE51bWJlciI6IlAxMjM0NTY3ODkiLCJmaXJzdE5hbWUiOiJKb2huIiwibGFzdE5hbWUiOiJEb2UiLCJkYXRlT2ZCaXJ0aCI6IjE5OTAtMDEtMDEiLCJuYXRpb25hbGl0eSI6IkFtZXJpY2FuIiwiY291bnRyeUNvZGUiOiJVUyIsImRhdGVPZklzc3VlIjoiMjAyNC0wMS0xNSIsImF1dGhvcml0eSI6IkRlcGFydG1lbnQgb2YgU3RhdGUiLCJpYXQiOjE3MDUzNzI5NzQsImV4cCI6MTczNjkxMjk3NH0.example";
    
    public static final String SAMPLE_VISA_JWT = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0cmF2ZWxlcklkIjoiVFJBVi0wMDEtMjAyNCIsInZpc2FOdW1iZXIiOiJWMjM0NTY3ODkiLCJmaXJzdE5hbWUiOiJKb2huIiwibGFzdE5hbWUiOiJEb2UiLCJuYXRpb25hbGl0eSI6IkFtZXJpY2FuIiwicGFzc3BvcnROdW1iZXIiOiJQMTIzNDU2Nzg5IiwidmlzYVR5cGUiOiJUb3VyaXN0IiwicHVycG9zZU9mVmlzaXQiOiJUb3VyaXNtIGFuZCBzaWdodHNlZWluZyIsImRhdGVPZklzc3VlIjoiMjAyNC0wMS0xNSIsImRhdGVPZkV4cGlyeSI6IjIwMjQtMDItMTUiLCJkdXJhdGlvbk9mU3RheSI6MzAsIm51bWJlck9mRW50cmllcyI6IlNpbmdsZSBFbnRyeSIsInJlbWFpbmluZ0VudHJpZXMiOjEsImlzc3VpbmdFbWJhc3N5IjoiVVMgRW1iYXNzeSAtIExvbmRvbiIsImlhdCI6MTcwNTM3Mjk3NCwiZXhwIjoxNzA1NDU5Mzc0fQ.example";
    
    // Sample QR Code Data
    public static final String SAMPLE_PASSPORT_QR = "PASSPORT:eyJjaXRpemVuSWQiOiJDSVQtMDAxLTIwMjQiLCJwYXNzcG9ydE51bWJlciI6IlAxMjM0NTY3ODkiLCJmaXJzdE5hbWUiOiJKb2huIiwibGFzdE5hbWUiOiJEb2UiLCJkYXRlT2ZCaXJ0aCI6IjE5OTAtMDEtMDEiLCJuYXRpb25hbGl0eSI6IkFtZXJpY2FuIiwiY291bnRyeUNvZGUiOiJVUyIsImRhdGVPZklzc3VlIjoiMjAyNC0wMS0xNSIsImF1dGhvcml0eSI6IkRlcGFydG1lbnQgb2YgU3RhdGUiLCJpYXQiOjE3MDUzNzI5NzQsImV4cCI6MTczNjkxMjk3NH0";
    
    public static final String SAMPLE_VISA_QR = "VISA:eyJ0cmF2ZWxlcklkIjoiVFJBVi0wMDEtMjAyNCIsInZpc2FOdW1iZXIiOiJWMjM0NTY3ODkiLCJmaXJzdE5hbWUiOiJKb2huIiwibGFzdE5hbWUiOiJEb2UiLCJuYXRpb25hbGl0eSI6IkFtZXJpY2FuIiwicGFzc3BvcnROdW1iZXIiOiJQMTIzNDU2Nzg5IiwidmlzYVR5cGUiOiJUb3VyaXN0IiwicHVycG9zZU9mVmlzaXQiOiJUb3VyaXNtIGFuZCBzaWdodHNlZWluZyIsImRhdGVPZklzc3VlIjoiMjAyNC0wMS0xNSIsImRhdGVPZkV4cGlyeSI6IjIwMjQtMDItMTUiLCJkdXJhdGlvbk9mU3RheSI6MzAsIm51bWJlck9mRW50cmllcyI6IlNpbmdsZSBFbnRyeSIsInJlbWFpbmluZ0VudHJpZXMiOjEsImlzc3VpbmdFbWJhc3N5IjoiVVMgRW1iYXNzeSAtIExvbmRvbiIsImlhdCI6MTcwNTM3Mjk3NCwiZXhwIjoxNzA1NDU5Mzc0fQ";
    
    // Test Dates
    public static final LocalDate TODAY = LocalDate.now();
    public static final LocalDate TOMORROW = TODAY.plusDays(1);
    public static final LocalDate NEXT_WEEK = TODAY.plusWeeks(1);
    public static final LocalDate NEXT_MONTH = TODAY.plusMonths(1);
    public static final LocalDate NEXT_YEAR = TODAY.plusYears(1);
    public static final LocalDate EXPIRED_DATE = TODAY.minusDays(1);
    
    // Test Purposes
    public static final String PURPOSE_TOURISM = "Tourism and sightseeing";
    public static final String PURPOSE_BUSINESS = "Business meetings and conferences";
    public static final String PURPOSE_STUDY = "University studies";
    public static final String PURPOSE_WORK = "Employment";
    public static final String PURPOSE_TRANSIT = "Transit through country";
    
    // Test Sponsors
    public static final String SPONSOR_TECHCORP = "TechCorp Inc.";
    public static final String SPONSOR_UNIVERSITY = "University of California";
    public static final String SPONSOR_CONTACT_TECHCORP = "+1-555-0123";
    public static final String SPONSOR_CONTACT_UNIVERSITY = "+1-555-0456";
    
    // Test Remarks
    public static final String REMARKS_SPECIAL_CONSIDERATION = "Special considerations apply";
    public static final String REMARKS_MEDICAL = "Medical clearance required";
    public static final String REMARKS_SECURITY = "Additional security screening required";
    
    // Data Classes
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Citizen {
        private String citizenId;
        private String passportNumber;
        private String firstName;
        private String lastName;
        private LocalDate dateOfBirth;
        private String nationality;
        private String countryCode;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Country {
        private String countryCode;
        private String countryName;
        private String authority;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Embassy {
        private String embassyId;
        private String embassyName;
        private String embassyLocation;
        private String countryCode;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class VisaRequest {
        private String travelerId;
        private String passportNumber;
        private String firstName;
        private String lastName;
        private String nationality;
        private String visaType;
        private String purposeOfVisit;
        private LocalDate dateOfIssue;
        private LocalDate dateOfExpiry;
        private int durationOfStay;
        private String numberOfEntries;
        private String issuingEmbassy;
        private String sponsorName;
        private String sponsorContact;
        private String remarks;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PassportRequest {
        private String citizenId;
        private String passportNumber;
        private String firstName;
        private String lastName;
        private LocalDate dateOfBirth;
        private String nationality;
        private String countryCode;
        private LocalDate dateOfIssue;
        private LocalDate dateOfExpiry;
        private String authority;
    }
} 