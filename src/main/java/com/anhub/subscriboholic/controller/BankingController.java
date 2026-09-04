package com.anhub.subscriboholic.controller;

import com.anhub.subscriboholic.service.EnableBankingAuthService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
public class BankingController {

    private final EnableBankingAuthService bankingAuthService;

    @GetMapping("/api/bank-data")
    public ResponseEntity<String> fetchBankData() {
        String authHeader = bankingAuthService.getAuthorizationHeader();

        System.out.println("authHeader: " + authHeader);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authHeader.substring("Bearer ".length()));
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();

        return restTemplate.exchange(
                "https://api.enablebanking.com/aspsps",
                HttpMethod.GET,
                entity,
                String.class
        );
    }

    @PostMapping("/api/bank-auth")
    public ResponseEntity<String> authBanking() {
        String authHeader = bankingAuthService.getAuthorizationHeader();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authHeader.substring("Bearer ".length()));
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);

        String validUntil = java.time.Instant.now()
                .plus(90, java.time.temporal.ChronoUnit.DAYS)
                .toString();

        Map<String, Object> requestBody = new java.util.HashMap<>();

        requestBody.put("access", java.util.Map.of("valid_until", validUntil));
        requestBody.put("aspsp", java.util.Map.of("name", "Nordea", "country", "FI"));
        requestBody.put("state", java.util.UUID.randomUUID().toString());
        requestBody.put("redirect_url", "http://localhost:8080/enable_banking_callback");

        // Optional params
//        requestBody.put("psu_type", "personal");
//        requestBody.put("auth_method", "methodName");
//        requestBody.put("credentials", java.util.Map.of("userId", "MyUsername"));
//        requestBody.put("credentials_autosubmit", true);
//        requestBody.put("language", "en");
//        requestBody.put("psu_id", "string");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        RestTemplate restTemplate = new RestTemplate();

        return restTemplate.postForEntity(
                "https://api.enablebanking.com/auth",
                entity,
                String.class
        );
    }
}
