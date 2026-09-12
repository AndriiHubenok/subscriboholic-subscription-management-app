package com.anhub.subscriboholic.banking;

import com.anhub.subscriboholic.banking.dto.TransactionDTO;
import com.anhub.subscriboholic.banking.dto.TransactionsPageResponse;
import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
class BankingService {

    private final String applicationId;
    private final String keyId;
    private final PrivateKey privateKey;

    private static final int MIN_CONSECUTIVE_PAYMENTS = 2;

    public BankingService() throws Exception {
        Dotenv dotenv = Dotenv.load();

        this.applicationId =
                required(dotenv.get("ENABLE_BANKING_APPLICATION_ID"));

        this.keyId =
                required(dotenv.get("ENABLE_BANKING_KEY_ID"));

        String privateKeyPath =
                required(dotenv.get("ENABLE_BANKING_PRIVATE_KEY_PATH"));

        this.privateKey = loadPrivateKey(privateKeyPath);
    }

    public String getAuthorizationHeader() {
        Instant now = Instant.now();

        String jwt = Jwts.builder()
                .setHeaderParam("kid", keyId)
                .setIssuer(applicationId)
                .setAudience("api.enablebanking.com")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(300)))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();

        return jwt;
    }

    private static String required(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Required environment variable is missing");
        }
        return value;
    }

    private PrivateKey loadPrivateKey(String filename) throws Exception {
        String keyContent = Files.readString(Paths.get(filename));

        String privateKeyPem = keyContent
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] decoded = Base64.getDecoder().decode(privateKeyPem);

        PKCS8EncodedKeySpec keySpec =
                new PKCS8EncodedKeySpec(decoded);

        return KeyFactory.getInstance("RSA")
                .generatePrivate(keySpec);
    }

    public List<List<TransactionDTO>> findMonthlySubscriptions(List<TransactionDTO> transactions) {
        List<TransactionDTO> debits = transactions.stream()
                .filter(t -> "DBIT".equalsIgnoreCase(t.creditDebitIndicator()))
                .toList();

        Map<String, List<TransactionDTO>> grouped = debits.stream()
                .collect(Collectors.groupingBy(this::buildGroupKey));

        List<List<TransactionDTO>> detectedSubscriptions = new ArrayList<>();

        for (List<TransactionDTO> group : grouped.values()) {
            if (group.size() < MIN_CONSECUTIVE_PAYMENTS) {
                continue;
            }

            List<TransactionDTO> sortedGroup = group.stream()
                    .sorted(Comparator.comparing(TransactionDTO::bookingDate))
                    .toList();

            List<TransactionDTO> currentChain = new ArrayList<>();
            currentChain.add(sortedGroup.get(0));

            for (int i = 1; i < sortedGroup.size(); i++) {
                TransactionDTO prev = sortedGroup.get(i - 1);
                TransactionDTO curr = sortedGroup.get(i);

                long daysBetween = ChronoUnit.DAYS.between(prev.bookingDate(), curr.bookingDate());

                if (daysBetween >= 27 && daysBetween <= 34) {
                    currentChain.add(curr);
                } else {
                    if (currentChain.size() >= MIN_CONSECUTIVE_PAYMENTS) {
                        detectedSubscriptions.add(new ArrayList<>(currentChain));
                    }
                    currentChain.clear();
                    currentChain.add(curr);
                }
            }

            if (currentChain.size() >= MIN_CONSECUTIVE_PAYMENTS) {
                detectedSubscriptions.add(new ArrayList<>(currentChain));
            }
        }

        return detectedSubscriptions;
    }

    private String buildGroupKey(TransactionDTO t) {
        String merchant = "UNKNOWN";
        if (t.creditor() != null && t.creditor().name() != null) {
            merchant = t.creditor().name().trim().toLowerCase();
        } else if (t.remittanceInformation() != null && !t.remittanceInformation().isEmpty()) {
            merchant = t.remittanceInformation().get(0).trim().toLowerCase();
        }

        return merchant + "|" + t.transactionAmount().amount() + "|" + t.transactionAmount().currency();
    }
}
