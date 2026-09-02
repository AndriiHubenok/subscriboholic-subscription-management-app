package com.anhub.subscriboholic.service;

import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Service
public class EnableBankingAuthService {

    private final String applicationId;
    private final String keyId;
    private final PrivateKey privateKey;

    public EnableBankingAuthService() throws Exception {
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

        return "Bearer " + jwt;
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
}
