package com.globalcitizen.e2e;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Multi-Country Visa End-to-End Test
 * 
 * This test simulates multiple countries issuing visas to the same traveler:
 * 1. Multiple embassies issue visas to a single traveler
 * 2. Immigration verifies all visas
 * 3. Tests visa entry/exit tracking
 */
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MultiCountryVisaTest {
    
    private String passportJwt;
    private String usVisaJwt;
    private String ukVisaJwt;
    private String frVisaJwt;
    private String deVisaJwt;
    
    @BeforeAll
    void setUp() {
        log.info("Starting Multi-Country Visa End-to-End Test");
        
        // Wait for all services to be healthy
        TestUtils.waitForAllServices();
        
        log.info("All services are healthy, proceeding with test");
    }
    
    @Test
    void testMultiCountryVisaJourney() {
        log.info("=== MULTI-COUNTRY VISA TEST STARTED ===");
        
        // Step 1: Issue passport (assuming already done from previous test)
        issuePassport();
        
        // Step 2: Issue US visa
        issueUSVisa();
        
        // Step 3: Issue UK visa
        issueUKVisa();
        
        // Step 4: Issue French visa
        issueFrenchVisa();
        
        // Step 5: Issue German visa
        issueGermanVisa();
        
        // Step 6: Verify all visas at immigration
        verifyAllVisasAtImmigration();
        
        // Step 7: Test entry/exit tracking
        testEntryExitTracking();
        
        log.info("=== MULTI-COUNTRY VISA TEST COMPLETED SUCCESSFULLY ===");
    }
    
    private void issuePassport() {
        TestUtils.logTestStep("Issuing Passport for Multi-Country Travel");
        
        TestData.PassportRequest passportRequest = TestUtils.createTestPassportRequest(
                TestData.JANE_SMITH.getCitizenId(),
                TestData.JANE_SMITH.getPassportNumber()
        );
        
        ResponseEntity<Map> response = TestUtils.retryWithBackoff(() -> 
                TestUtils.CountryServiceClient.issuePassport(passportRequest)
        );
        
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        
        Map<String, Object> responseBody = response.getBody();
        passportJwt = (String) responseBody.get("jwtToken");
        
        assertThat(passportJwt).isNotNull().isNotEmpty();
        
        TestUtils.logAssertion("Passport issued successfully for multi-country travel");
    }
    
    private void issueUSVisa() {
        TestUtils.logTestStep("Issuing US Visa");
        
        TestData.VisaRequest visaRequest = TestData.VisaRequest.builder()
                .travelerId(TestData.JANE_SMITH.getCitizenId())
                .passportNumber(TestData.JANE_SMITH.getPassportNumber())
                .firstName(TestData.JANE_SMITH.getFirstName())
                .lastName(TestData.JANE_SMITH.getLastName())
                .nationality(TestData.JANE_SMITH.getNationality())
                .visaType(TestData.VISA_TYPE_BUSINESS)
                .purposeOfVisit(TestData.PURPOSE_BUSINESS)
                .dateOfIssue(TestData.TODAY)
                .dateOfExpiry(TestData.NEXT_MONTH)
                .durationOfStay(90)
                .numberOfEntries("Multiple Entry")
                .issuingEmbassy(TestData.US_EMBASSY_LONDON.getEmbassyName())
                .sponsorName(TestData.SPONSOR_TECHCORP)
                .sponsorContact(TestData.SPONSOR_CONTACT_TECHCORP)
                .build();
        
        ResponseEntity<Map> response = TestUtils.retryWithBackoff(() -> 
                TestUtils.EmbassyServiceClient.issueVisa(visaRequest)
        );
        
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        
        Map<String, Object> responseBody = response.getBody();
        usVisaJwt = (String) responseBody.get("jwtToken");
        
        assertThat(usVisaJwt).isNotNull().isNotEmpty();
        
        TestUtils.logAssertion("US Business visa issued successfully");
    }
    
    private void issueUKVisa() {
        TestUtils.logTestStep("Issuing UK Visa");
        
        TestData.VisaRequest visaRequest = TestData.VisaRequest.builder()
                .travelerId(TestData.JANE_SMITH.getCitizenId())
                .passportNumber(TestData.JANE_SMITH.getPassportNumber())
                .firstName(TestData.JANE_SMITH.getFirstName())
                .lastName(TestData.JANE_SMITH.getLastName())
                .nationality(TestData.JANE_SMITH.getNationality())
                .visaType(TestData.VISA_TYPE_TOURIST)
                .purposeOfVisit(TestData.PURPOSE_TOURISM)
                .dateOfIssue(TestData.TODAY)
                .dateOfExpiry(TestData.NEXT_MONTH)
                .durationOfStay(30)
                .numberOfEntries("Single Entry")
                .issuingEmbassy(TestData.UK_EMBASSY_WASHINGTON.getEmbassyName())
                .build();
        
        ResponseEntity<Map> response = TestUtils.retryWithBackoff(() -> 
                TestUtils.EmbassyServiceClient.issueVisa(visaRequest)
        );
        
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        
        Map<String, Object> responseBody = response.getBody();
        ukVisaJwt = (String) responseBody.get("jwtToken");
        
        assertThat(ukVisaJwt).isNotNull().isNotEmpty();
        
        TestUtils.logAssertion("UK Tourist visa issued successfully");
    }
    
    private void issueFrenchVisa() {
        TestUtils.logTestStep("Issuing French Visa");
        
        TestData.VisaRequest visaRequest = TestData.VisaRequest.builder()
                .travelerId(TestData.JANE_SMITH.getCitizenId())
                .passportNumber(TestData.JANE_SMITH.getPassportNumber())
                .firstName(TestData.JANE_SMITH.getFirstName())
                .lastName(TestData.JANE_SMITH.getLastName())
                .nationality(TestData.JANE_SMITH.getNationality())
                .visaType(TestData.VISA_TYPE_STUDENT)
                .purposeOfVisit(TestData.PURPOSE_STUDY)
                .dateOfIssue(TestData.TODAY)
                .dateOfExpiry(TestData.NEXT_YEAR)
                .durationOfStay(365)
                .numberOfEntries("Multiple Entry")
                .issuingEmbassy(TestData.FR_EMBASSY_NEW_YORK.getEmbassyName())
                .sponsorName(TestData.SPONSOR_UNIVERSITY)
                .sponsorContact(TestData.SPONSOR_CONTACT_UNIVERSITY)
                .build();
        
        ResponseEntity<Map> response = TestUtils.retryWithBackoff(() -> 
                TestUtils.EmbassyServiceClient.issueVisa(visaRequest)
        );
        
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        
        Map<String, Object> responseBody = response.getBody();
        frVisaJwt = (String) responseBody.get("jwtToken");
        
        assertThat(frVisaJwt).isNotNull().isNotEmpty();
        
        TestUtils.logAssertion("French Student visa issued successfully");
    }
    
    private void issueGermanVisa() {
        TestUtils.logTestStep("Issuing German Visa");
        
        TestData.VisaRequest visaRequest = TestData.VisaRequest.builder()
                .travelerId(TestData.JANE_SMITH.getCitizenId())
                .passportNumber(TestData.JANE_SMITH.getPassportNumber())
                .firstName(TestData.JANE_SMITH.getFirstName())
                .lastName(TestData.JANE_SMITH.getLastName())
                .nationality(TestData.JANE_SMITH.getNationality())
                .visaType(TestData.VISA_TYPE_WORK)
                .purposeOfVisit(TestData.PURPOSE_WORK)
                .dateOfIssue(TestData.TODAY)
                .dateOfExpiry(TestData.NEXT_YEAR)
                .durationOfStay(730)
                .numberOfEntries("Multiple Entry")
                .issuingEmbassy("German Embassy - London")
                .sponsorName("GermanTech GmbH")
                .sponsorContact("+49-30-12345678")
                .build();
        
        ResponseEntity<Map> response = TestUtils.retryWithBackoff(() -> 
                TestUtils.EmbassyServiceClient.issueVisa(visaRequest)
        );
        
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        
        Map<String, Object> responseBody = response.getBody();
        deVisaJwt = (String) responseBody.get("jwtToken");
        
        assertThat(deVisaJwt).isNotNull().isNotEmpty();
        
        TestUtils.logAssertion("German Work visa issued successfully");
    }
    
    private void verifyAllVisasAtImmigration() {
        TestUtils.logTestStep("Verifying All Visas at Immigration");
        
        List<String> visas = List.of(usVisaJwt, ukVisaJwt, frVisaJwt, deVisaJwt);
        List<String> countries = List.of("US", "UK", "France", "Germany");
        List<String> types = List.of("Business", "Tourist", "Student", "Work");
        
        for (int i = 0; i < visas.size(); i++) {
            String visaJwt = visas.get(i);
            String country = countries.get(i);
            String type = types.get(i);
            
            TestUtils.logTestStep("Verifying " + country + " " + type + " Visa");
            
            ResponseEntity<Map> response = TestUtils.retryWithBackoff(() -> 
                    TestUtils.ImmigrationServiceClient.verifyVisa(visaJwt)
            );
            
            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(response.getBody()).isNotNull();
            
            Map<String, Object> responseBody = response.getBody();
            assertThat(responseBody.get("valid")).isEqualTo(true);
            
            Map<String, Object> visaData = (Map<String, Object>) responseBody.get("visaData");
            assertThat(visaData.get("passportNumber")).isEqualTo(TestData.JANE_SMITH.getPassportNumber());
            assertThat(visaData.get("visaType")).isEqualTo(type);
            
            TestUtils.logAssertion(country + " " + type + " visa verified successfully");
        }
    }
    
    private void testEntryExitTracking() {
        TestUtils.logTestStep("Testing Entry/Exit Tracking");
        
        // Record entry to US
        recordEntry("US", "New York JFK Airport", TestData.PURPOSE_BUSINESS);
        
        // Record exit from US
        recordExit("US", "Los Angeles LAX Airport");
        
        // Record entry to UK
        recordEntry("UK", "London Heathrow Airport", TestData.PURPOSE_TOURISM);
        
        // Record exit from UK
        recordExit("UK", "London Gatwick Airport");
        
        // Record entry to France
        recordEntry("France", "Paris Charles de Gaulle Airport", TestData.PURPOSE_STUDY);
        
        // Record exit from France
        recordExit("France", "Paris Orly Airport");
        
        // Record entry to Germany
        recordEntry("Germany", "Berlin Brandenburg Airport", TestData.PURPOSE_WORK);
        
        TestUtils.logAssertion("Entry/exit tracking completed successfully");
    }
    
    private void recordEntry(String country, String entryPoint, String purpose) {
        TestUtils.logTestStep("Recording Entry to " + country);
        
        Map<String, Object> entryRequest = Map.of(
                "passportNumber", TestData.JANE_SMITH.getPassportNumber(),
                "visaNumber", "V" + TestData.JANE_SMITH.getCitizenId().replace("-", "") + "-" + country,
                "entryDate", TestData.TODAY.toString(),
                "entryPoint", entryPoint,
                "purpose", purpose,
                "durationOfStay", 30,
                "country", country
        );
        
        ResponseEntity<Map> response = TestUtils.retryWithBackoff(() -> 
                TestUtils.ImmigrationServiceClient.recordEntry(entryRequest)
        );
        
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody).containsKey("entryId");
        assertThat(responseBody).containsKey("status");
        
        TestUtils.logAssertion("Entry to " + country + " recorded successfully");
    }
    
    private void recordExit(String country, String exitPoint) {
        TestUtils.logTestStep("Recording Exit from " + country);
        
        Map<String, Object> exitRequest = Map.of(
                "passportNumber", TestData.JANE_SMITH.getPassportNumber(),
                "visaNumber", "V" + TestData.JANE_SMITH.getCitizenId().replace("-", "") + "-" + country,
                "exitDate", TestData.TODAY.plusDays(7).toString(),
                "exitPoint", exitPoint,
                "country", country
        );
        
        ResponseEntity<Map> response = TestUtils.retryWithBackoff(() -> 
                TestUtils.post(IMMIGRATION_SERVICE_URL + "/api/entry-log/exit", exitRequest, Map.class)
        );
        
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody).containsKey("exitId");
        assertThat(responseBody).containsKey("status");
        
        TestUtils.logAssertion("Exit from " + country + " recorded successfully");
    }
    
    @Test
    void testVisaValidationRules() {
        TestUtils.logTestStep("Testing Visa Validation Rules");
        
        // Test that passport is required for visa verification
        ResponseEntity<Map> response = TestUtils.retryWithBackoff(() -> 
                TestUtils.ImmigrationServiceClient.verifyVisa("invalid-jwt-token")
        );
        
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody.get("valid")).isEqualTo(false);
        assertThat(responseBody).containsKey("error");
        
        TestUtils.logAssertion("Invalid visa correctly rejected");
    }
    
    @Test
    void testMultipleVisaTypes() {
        TestUtils.logTestStep("Testing Multiple Visa Types for Same Traveler");
        
        // Verify that the same traveler can have multiple valid visas
        assertThat(usVisaJwt).isNotNull().isNotEmpty();
        assertThat(ukVisaJwt).isNotNull().isNotEmpty();
        assertThat(frVisaJwt).isNotNull().isNotEmpty();
        assertThat(deVisaJwt).isNotNull().isNotEmpty();
        
        // All visas should be for the same passport number
        List<String> visas = List.of(usVisaJwt, ukVisaJwt, frVisaJwt, deVisaJwt);
        
        for (String visaJwt : visas) {
            ResponseEntity<Map> response = TestUtils.retryWithBackoff(() -> 
                    TestUtils.ImmigrationServiceClient.verifyVisa(visaJwt)
            );
            
            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(response.getBody()).isNotNull();
            
            Map<String, Object> responseBody = response.getBody();
            assertThat(responseBody.get("valid")).isEqualTo(true);
            
            Map<String, Object> visaData = (Map<String, Object>) responseBody.get("visaData");
            assertThat(visaData.get("passportNumber")).isEqualTo(TestData.JANE_SMITH.getPassportNumber());
        }
        
        TestUtils.logAssertion("Multiple visa types validated successfully for same traveler");
    }
} 