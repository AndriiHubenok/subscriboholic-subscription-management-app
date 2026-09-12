package com.anhub.subscriboholic.banking;

import com.anhub.subscriboholic.banking.dto.TransactionDTO;
import com.anhub.subscriboholic.banking.dto.TransactionsPageResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
class BankingController {

    private final BankingService bankingService;

    @GetMapping("/api/bank-data")
    public ResponseEntity<String> fetchBankData() {
        String authHeader = bankingService.getAuthorizationHeader();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authHeader);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();

        return restTemplate.exchange(
                "https://api.enablebanking.com/aspsps?country=LT",
                HttpMethod.GET,
                entity,
                String.class
        );
    }

    @PostMapping("/api/bank-auth")
    public ResponseEntity<String> authBanking() {
        String authHeader = bankingService.getAuthorizationHeader();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authHeader);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);

        String validUntil = java.time.Instant.now()
                .plus(90, java.time.temporal.ChronoUnit.DAYS)
                .toString();

        Map<String, Object> requestBody = new java.util.HashMap<>();

        requestBody.put("access", java.util.Map.of("valid_until", validUntil));
        requestBody.put("aspsp", java.util.Map.of("name", "Mock ASPSP", "country", "DE"));
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

        String authHeader = bankingService.getAuthorizationHeader();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authHeader);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> requestBody = Map.of("code", code);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();

        return restTemplate.postForEntity(
                "https://api.enablebanking.com/sessions",
                entity,
                String.class
        );
    }

    @GetMapping("/api/bank-transactions/{accountId}")
    public ResponseEntity<String> fetchTransactions(@PathVariable String accountId,
                                                    @RequestParam(name = "continuation_key", required = false) String continuationKey) {
        String authHeader = bankingService.getAuthorizationHeader();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authHeader);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();

        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString("https://api.enablebanking.com/accounts/" + accountId + "/transactions");

        if (continuationKey != null && !continuationKey.isBlank()) {
            uriBuilder.queryParam("continuation_key", continuationKey);
        }

        URI targetUri = uriBuilder.build().encode().toUri();

        return restTemplate.exchange(
                targetUri,
                HttpMethod.GET,
                entity,
                String.class
        );
    }

    @GetMapping("/api/possible-subscriptions/{accountId}")
    public ResponseEntity<List<List<TransactionDTO>>> fetchPossibleTransactions(@PathVariable String accountId) {
        String authHeader = bankingService.getAuthorizationHeader();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authHeader);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();

        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString("https://api.enablebanking.com/accounts/" + accountId + "/transactions");

        URI targetUri = uriBuilder.build().encode().toUri();

        ResponseEntity<TransactionsPageResponse> response = restTemplate.exchange(
                targetUri,
                HttpMethod.GET,
                entity,
                TransactionsPageResponse.class
        );


        List<TransactionDTO> transactions = response.getBody() != null ? response.getBody().transactions() : new ArrayList<>();

        return ResponseEntity.ok(bankingService.findMonthlySubscriptions(transactions));
    }
}
