package com.globalcitizen.e2e;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Complete Travel Journey End-to-End Test
 * 
 * This test simulates the complete journey of a citizen from passport issuance to immigration verification:
 * 1. Central Authority creates mother key and global keys
 * 2. Country A issues a digital passport to a citizen
 * 3. Embassy B issues a visa to the same citizen
 * 4. Citizen travels to Country B
 * 5. Immigration verifies both passport and visa using QR codes
 */
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CompleteTravelJourneyTest {
    
    private String motherKeyId;
    private String globalKeyId;
    private String countryCertificateId;
    private String passportJwt;
    private String visaJwt;
    private String passportQrCode;
    private String visaQrCode;
    
    @BeforeAll
    void setUp() {
        log.info("Starting Complete Travel Journey End-to-End Test");
        
        // Wait for all services to be healthy
        TestUtils.waitForAllServices();
        
        log.info("All services are healthy, proceeding with test");
    }
    
    @Test
    void testCompleteTravelJourney() {
        log.info("=== COMPLETE TRAVEL JOURNEY TEST STARTED ===");
        
        // Step 1: Central Authority creates mother key
        createMotherKey();
        
        // Step 2: Central Authority creates global key
        createGlobalKey();
        
        // Step 3: Central Authority issues country certificate
        issueCountryCertificate();
        
        // Step 4: Country issues digital passport
        issueDigitalPassport();
        
        // Step 5: Embassy issues visa
        issueVisa();
        
        // Step 6: Immigration verifies passport
        verifyPassportAtImmigration();
        
        // Step 7: Immigration verifies visa
        verifyVisaAtImmigration();
        
        // Step 8: Immigration verifies QR codes
        verifyQrCodesAtImmigration();
        
        // Step 9: Record entry
        recordEntry();
        
        log.info("=== COMPLETE TRAVEL JOURNEY TEST COMPLETED SUCCESSFULLY ===");
    }
    
    private void createMotherKey() {
        TestUtils.logTestStep("Creating Mother Key");
        
        Map<String, Object> motherKeyRequest = Map.of(
                "keyId", TestData.SAMPLE_MOTHER_KEY_ID,
                "algorithm", "Dilithium5",
                "description", "Global Citizen Mother Key for 2024"
        );
        
        ResponseEntity<Map> response = TestUtils.retryWithBackoff(() -> 
                TestUtils.CentralAuthorityClient.createMotherKey(motherKeyRequest)
        );
        
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody).containsKey("keyId");
        assertThat(responseBody).containsKey("status");
        
        motherKeyId = (String) responseBody.get("keyId");
        assertThat(motherKeyId).isEqualTo(TestData.SAMPLE_MOTHER_KEY_ID);
        
        TestUtils.logAssertion("Mother key created successfully with ID: " + motherKeyId);
    }
    
    private void createGlobalKey() {
        TestUtils.logTestStep("Creating Global Key");
        
        Map<String, Object> globalKeyRequest = Map.of(
                "keyId", TestData.SAMPLE_GLOBAL_KEY_ID,
                "motherKeyId", motherKeyId,
                "algorithm", "Dilithium5",
                "description", "Global Key for signing country certificates",
                "region", "Global"
        );
        
        ResponseEntity<Map> response = TestUtils.retryWithBackoff(() -> 
                TestUtils.CentralAuthorityClient.createGlobalKey(globalKeyRequest)
        );
        
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody).containsKey("keyId");
        assertThat(responseBody).containsKey("status");
        assertThat(responseBody).containsKey("signedBy");
        
        globalKeyId = (String) responseBody.get("keyId");
        assertThat(globalKeyId).isEqualTo(TestData.SAMPLE_GLOBAL_KEY_ID);
        assertThat(responseBody.get("signedBy")).isEqualTo(motherKeyId);
        
        TestUtils.logAssertion("Global key created successfully with ID: " + globalKeyId);
    }
    
    private void issueCountryCertificate() {
        TestUtils.logTestStep("Issuing Country Certificate");
        
        Map<String, Object> certificateRequest = Map.of(
                "countryCode", TestData.UNITED_STATES.getCountryCode(),
                "countryName", TestData.UNITED_STATES.getCountryName(),
                "authority", TestData.UNITED_STATES.getAuthority(),
                "globalKeyId", globalKeyId,
                "validFrom", TestData.TODAY.toString(),
                "validUntil", TestData.NEXT_YEAR.toString()
        );
        
        ResponseEntity<Map> response = TestUtils.retryWithBackoff(() -> 
                TestUtils.CentralAuthorityClient.createCountryCertificate(certificateRequest)
        );
        
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody).containsKey("certificateId");
        assertThat(responseBody).containsKey("status");
        assertThat(responseBody).containsKey("signedBy");
        
        countryCertificateId = (String) responseBody.get("certificateId");
        assertThat(responseBody.get("signedBy")).isEqualTo(globalKeyId);
        
        TestUtils.logAssertion("Country certificate issued successfully with ID: " + countryCertificateId);
    }
    
    private void issueDigitalPassport() {
        TestUtils.logTestStep("Issuing Digital Passport");
        
        TestData.PassportRequest passportRequest = TestUtils.createTestPassportRequest(
                TestData.JOHN_DOE.getCitizenId(),
                TestData.JOHN_DOE.getPassportNumber()
        );
        
        ResponseEntity<Map> response = TestUtils.retryWithBackoff(() -> 
                TestUtils.CountryServiceClient.issuePassport(passportRequest)
        );
        
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody).containsKey("jwtToken");
        assertThat(responseBody).containsKey("qrCode");
        assertThat(responseBody).containsKey("status");
        
        passportJwt = (String) responseBody.get("jwtToken");
        passportQrCode = (String) responseBody.get("qrCode");
        
        assertThat(passportJwt).isNotNull().isNotEmpty();
        assertThat(passportQrCode).isNotNull().isNotEmpty();
        
        TestUtils.logAssertion("Digital passport issued successfully");
        TestUtils.logAssertion("Passport JWT: " + passportJwt.substring(0, 50) + "...");
        TestUtils.logAssertion("Passport QR Code: " + passportQrCode.substring(0, 50) + "...");
    }
    
    private void issueVisa() {
        TestUtils.logTestStep("Issuing Visa");
        
        // First verify the passport at the embassy
        ResponseEntity<Map> passportVerificationResponse = TestUtils.retryWithBackoff(() -> 
                TestUtils.EmbassyServiceClient.verifyPassport(passportJwt)
        );
        
        assertThat(passportVerificationResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(passportVerificationResponse.getBody()).isNotNull();
        
        Map<String, Object> verificationBody = passportVerificationResponse.getBody();
        assertThat(verificationBody).containsKey("valid");
        assertThat(verificationBody.get("valid")).isEqualTo(true);
        
        TestUtils.logAssertion("Passport verified successfully at embassy");
        
        // Now issue the visa
        TestData.VisaRequest visaRequest = TestUtils.createTestVisaRequest(
                TestData.JOHN_DOE.getCitizenId(),
                TestData.JOHN_DOE.getPassportNumber()
        );
        
        ResponseEntity<Map> response = TestUtils.retryWithBackoff(() -> 
                TestUtils.EmbassyServiceClient.issueVisa(visaRequest)
        );
        
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody).containsKey("jwtToken");
        assertThat(responseBody).containsKey("qrCode");
        assertThat(responseBody).containsKey("status");
        
        visaJwt = (String) responseBody.get("jwtToken");
        visaQrCode = (String) responseBody.get("qrCode");
        
        assertThat(visaJwt).isNotNull().isNotEmpty();
        assertThat(visaQrCode).isNotNull().isNotEmpty();
        
        TestUtils.logAssertion("Visa issued successfully");
        TestUtils.logAssertion("Visa JWT: " + visaJwt.substring(0, 50) + "...");
        TestUtils.logAssertion("Visa QR Code: " + visaQrCode.substring(0, 50) + "...");
    }
    
    private void verifyPassportAtImmigration() {
        TestUtils.logTestStep("Verifying Passport at Immigration");
        
        ResponseEntity<Map> response = TestUtils.retryWithBackoff(() -> 
                TestUtils.ImmigrationServiceClient.verifyPassport(passportJwt)
        );
        
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody).containsKey("valid");
        assertThat(responseBody).containsKey("passportData");
        
        assertThat(responseBody.get("valid")).isEqualTo(true);
        
        Map<String, Object> passportData = (Map<String, Object>) responseBody.get("passportData");
        assertThat(passportData).containsKey("citizenId");
        assertThat(passportData).containsKey("passportNumber");
        assertThat(passportData).containsKey("firstName");
        assertThat(passportData).containsKey("lastName");
        
        assertThat(passportData.get("citizenId")).isEqualTo(TestData.JOHN_DOE.getCitizenId());
        assertThat(passportData.get("passportNumber")).isEqualTo(TestData.JOHN_DOE.getPassportNumber());
        assertThat(passportData.get("firstName")).isEqualTo(TestData.JOHN_DOE.getFirstName());
        assertThat(passportData.get("lastName")).isEqualTo(TestData.JOHN_DOE.getLastName());
        
        TestUtils.logAssertion("Passport verified successfully at immigration");
    }
    
    private void verifyVisaAtImmigration() {
        TestUtils.logTestStep("Verifying Visa at Immigration");
        
        ResponseEntity<Map> response = TestUtils.retryWithBackoff(() -> 
                TestUtils.ImmigrationServiceClient.verifyVisa(visaJwt)
        );
        
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody).containsKey("valid");
        assertThat(responseBody).containsKey("visaData");
        
        assertThat(responseBody.get("valid")).isEqualTo(true);
        
        Map<String, Object> visaData = (Map<String, Object>) responseBody.get("visaData");
        assertThat(visaData).containsKey("travelerId");
        assertThat(visaData).containsKey("visaNumber");
        assertThat(visaData).containsKey("visaType");
        assertThat(visaData).containsKey("passportNumber");
        
        assertThat(visaData.get("passportNumber")).isEqualTo(TestData.JOHN_DOE.getPassportNumber());
        assertThat(visaData.get("visaType")).isEqualTo(TestData.VISA_TYPE_TOURIST);
        
        TestUtils.logAssertion("Visa verified successfully at immigration");
    }
    
    private void verifyQrCodesAtImmigration() {
        TestUtils.logTestStep("Verifying QR Codes at Immigration");
        
        // Verify passport QR code
        ResponseEntity<Map> passportQrResponse = TestUtils.retryWithBackoff(() -> 
                TestUtils.ImmigrationServiceClient.verifyQrCode(passportQrCode)
        );
        
        assertThat(passportQrResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(passportQrResponse.getBody()).isNotNull();
        
        Map<String, Object> passportQrBody = passportQrResponse.getBody();
        assertThat(passportQrBody).containsKey("valid");
        assertThat(passportQrBody).containsKey("documentType");
        assertThat(passportQrBody.get("valid")).isEqualTo(true);
        assertThat(passportQrBody.get("documentType")).isEqualTo("PASSPORT");
        
        TestUtils.logAssertion("Passport QR code verified successfully");
        
        // Verify visa QR code
        ResponseEntity<Map> visaQrResponse = TestUtils.retryWithBackoff(() -> 
                TestUtils.ImmigrationServiceClient.verifyQrCode(visaQrCode)
        );
        
        assertThat(visaQrResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(visaQrResponse.getBody()).isNotNull();
        
        Map<String, Object> visaQrBody = visaQrResponse.getBody();
        assertThat(visaQrBody).containsKey("valid");
        assertThat(visaQrBody).containsKey("documentType");
        assertThat(visaQrBody.get("valid")).isEqualTo(true);
        assertThat(visaQrBody.get("documentType")).isEqualTo("VISA");
        
        TestUtils.logAssertion("Visa QR code verified successfully");
    }
    
    private void recordEntry() {
        TestUtils.logTestStep("Recording Entry");
        
        Map<String, Object> entryRequest = Map.of(
                "passportNumber", TestData.JOHN_DOE.getPassportNumber(),
                "visaNumber", "V" + TestData.JOHN_DOE.getCitizenId().replace("-", ""),
                "entryDate", TestData.TODAY.toString(),
                "entryPoint", "London Heathrow Airport",
                "purpose", TestData.PURPOSE_TOURISM,
                "durationOfStay", 30
        );
        
        ResponseEntity<Map> response = TestUtils.retryWithBackoff(() -> 
                TestUtils.ImmigrationServiceClient.recordEntry(entryRequest)
        );
        
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody).containsKey("entryId");
        assertThat(responseBody).containsKey("status");
        
        TestUtils.logAssertion("Entry recorded successfully with ID: " + responseBody.get("entryId"));
    }
    
    @Test
    void testPublicKeyRegistry() {
        TestUtils.logTestStep("Testing Public Key Registry");
        
        ResponseEntity<Map> response = TestUtils.retryWithBackoff(() -> 
                TestUtils.ImmigrationServiceClient.getPublicKeyRegistry()
        );
        
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody).containsKey("countryKeys");
        assertThat(responseBody).containsKey("embassyKeys");
        
        TestUtils.logAssertion("Public key registry accessed successfully");
    }
    
    @Test
    void testServiceHealth() {
        TestUtils.logTestStep("Testing Service Health");
        
        // Test Central Authority health
        ResponseEntity<Map> centralAuthResponse = TestUtils.retryWithBackoff(() -> 
                TestUtils.get(CENTRAL_AUTHORITY_URL + "/actuator/health", Map.class)
        );
        assertThat(centralAuthResponse.getStatusCode().is2xxSuccessful()).isTrue();
        
        // Test Country Service health
        ResponseEntity<Map> countryResponse = TestUtils.retryWithBackoff(() -> 
                TestUtils.get(COUNTRY_SERVICE_URL + "/actuator/health", Map.class)
        );
        assertThat(countryResponse.getStatusCode().is2xxSuccessful()).isTrue();
        
        // Test Embassy Service health
        ResponseEntity<Map> embassyResponse = TestUtils.retryWithBackoff(() -> 
                TestUtils.get(EMBASSY_SERVICE_URL + "/actuator/health", Map.class)
        );
        assertThat(embassyResponse.getStatusCode().is2xxSuccessful()).isTrue();
        
        // Test Immigration Service health
        ResponseEntity<Map> immigrationResponse = TestUtils.retryWithBackoff(() -> 
                TestUtils.get(IMMIGRATION_SERVICE_URL + "/actuator/health", Map.class)
        );
        assertThat(immigrationResponse.getStatusCode().is2xxSuccessful()).isTrue();
        
        TestUtils.logAssertion("All services are healthy");
    }
} 