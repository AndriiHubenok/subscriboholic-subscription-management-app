package com.anhub.subscriboholic.banking.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AmountDTO(
        BigDecimal amount,
        String currency
) {}
