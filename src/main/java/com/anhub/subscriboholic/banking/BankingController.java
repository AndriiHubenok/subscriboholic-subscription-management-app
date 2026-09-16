package com.anhub.subscriboholic.banking;

import com.anhub.subscriboholic.banking.dto.AspspDTO;
import com.anhub.subscriboholic.banking.dto.TransactionDTO;
import com.anhub.subscriboholic.banking.dto.TransactionsPageResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.AllArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/banking")
class BankingController {

    private final BankingService bankingService;
    private final Cache<String, String> cache;

    @GetMapping("/bank-data")
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

    @PostMapping("/bank-auth")
    public ResponseEntity<String> authBanking(@RequestBody AspspDTO aspspDTO) {
        String authHeader = bankingService.getAuthorizationHeader();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authHeader);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);

        String validUntil = Instant.now()
                .plus(90, ChronoUnit.DAYS)
                .toString();

        Map<String, Object> requestBody = new HashMap<>();

        requestBody.put("access", Map.of("valid_until", validUntil));
        requestBody.put("aspsp", aspspDTO);
        requestBody.put("state", UUID.randomUUID().toString());
        requestBody.put("redirect_url", "http://localhost:60606/api/banking/enable_banking_callback");

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

    @GetMapping("/bank-transactions/{accountId}")
    public ResponseEntity<List<TransactionDTO>> fetchTransactions(@PathVariable String accountId) {
        List<TransactionDTO> result = bankingService.requestTransactions(accountId);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/possible-subscriptions/{accountId}")
    public ResponseEntity<List<TransactionDTO>> fetchPossibleTransactions(@PathVariable String accountId) {
        List<TransactionDTO> transactions = bankingService.requestTransactions(accountId);
        List<TransactionDTO> possibleSubscriptions = bankingService.findMonthlySubscriptions(transactions)
                .stream().map(List::getLast).toList();
        return ResponseEntity.ok(possibleSubscriptions);
    }
}
