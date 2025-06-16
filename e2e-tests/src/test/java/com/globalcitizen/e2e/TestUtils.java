package com.globalcitizen.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for end-to-end tests
 */
@Slf4j
public class TestUtils {
    
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    
    private static final RestTemplate restTemplate = new RestTemplate();
    
    // Service URLs
    private static final String CENTRAL_AUTHORITY_URL = System.getProperty("CENTRAL_AUTHORITY_URL", "http://localhost:8080");
    private static final String COUNTRY_SERVICE_URL = System.getProperty("COUNTRY_SERVICE_URL", "http://localhost:8081");
    private static final String EMBASSY_SERVICE_URL = System.getProperty("EMBASSY_SERVICE_URL", "http://localhost:8082");
    private static final String IMMIGRATION_SERVICE_URL = System.getProperty("IMMIGRATION_SERVICE_URL", "http://localhost:8083");
    private static final String UI_SERVICE_URL = System.getProperty("UI_SERVICE_URL", "http://localhost:8084");
    
    // Test configuration
    private static final int DEFAULT_TIMEOUT_SECONDS = 30;
    private static final int DEFAULT_RETRY_COUNT = 3;
    
    /**
     * Wait for a service to be healthy
     */
    public static void waitForServiceHealth(String serviceName, String healthUrl) {
        log.info("Waiting for {} service to be healthy at {}", serviceName, healthUrl);
        
        Awaitility.await()
                .atMost(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .pollInterval(2, TimeUnit.SECONDS)
                .until(() -> {
                    try {
                        ResponseEntity<String> response = restTemplate.getForEntity(healthUrl, String.class);
                        return response.getStatusCode().is2xxSuccessful();
                    } catch (Exception e) {
                        log.debug("Service {} not ready yet: {}", serviceName, e.getMessage());
                        return false;
                    }
                });
        
        log.info("{} service is healthy", serviceName);
    }
    
    /**
     * Wait for all services to be healthy
     */
    public static void waitForAllServices() {
        log.info("Waiting for all services to be healthy...");
        
        waitForServiceHealth("Central Authority", CENTRAL_AUTHORITY_URL + "/actuator/health");
        waitForServiceHealth("Country Service", COUNTRY_SERVICE_URL + "/actuator/health");
        waitForServiceHealth("Embassy Service", EMBASSY_SERVICE_URL + "/actuator/health");
        waitForServiceHealth("Immigration Service", IMMIGRATION_SERVICE_URL + "/actuator/health");
        waitForServiceHealth("UI Service", UI_SERVICE_URL + "/actuator/health");
        
        log.info("All services are healthy");
    }
    
    /**
     * Create HTTP headers with JSON content type
     */
    public static HttpHeaders createJsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
    
    /**
     * Create HTTP headers with JSON content type and authorization
     */
    public static HttpHeaders createJsonHeadersWithAuth(String token) {
        HttpHeaders headers = createJsonHeaders();
        headers.setBearerAuth(token);
        return headers;
    }
    
    /**
     * Make a GET request
     */
    public static <T> ResponseEntity<T> get(String url, Class<T> responseType) {
        return restTemplate.getForEntity(url, responseType);
    }
    
    /**
     * Make a POST request
     */
    public static <T> ResponseEntity<T> post(String url, Object request, Class<T> responseType) {
        HttpHeaders headers = createJsonHeaders();
        HttpEntity<Object> entity = new HttpEntity<>(request, headers);
        return restTemplate.postForEntity(url, entity, responseType);
    }
    
    /**
     * Make a POST request with authorization
     */
    public static <T> ResponseEntity<T> postWithAuth(String url, Object request, String token, Class<T> responseType) {
        HttpHeaders headers = createJsonHeadersWithAuth(token);
        HttpEntity<Object> entity = new HttpEntity<>(request, headers);
        return restTemplate.postForEntity(url, entity, responseType);
    }
    
    /**
     * Make a PUT request
     */
    public static <T> ResponseEntity<T> put(String url, Object request, Class<T> responseType) {
        HttpHeaders headers = createJsonHeaders();
        HttpEntity<Object> entity = new HttpEntity<>(request, headers);
        return restTemplate.exchange(url, HttpMethod.PUT, entity, responseType);
    }
    
    /**
     * Make a DELETE request
     */
    public static ResponseEntity<Void> delete(String url) {
        return restTemplate.exchange(url, HttpMethod.DELETE, null, Void.class);
    }
    
    /**
     * Convert object to JSON string
     */
    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert object to JSON", e);
        }
    }
    
    /**
     * Convert JSON string to object
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert JSON to object", e);
        }
    }
    
    /**
     * Convert JSON string to Map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> fromJsonToMap(String json) {
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert JSON to Map", e);
        }
    }
    
    /**
     * Retry operation with exponential backoff
     */
    public static <T> T retryWithBackoff(RetryableOperation<T> operation, int maxRetries) {
        Exception lastException = null;
        
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                return operation.execute();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxRetries) {
                    long delay = (long) Math.pow(2, attempt) * 1000; // Exponential backoff
                    log.warn("Operation failed, retrying in {} ms (attempt {}/{}): {}", 
                            delay, attempt + 1, maxRetries + 1, e.getMessage());
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Operation interrupted", ie);
                    }
                }
            }
        }
        
        throw new RuntimeException("Operation failed after " + (maxRetries + 1) + " attempts", lastException);
    }
    
    /**
     * Retry operation with default retry count
     */
    public static <T> T retryWithBackoff(RetryableOperation<T> operation) {
        return retryWithBackoff(operation, DEFAULT_RETRY_COUNT);
    }
    
    /**
     * Wait for condition with timeout
     */
    public static void waitForCondition(RetryableOperation<Boolean> condition, Duration timeout) {
        Awaitility.await()
                .atMost(timeout)
                .pollInterval(1, TimeUnit.SECONDS)
                .until(() -> {
                    try {
                        return condition.execute();
                    } catch (Exception e) {
                        log.debug("Condition check failed: {}", e.getMessage());
                        return false;
                    }
                });
    }
    
    /**
     * Wait for condition with default timeout
     */
    public static void waitForCondition(RetryableOperation<Boolean> condition) {
        waitForCondition(condition, Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS));
    }
    
    /**
     * Generate a unique test ID
     */
    public static String generateTestId(String prefix) {
        return prefix + "-" + System.currentTimeMillis() + "-" + Thread.currentThread().getId();
    }
    
    /**
     * Log test step
     */
    public static void logTestStep(String step) {
        log.info("=== TEST STEP: {} ===", step);
    }
    
    /**
     * Log test assertion
     */
    public static void logAssertion(String assertion) {
        log.info("✓ Assertion: {}", assertion);
    }
    
    /**
     * Log test error
     */
    public static void logError(String error) {
        log.error("✗ Error: {}", error);
    }
    
    /**
     * Create a test citizen with unique ID
     */
    public static TestData.Citizen createTestCitizen(String prefix) {
        String citizenId = generateTestId(prefix);
        return TestData.Citizen.builder()
                .citizenId(citizenId)
                .passportNumber("P" + citizenId.replace("-", ""))
                .firstName("Test")
                .lastName("Citizen")
                .dateOfBirth(TestData.TODAY.minusYears(25))
                .nationality("Test Nationality")
                .countryCode("TS")
                .build();
    }
    
    /**
     * Create a test visa request
     */
    public static TestData.VisaRequest createTestVisaRequest(String travelerId, String passportNumber) {
        return TestData.VisaRequest.builder()
                .travelerId(travelerId)
                .passportNumber(passportNumber)
                .firstName("Test")
                .lastName("Traveler")
                .nationality("Test Nationality")
                .visaType(TestData.VISA_TYPE_TOURIST)
                .purposeOfVisit(TestData.PURPOSE_TOURISM)
                .dateOfIssue(TestData.TODAY)
                .dateOfExpiry(TestData.NEXT_MONTH)
                .durationOfStay(30)
                .numberOfEntries("Single Entry")
                .issuingEmbassy("Test Embassy")
                .build();
    }
    
    /**
     * Create a test passport request
     */
    public static TestData.PassportRequest createTestPassportRequest(String citizenId, String passportNumber) {
        return TestData.PassportRequest.builder()
                .citizenId(citizenId)
                .passportNumber(passportNumber)
                .firstName("Test")
                .lastName("Citizen")
                .dateOfBirth(TestData.TODAY.minusYears(25))
                .nationality("Test Nationality")
                .countryCode("TS")
                .dateOfIssue(TestData.TODAY)
                .dateOfExpiry(TestData.NEXT_YEAR)
                .authority("Test Authority")
                .build();
    }
    
    /**
     * Functional interface for retryable operations
     */
    @FunctionalInterface
    public interface RetryableOperation<T> {
        T execute() throws Exception;
    }
    
    /**
     * Central Authority API client
     */
    public static class CentralAuthorityClient {
        
        public static ResponseEntity<Map> createMotherKey(Object request) {
            return post(CENTRAL_AUTHORITY_URL + "/api/keys/mother", request, Map.class);
        }
        
        public static ResponseEntity<Map> createGlobalKey(Object request) {
            return post(CENTRAL_AUTHORITY_URL + "/api/keys/global", request, Map.class);
        }
        
        public static ResponseEntity<Map> getMotherKey() {
            return get(CENTRAL_AUTHORITY_URL + "/api/keys/mother", Map.class);
        }
        
        public static ResponseEntity<Map> getGlobalKeys() {
            return get(CENTRAL_AUTHORITY_URL + "/api/keys/global", Map.class);
        }
        
        public static ResponseEntity<Map> createCountryCertificate(Object request) {
            return post(CENTRAL_AUTHORITY_URL + "/api/certificates/country", request, Map.class);
        }
        
        public static ResponseEntity<Map> getCountryCertificates() {
            return get(CENTRAL_AUTHORITY_URL + "/api/certificates/country", Map.class);
        }
    }
    
    /**
     * Country Service API client
     */
    public static class CountryServiceClient {
        
        public static ResponseEntity<Map> issuePassport(Object request) {
            return post(COUNTRY_SERVICE_URL + "/api/passports/issue", request, Map.class);
        }
        
        public static ResponseEntity<Map> getPassport(String citizenId) {
            return get(COUNTRY_SERVICE_URL + "/api/passports/" + citizenId, Map.class);
        }
        
        public static ResponseEntity<Map> searchPassport(String passportNumber) {
            return get(COUNTRY_SERVICE_URL + "/api/passports/search?passportNumber=" + passportNumber, Map.class);
        }
        
        public static ResponseEntity<Map> getPublicKey() {
            return get(COUNTRY_SERVICE_URL + "/api/keys/public", Map.class);
        }
        
        public static ResponseEntity<Map> revokePassport(String citizenId) {
            return put(COUNTRY_SERVICE_URL + "/api/passports/" + citizenId + "/revoke", null, Map.class);
        }
    }
    
    /**
     * Embassy Service API client
     */
    public static class EmbassyServiceClient {
        
        public static ResponseEntity<Map> issueVisa(Object request) {
            return post(EMBASSY_SERVICE_URL + "/api/visas/issue", request, Map.class);
        }
        
        public static ResponseEntity<Map> getVisa(String travelerId) {
            return get(EMBASSY_SERVICE_URL + "/api/visas/" + travelerId, Map.class);
        }
        
        public static ResponseEntity<Map> searchVisa(String visaNumber) {
            return get(EMBASSY_SERVICE_URL + "/api/visas/search?visaNumber=" + visaNumber, Map.class);
        }
        
        public static ResponseEntity<Map> verifyPassport(String jwtToken) {
            return post(EMBASSY_SERVICE_URL + "/api/passports/verify", Map.of("jwtToken", jwtToken), Map.class);
        }
        
        public static ResponseEntity<Map> getPublicKey() {
            return get(EMBASSY_SERVICE_URL + "/api/keys/public", Map.class);
        }
        
        public static ResponseEntity<Map> revokeVisa(String travelerId) {
            return put(EMBASSY_SERVICE_URL + "/api/visas/" + travelerId + "/revoke", null, Map.class);
        }
    }
    
    /**
     * Immigration Service API client
     */
    public static class ImmigrationServiceClient {
        
        public static ResponseEntity<Map> verifyPassport(String jwtToken) {
            return post(IMMIGRATION_SERVICE_URL + "/api/verify/passport", Map.of("jwtToken", jwtToken), Map.class);
        }
        
        public static ResponseEntity<Map> verifyVisa(String jwtToken) {
            return post(IMMIGRATION_SERVICE_URL + "/api/verify/visa", Map.of("jwtToken", jwtToken), Map.class);
        }
        
        public static ResponseEntity<Map> verifyQrCode(String qrCodeData) {
            return post(IMMIGRATION_SERVICE_URL + "/api/verify/qr", Map.of("qrCodeData", qrCodeData), Map.class);
        }
        
        public static ResponseEntity<Map> getPublicKeyRegistry() {
            return get(IMMIGRATION_SERVICE_URL + "/api/public-keys", Map.class);
        }
        
        public static ResponseEntity<Map> recordEntry(Object request) {
            return post(IMMIGRATION_SERVICE_URL + "/api/entry-log/record", request, Map.class);
        }
    }
} 