package com.anhub.subscriboholic.controller;

import com.anhub.subscriboholic.service.EnableBankingAuthService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
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
        requestBody.put("redirect_url", "http://localhost:60606/enable_banking_callback");

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

    @GetMapping("/enable_banking_callback")
    public ResponseEntity<String> handleBankCallback(
            @RequestParam("code") String code,
            @RequestParam("state") String state) {
        System.out.println("Handle Banking Callback is called");

        String authHeader = bankingAuthService.getAuthorizationHeader();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authHeader.substring("Bearer ".length()));
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> requestBody = Map.of("code", code);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://api.enablebanking.com/sessions",
                entity,
                String.class
        );

        return response;
    }

    @GetMapping("/api/bank-transactions/{accountId}")
    public ResponseEntity<String> fetchTransactions(@PathVariable String accountId) {
        String authHeader = bankingAuthService.getAuthorizationHeader();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authHeader.substring("Bearer ".length()));
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();

        String url = "https://api.enablebanking.com/accounts/" + accountId + "/transactions";

        return restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
        );
    }
}
