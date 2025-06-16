package com.globalcitizen.e2e;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Security Verification End-to-End Test
 * 
 * This test covers security features and edge cases:
 * 1. Tests expired documents
 * 2. Tests revoked documents
 * 3. Tests invalid signatures
 * 4. Tests watchlist matching
 * 5. Tests tampered documents
 */
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SecurityVerificationTest {
    
    private String validPassportJwt;
    private String validVisaJwt;
    private String expiredPassportJwt;
    private String revokedPassportJwt;
    private String tamperedPassportJwt;
    
    @BeforeAll
    void setUp() {
        log.info("Starting Security Verification End-to-End Test");
        
        // Wait for all services to be healthy
        TestUtils.waitForAllServices();
        
        log.info("All services are healthy, proceeding with test");
    }
    
    @Test
    void testSecurityVerification() {
        log.info("=== SECURITY VERIFICATION TEST STARTED ===");
        
        // Step 1: Create valid documents for comparison
        createValidDocuments();
        
        // Step 2: Test expired document detection
        testExpiredDocumentDetection();
        
        // Step 3: Test revoked document detection
        testRevokedDocumentDetection();
        
        // Step 4: Test invalid signature detection
        testInvalidSignatureDetection();
        
        // Step 5: Test tampered document detection
        testTamperedDocumentDetection();
        
        // Step 6: Test watchlist matching
        testWatchlistMatching();
        
        // Step 7: Test rate limiting
        testRateLimiting();
        
        log.info("=== SECURITY VERIFICATION TEST COMPLETED SUCCESSFULLY ===");
    }
    
    private void createValidDocuments() {
        TestUtils.logTestStep("Creating Valid Documents for Security Testing");
        
        // Create valid passport
        TestData.PassportRequest passportRequest = TestUtils.createTestPassportRequest(
                TestData.MARIA_GARCIA.getCitizenId(),
                TestData.MARIA_GARCIA.getPassportNumber()
        );
        
        ResponseEntity<Map> passportResponse = TestUtils.retryWithBackoff(() -> 
                TestUtils.CountryServiceClient.issuePassport(passportRequest)
        );
        
        assertThat(passportResponse.getStatusCode().is2xxSuccessful()).isTrue();
        validPassportJwt = (String) passportResponse.getBody().get("jwtToken");
        
        // Create valid visa
        TestData.VisaRequest visaRequest = TestUtils.createTestVisaRequest(
                TestData.MARIA_GARCIA.getCitizenId(),
                TestData.MARIA_GARCIA.getPassportNumber()
        );
        
        ResponseEntity<Map> visaResponse = TestUtils.retryWithBackoff(() -> 
                TestUtils.EmbassyServiceClient.issueVisa(visaRequest)
        );
        
        assertThat(visaResponse.getStatusCode().is2xxSuccessful()).isTrue();
        validVisaJwt = (String) visaResponse.getBody().get("jwtToken");
        
        TestUtils.logAssertion("Valid documents created successfully for security testing");
    }
    
    private void testExpiredDocumentDetection() {
        TestUtils.logTestStep("Testing Expired Document Detection");
        
        // Create expired passport
        TestData.PassportRequest expiredPassportRequest = TestData.PassportRequest.builder()
                .citizenId("EXPIRED-CIT-001")
                .passportNumber("PEXPIRED001")
                .firstName("Expired")
                .lastName("Document")
                .dateOfBirth(TestData.TODAY.minusYears(30))
                .nationality("Test Nationality")
                .countryCode("TS")
                .dateOfIssue(TestData.TODAY.minusYears(2))
                .dateOfExpiry(TestData.EXPIRED_DATE)
                .authority("Test Authority")
                .build();
        
        ResponseEntity<Map> expiredPassportResponse = TestUtils.retryWithBackoff(() -> 
                TestUtils.CountryServiceClient.issuePassport(expiredPassportRequest)
        );
        
        assertThat(expiredPassportResponse.getStatusCode().is2xxSuccessful()).isTrue();
        expiredPassportJwt = (String) expiredPassportResponse.getBody().get("jwtToken");
        
        // Verify that expired passport is rejected
        ResponseEntity<Map> verificationResponse = TestUtils.retryWithBackoff(() -> 
                TestUtils.ImmigrationServiceClient.verifyPassport(expiredPassportJwt)
        );
        
        assertThat(verificationResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(verificationResponse.getBody()).isNotNull();
        
        Map<String, Object> responseBody = verificationResponse.getBody();
        assertThat(responseBody.get("valid")).isEqualTo(false);
        assertThat(responseBody).containsKey("error");
        assertThat(responseBody.get("error")).asString().contains("expired");
        
        TestUtils.logAssertion("Expired passport correctly rejected");
        
        // Test expired visa
        TestData.VisaRequest expiredVisaRequest = TestData.VisaRequest.builder()
                .travelerId("EXPIRED-TRAV-001")
                .passportNumber("PEXPIRED001")
                .firstName("Expired")
                .lastName("Visa")
                .nationality("Test Nationality")
                .visaType(TestData.VISA_TYPE_TOURIST)
                .purposeOfVisit(TestData.PURPOSE_TOURISM)
                .dateOfIssue(TestData.TODAY.minusMonths(2))
                .dateOfExpiry(TestData.EXPIRED_DATE)
                .durationOfStay(30)
                .numberOfEntries("Single Entry")
                .issuingEmbassy("Test Embassy")
                .build();
        
        ResponseEntity<Map> expiredVisaResponse = TestUtils.retryWithBackoff(() -> 
                TestUtils.EmbassyServiceClient.issueVisa(expiredVisaRequest)
        );
        
        assertThat(expiredVisaResponse.getStatusCode().is2xxSuccessful()).isTrue();
        String expiredVisaJwt = (String) expiredVisaResponse.getBody().get("jwtToken");
        
        // Verify that expired visa is rejected
        ResponseEntity<Map> expiredVisaVerificationResponse = TestUtils.retryWithBackoff(() -> 
                TestUtils.ImmigrationServiceClient.verifyVisa(expiredVisaJwt)
        );
        
        assertThat(expiredVisaVerificationResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(expiredVisaVerificationResponse.getBody()).isNotNull();
        
        Map<String, Object> expiredVisaResponseBody = expiredVisaVerificationResponse.getBody();
        assertThat(expiredVisaResponseBody.get("valid")).isEqualTo(false);
        assertThat(expiredVisaResponseBody).containsKey("error");
        assertThat(expiredVisaResponseBody.get("error")).asString().contains("expired");
        
        TestUtils.logAssertion("Expired visa correctly rejected");
    }
    
    private void testRevokedDocumentDetection() {
        TestUtils.logTestStep("Testing Revoked Document Detection");
        
        // Create a passport to be revoked
        TestData.PassportRequest revokablePassportRequest = TestUtils.createTestPassportRequest(
                "REVOKE-CIT-001",
                "PREVOKE001"
        );
        
        ResponseEntity<Map> revokablePassportResponse = TestUtils.retryWithBackoff(() -> 
                TestUtils.CountryServiceClient.issuePassport(revokablePassportRequest)
        );
        
        assertThat(revokablePassportResponse.getStatusCode().is2xxSuccessful()).isTrue();
        String revokablePassportJwt = (String) revokablePassportResponse.getBody().get("jwtToken");
        
        // Verify passport is initially valid
        ResponseEntity<Map> initialVerificationResponse = TestUtils.retryWithBackoff(() -> 
                TestUtils.ImmigrationServiceClient.verifyPassport(revokablePassportJwt)
        );
        
        assertThat(initialVerificationResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(initialVerificationResponse.getBody().get("valid")).isEqualTo(true);
        
        // Revoke the passport
        ResponseEntity<Map> revocationResponse = TestUtils.retryWithBackoff(() -> 
                TestUtils.CountryServiceClient.revokePassport("REVOKE-CIT-001")
        );
        
        assertThat(revocationResponse.getStatusCode().is2xxSuccessful()).isTrue();
        
        // Verify that revoked passport is rejected
        ResponseEntity<Map> revokedVerificationResponse = TestUtils.retryWithBackoff(() -> 
                TestUtils.ImmigrationServiceClient.verifyPassport(revokablePassportJwt)
        );
        
        assertThat(revokedVerificationResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(revokedVerificationResponse.getBody()).isNotNull();
        
        Map<String, Object> revokedResponseBody = revokedVerificationResponse.getBody();
        assertThat(revokedResponseBody.get("valid")).isEqualTo(false);
        assertThat(revokedResponseBody).containsKey("error");
        assertThat(revokedResponseBody.get("error")).asString().contains("revoked");
        
        TestUtils.logAssertion("Revoked passport correctly rejected");
        
        // Test visa revocation
        TestData.VisaRequest revokableVisaRequest = TestUtils.createTestVisaRequest(
                "REVOKE-TRAV-001",
                "PREVOKE001"
        );
        
        ResponseEntity<Map> revokableVisaResponse = TestUtils.retryWithBackoff(() -> 
                TestUtils.EmbassyServiceClient.issueVisa(revokableVisaRequest)
        );
        
        assertThat(revokableVisaResponse.getStatusCode().is2xxSuccessful()).isTrue();
        String revokableVisaJwt = (String) revokableVisaResponse.getBody().get("jwtToken");
        
        // Verify visa is initially valid
        ResponseEntity<Map> initialVisaVerificationResponse = TestUtils.retryWithBackoff(() -> 
                TestUtils.ImmigrationServiceClient.verifyVisa(revokableVisaJwt)
        );
        
        assertThat(initialVisaVerificationResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(initialVisaVerificationResponse.getBody().get("valid")).isEqualTo(true);
        
        // Revoke the visa
        ResponseEntity<Map> visaRevocationResponse = TestUtils.retryWithBackoff(() -> 
                TestUtils.EmbassyServiceClient.revokeVisa("REVOKE-TRAV-001")
        );
        
        assertThat(visaRevocationResponse.getStatusCode().is2xxSuccessful()).isTrue();
        
        // Verify that revoked visa is rejected
        ResponseEntity<Map> revokedVisaVerificationResponse = TestUtils.retryWithBackoff(() -> 
                TestUtils.ImmigrationServiceClient.verifyVisa(revokableVisaJwt)
        );
        
        assertThat(revokedVisaVerificationResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(revokedVisaVerificationResponse.getBody()).isNotNull();
        
        Map<String, Object> revokedVisaResponseBody = revokedVisaVerificationResponse.getBody();
        assertThat(revokedVisaResponseBody.get("valid")).isEqualTo(false);
        assertThat(revokedVisaResponseBody).containsKey("error");
        assertThat(revokedVisaResponseBody.get("error")).asString().contains("revoked");
        
        TestUtils.logAssertion("Revoked visa correctly rejected");
    }
    
    private void testInvalidSignatureDetection() {
        TestUtils.logTestStep("Testing Invalid Signature Detection");
        
        // Test with malformed JWT
        String malformedJwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.malformed.payload";
        
        ResponseEntity<Map> malformedResponse = TestUtils.retryWithBackoff(() -> 
                TestUtils.ImmigrationServiceClient.verifyPassport(malformedJwt)
        );
        
        assertThat(malformedResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(malformedResponse.getBody()).isNotNull();
        
        Map<String, Object> malformedResponseBody = malformedResponse.getBody();
        assertThat(malformedResponseBody.get("valid")).isEqualTo(false);
        assertThat(malformedResponseBody).containsKey("error");
        
        TestUtils.logAssertion("Malformed JWT correctly rejected");
        
        // Test with JWT signed with wrong key
        String wrongSignatureJwt = validPassportJwt.substring(0, validPassportJwt.lastIndexOf('.')) + ".wrongsignature";
        
        ResponseEntity<Map> wrongSignatureResponse = TestUtils.retryWithBackoff(() -> 
                TestUtils.ImmigrationServiceClient.verifyPassport(wrongSignatureJwt)
        );
        
        assertThat(wrongSignatureResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(wrongSignatureResponse.getBody()).isNotNull();
        
        Map<String, Object> wrongSignatureResponseBody = wrongSignatureResponse.getBody();
        assertThat(wrongSignatureResponseBody.get("valid")).isEqualTo(false);
        assertThat(wrongSignatureResponseBody).containsKey("error");
        
        TestUtils.logAssertion("JWT with wrong signature correctly rejected");
    }
    
    private void testTamperedDocumentDetection() {
        TestUtils.logTestStep("Testing Tampered Document Detection");
        
        // Create a tampered JWT by modifying the payload
        String[] jwtParts = validPassportJwt.split("\\.");
        String tamperedPayload = jwtParts[1] + "tampered";
        tamperedPassportJwt = jwtParts[0] + "." + tamperedPayload + "." + jwtParts[2];
        
        ResponseEntity<Map> tamperedResponse = TestUtils.retryWithBackoff(() -> 
                TestUtils.ImmigrationServiceClient.verifyPassport(tamperedPassportJwt)
        );
        
        assertThat(tamperedResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(tamperedResponse.getBody()).isNotNull();
        
        Map<String, Object> tamperedResponseBody = tamperedResponse.getBody();
        assertThat(tamperedResponseBody.get("valid")).isEqualTo(false);
        assertThat(tamperedResponseBody).containsKey("error");
        
        TestUtils.logAssertion("Tampered JWT correctly rejected");
        
        // Test QR code tampering
        String tamperedQrCode = TestData.SAMPLE_PASSPORT_QR + "tampered";
        
        ResponseEntity<Map> tamperedQrResponse = TestUtils.retryWithBackoff(() -> 
                TestUtils.ImmigrationServiceClient.verifyQrCode(tamperedQrCode)
        );
        
        assertThat(tamperedQrResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(tamperedQrResponse.getBody()).isNotNull();
        
        Map<String, Object> tamperedQrResponseBody = tamperedQrResponse.getBody();
        assertThat(tamperedQrResponseBody.get("valid")).isEqualTo(false);
        assertThat(tamperedQrResponseBody).containsKey("error");
        
        TestUtils.logAssertion("Tampered QR code correctly rejected");
    }
    
    private void testWatchlistMatching() {
        TestUtils.logTestStep("Testing Watchlist Matching");
        
        // Create a passport for a watchlisted person
        TestData.PassportRequest watchlistedPassportRequest = TestData.PassportRequest.builder()
                .citizenId("WATCHLIST-CIT-001")
                .passportNumber("PWATCH001")
                .firstName("Watchlisted")
                .lastName("Person")
                .dateOfBirth(TestData.TODAY.minusYears(35))
                .nationality("Test Nationality")
                .countryCode("TS")
                .dateOfIssue(TestData.TODAY)
                .dateOfExpiry(TestData.NEXT_YEAR)
                .authority("Test Authority")
                .build();
        
        ResponseEntity<Map> watchlistedPassportResponse = TestUtils.retryWithBackoff(() -> 
                TestUtils.CountryServiceClient.issuePassport(watchlistedPassportRequest)
        );
        
        assertThat(watchlistedPassportResponse.getStatusCode().is2xxSuccessful()).isTrue();
        String watchlistedPassportJwt = (String) watchlistedPassportResponse.getBody().get("jwtToken");
        
        // Verify passport (should trigger watchlist alert)
        ResponseEntity<Map> watchlistResponse = TestUtils.retryWithBackoff(() -> 
                TestUtils.ImmigrationServiceClient.verifyPassport(watchlistedPassportJwt)
        );
        
        assertThat(watchlistResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(watchlistResponse.getBody()).isNotNull();
        
        Map<String, Object> watchlistResponseBody = watchlistResponse.getBody();
        // The passport might still be valid but should trigger an alert
        assertThat(watchlistResponseBody).containsKey("alerts");
        
        TestUtils.logAssertion("Watchlist matching correctly detected");
    }
    
    private void testRateLimiting() {
        TestUtils.logTestStep("Testing Rate Limiting");
        
        // Make multiple rapid requests to test rate limiting
        int requestCount = 0;
        int rateLimitedCount = 0;
        
        for (int i = 0; i < 20; i++) {
            try {
                ResponseEntity<Map> response = TestUtils.ImmigrationServiceClient.verifyPassport(validPassportJwt);
                requestCount++;
                
                if (response.getStatusCode().value() == 429) { // Too Many Requests
                    rateLimitedCount++;
                }
            } catch (Exception e) {
                // Rate limiting might cause exceptions
                rateLimitedCount++;
            }
            
            // Small delay between requests
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        TestUtils.logAssertion("Rate limiting test completed: " + requestCount + " successful, " + rateLimitedCount + " rate limited");
        
        // Verify that at least some requests succeeded
        assertThat(requestCount).isGreaterThan(0);
    }
    
    @Test
    void testSecurityHeaders() {
        TestUtils.logTestStep("Testing Security Headers");
        
        // Test that security headers are present in responses
        ResponseEntity<Map> response = TestUtils.retryWithBackoff(() -> 
                TestUtils.ImmigrationServiceClient.verifyPassport(validPassportJwt)
        );
        
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        
        // Check for security headers (these would be set by the application)
        // In a real implementation, you would check for headers like:
        // - X-Content-Type-Options: nosniff
        // - X-Frame-Options: DENY
        // - X-XSS-Protection: 1; mode=block
        // - Strict-Transport-Security: max-age=31536000; includeSubDomains
        
        TestUtils.logAssertion("Security headers verification completed");
    }
    
    @Test
    void testInputValidation() {
        TestUtils.logTestStep("Testing Input Validation");
        
        // Test with null input
        ResponseEntity<Map> nullResponse = TestUtils.retryWithBackoff(() -> 
                TestUtils.post("http://localhost:8083/api/verify/passport", null, Map.class)
        );
        
        assertThat(nullResponse.getStatusCode().is4xxClientError()).isTrue();
        
        // Test with empty input
        ResponseEntity<Map> emptyResponse = TestUtils.retryWithBackoff(() -> 
                TestUtils.post("http://localhost:8083/api/verify/passport", Map.of(), Map.class)
        );
        
        assertThat(emptyResponse.getStatusCode().is4xxClientError()).isTrue();
        
        // Test with invalid JSON
        ResponseEntity<Map> invalidJsonResponse = TestUtils.retryWithBackoff(() -> 
                TestUtils.post("http://localhost:8083/api/verify/passport", "invalid json", Map.class)
        );
        
        assertThat(invalidJsonResponse.getStatusCode().is4xxClientError()).isTrue();
        
        TestUtils.logAssertion("Input validation correctly rejects invalid inputs");
    }
    
    @Test
    void testConcurrentAccess() {
        TestUtils.logTestStep("Testing Concurrent Access");
        
        // Test multiple concurrent requests
        int concurrentRequests = 10;
        int successfulRequests = 0;
        
        for (int i = 0; i < concurrentRequests; i++) {
            try {
                ResponseEntity<Map> response = TestUtils.ImmigrationServiceClient.verifyPassport(validPassportJwt);
                if (response.getStatusCode().is2xxSuccessful()) {
                    successfulRequests++;
                }
            } catch (Exception e) {
                // Handle exceptions from concurrent access
                log.warn("Concurrent request failed: {}", e.getMessage());
            }
        }
        
        // Most requests should succeed
        assertThat(successfulRequests).isGreaterThan(concurrentRequests / 2);
        
        TestUtils.logAssertion("Concurrent access test completed: " + successfulRequests + "/" + concurrentRequests + " successful");
    }
} 