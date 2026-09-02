package com.anhub.subscriboholic.controller;

import com.anhub.subscriboholic.service.EnableBankingAuthService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@AllArgsConstructor
public class BankingController {

    private final EnableBankingAuthService bankingAuthService;

    @GetMapping("/api/bank-data")
    public ResponseEntity<String> fetchBankData() {
        String authHeader = bankingAuthService.getAuthorizationHeader();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authHeader.substring("Bearer ".length()));
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();

        return restTemplate.exchange(
                "https://api.enablebanking.com/aspsps?country=DE",
                HttpMethod.GET,
                entity,
                String.class
        );
    }
}
