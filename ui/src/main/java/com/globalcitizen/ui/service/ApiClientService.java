package com.globalcitizen.ui.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiClientService {
    
    @Value("${api.central-authority.url:http://localhost:8081}")
    private String centralAuthorityUrl;
    
    @Value("${api.country-service.url:http://localhost:8082}")
    private String countryServiceUrl;
    
    @Value("${api.embassy-service.url:http://localhost:8083}")
    private String embassyServiceUrl;
    
    @Value("${api.immigration-service.url:http://localhost:8084}")
    private String immigrationServiceUrl;
    
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    
    public ApiClientService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                .build();
    }
    
    // Central Authority API calls
    public Mono<Map> getMotherKey() {
        return webClient.get()
                .uri(centralAuthorityUrl + "/api/keys/mother")
                .retrieve()
                .bodyToMono(Map.class);
    }
    
    public Mono<Map> getGlobalKeys() {
        return webClient.get()
                .uri(centralAuthorityUrl + "/api/keys/global")
                .retrieve()
                .bodyToMono(Map.class);
    }
    
    public Mono<Map> getKeyStatistics() {
        return webClient.get()
                .uri(centralAuthorityUrl + "/api/keys/stats")
                .retrieve()
                .bodyToMono(Map.class);
    }
    
    public Mono<Map> requestCertificate(Map request) {
        return webClient.post()
                .uri(centralAuthorityUrl + "/api/certificates/request")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class);
    }
    
    // Country Service API calls
    public Mono<Map> issuePassport(Map passportData) {
        return webClient.post()
                .uri(countryServiceUrl + "/api/passports/issue")
                .bodyValue(passportData)
                .retrieve()
                .bodyToMono(Map.class);
    }
    
    public Mono<Map> getPassportByCitizenId(String citizenId) {
        return webClient.get()
                .uri(countryServiceUrl + "/api/passports/citizen/" + citizenId)
                .retrieve()
                .bodyToMono(Map.class);
    }
    
    public Mono<Map> getCountryPublicKey() {
        return webClient.get()
                .uri(countryServiceUrl + "/api/country-key/public")
                .retrieve()
                .bodyToMono(Map.class);
    }
    
    // Embassy Service API calls
    public Mono<Map> issueVisa(Map visaData) {
        return webClient.post()
                .uri(embassyServiceUrl + "/api/visas/issue")
                .bodyValue(visaData)
                .retrieve()
                .bodyToMono(Map.class);
    }
    
    public Mono<Map> getVisaById(String visaId) {
        return webClient.get()
                .uri(embassyServiceUrl + "/api/visas/id/" + visaId)
                .retrieve()
                .bodyToMono(Map.class);
    }
    
    public Mono<Map> getEmbassyPublicKey() {
        return webClient.get()
                .uri(embassyServiceUrl + "/api/embassy-key/public")
                .retrieve()
                .bodyToMono(Map.class);
    }
    
    // Immigration Service API calls
    public Mono<Map> validatePassport(Map validationRequest) {
        return webClient.post()
                .uri(immigrationServiceUrl + "/api/validate/passport")
                .bodyValue(validationRequest)
                .retrieve()
                .bodyToMono(Map.class);
    }
    
    public Mono<Map> validateVisa(Map validationRequest) {
        return webClient.post()
                .uri(immigrationServiceUrl + "/api/validate/visa")
                .bodyValue(validationRequest)
                .retrieve()
                .bodyToMono(Map.class);
    }
    
    // Central Authority endpoints
    public Mono<Map<String, Object>> getCentralAuthorityStats() {
        return webClient.get()
                .uri("/central-authority/stats")
                .retrieve()
                .bodyToMono(Map.class)
                .doOnError(error -> log.error("Error fetching central authority stats: {}", error.getMessage()));
    }
    
    public Mono<Map<String, Object>> getCountryCertificates() {
        return webClient.get()
                .uri("/central-authority/country-certificates")
                .retrieve()
                .bodyToMono(Map.class)
                .doOnError(error -> log.error("Error fetching country certificates: {}", error.getMessage()));
    }
    
    // Country Service endpoints
    public Mono<Map<String, Object>> getCountryPublicKey() {
        return webClient.get()
                .uri("/country/public-key")
                .retrieve()
                .bodyToMono(Map.class)
                .doOnError(error -> log.error("Error fetching country public key: {}", error.getMessage()));
    }
    
    public Mono<Map<String, Object>> issuePassport(Map<String, String> passportData) {
        return webClient.post()
                .uri("/country/passport/issue")
                .bodyValue(passportData)
                .retrieve()
                .bodyToMono(Map.class)
                .doOnSuccess(result -> log.info("Passport issued successfully for citizen: {}", passportData.get("citizenId")))
                .doOnError(error -> log.error("Error issuing passport: {}", error.getMessage()));
    }
    
    public Mono<Map<String, Object>> getPassportByCitizenId(String citizenId) {
        return webClient.get()
                .uri("/country/passport/{citizenId}", citizenId)
                .retrieve()
                .bodyToMono(Map.class)
                .doOnError(error -> log.error("Error fetching passport for citizen {}: {}", citizenId, error.getMessage()));
    }
    
    public Mono<Map<String, Object>> getAllPassports() {
        return webClient.get()
                .uri("/country/passports")
                .retrieve()
                .bodyToMono(Map.class)
                .doOnError(error -> log.error("Error fetching all passports: {}", error.getMessage()));
    }
    
    public Mono<Map<String, Object>> revokePassport(String citizenId) {
        return webClient.delete()
                .uri("/country/passport/{citizenId}", citizenId)
                .retrieve()
                .bodyToMono(Map.class)
                .doOnSuccess(result -> log.info("Passport revoked successfully for citizen: {}", citizenId))
                .doOnError(error -> log.error("Error revoking passport: {}", error.getMessage()));
    }
    
    // Embassy Service endpoints
    public Mono<Map<String, Object>> getEmbassyPublicKey() {
        return webClient.get()
                .uri("/embassy/public-key")
                .retrieve()
                .bodyToMono(Map.class)
                .doOnError(error -> log.error("Error fetching embassy public key: {}", error.getMessage()));
    }
    
    public Mono<Map<String, Object>> issueVisa(Map<String, String> visaData) {
        return webClient.post()
                .uri("/embassy/visa/issue")
                .bodyValue(visaData)
                .retrieve()
                .bodyToMono(Map.class)
                .doOnSuccess(result -> log.info("Visa issued successfully for traveler: {}", visaData.get("travelerId")))
                .doOnError(error -> log.error("Error issuing visa: {}", error.getMessage()));
    }
    
    public Mono<Map<String, Object>> getVisaByTravelerId(String travelerId) {
        return webClient.get()
                .uri("/embassy/visa/{travelerId}", travelerId)
                .retrieve()
                .bodyToMono(Map.class)
                .doOnError(error -> log.error("Error fetching visa for traveler {}: {}", travelerId, error.getMessage()));
    }
    
    // Immigration Service endpoints
    public Mono<Map<String, Object>> verifyPassport(String jwtToken) {
        return webClient.post()
                .uri(immigrationServiceUrl + "/verify/passport")
                .bodyValue(Map.of("jwtToken", jwtToken))
                .retrieve()
                .bodyToMono(Map.class)
                .onErrorResume(e -> {
                    log.error("Error verifying passport: {}", e.getMessage());
                    return Mono.error(e);
                });
    }
    
    public Mono<Map<String, Object>> verifyVisa(String jwtToken) {
        return webClient.post()
                .uri(immigrationServiceUrl + "/verify/visa")
                .bodyValue(Map.of("jwtToken", jwtToken))
                .retrieve()
                .bodyToMono(Map.class)
                .onErrorResume(e -> {
                    log.error("Error verifying visa: {}", e.getMessage());
                    return Mono.error(e);
                });
    }
    
    public Mono<Map<String, Object>> verifyQrCode(String qrCodeData) {
        return webClient.post()
                .uri(immigrationServiceUrl + "/verify/qr")
                .bodyValue(Map.of("qrCodeData", qrCodeData))
                .retrieve()
                .bodyToMono(Map.class)
                .onErrorResume(e -> {
                    log.error("Error verifying QR code: {}", e.getMessage());
                    return Mono.error(e);
                });
    }
    
    public Mono<Map<String, Object>> getPublicKeyRegistry() {
        return webClient.get()
                .uri(immigrationServiceUrl + "/public-keys")
                .retrieve()
                .bodyToMono(Map.class)
                .onErrorResume(e -> {
                    log.error("Error fetching public key registry: {}", e.getMessage());
                    return Mono.error(e);
                });
    }
    
    public Mono<Map<String, Object>> getEntryLog(String startDate, String endDate, String type, String status) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(immigrationServiceUrl + "/entry-log")
                        .queryParam("startDate", startDate)
                        .queryParam("endDate", endDate)
                        .queryParam("type", type)
                        .queryParam("status", status)
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .onErrorResume(e -> {
                    log.error("Error fetching entry log: {}", e.getMessage());
                    return Mono.error(e);
                });
    }
    
    public Mono<Map<String, Object>> getSecurityAlerts(String priority, String type, String status) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(immigrationServiceUrl + "/alerts")
                        .queryParam("priority", priority)
                        .queryParam("type", type)
                        .queryParam("status", status)
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .onErrorResume(e -> {
                    log.error("Error fetching security alerts: {}", e.getMessage());
                    return Mono.error(e);
                });
    }
    
    public Mono<Map<String, Object>> getImmigrationStatistics(String timePeriod, String startDate, String endDate) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(immigrationServiceUrl + "/statistics")
                        .queryParam("timePeriod", timePeriod)
                        .queryParam("startDate", startDate)
                        .queryParam("endDate", endDate)
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .onErrorResume(e -> {
                    log.error("Error fetching immigration statistics: {}", e.getMessage());
                    return Mono.error(e);
                });
    }
    
    public Mono<Map<String, Object>> acknowledgeAlert(String alertId) {
        return webClient.post()
                .uri(immigrationServiceUrl + "/alerts/" + alertId + "/acknowledge")
                .retrieve()
                .bodyToMono(Map.class)
                .onErrorResume(e -> {
                    log.error("Error acknowledging alert: {}", e.getMessage());
                    return Mono.error(e);
                });
    }
    
    public Mono<Map<String, Object>> recordEntry(String travelerId, String documentType, String documentNumber) {
        return webClient.post()
                .uri(immigrationServiceUrl + "/entry-log/record")
                .bodyValue(Map.of(
                    "travelerId", travelerId,
                    "documentType", documentType,
                    "documentNumber", documentNumber,
                    "timestamp", java.time.LocalDateTime.now().toString()
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .onErrorResume(e -> {
                    log.error("Error recording entry: {}", e.getMessage());
                    return Mono.error(e);
                });
    }
} 