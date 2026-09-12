package com.anhub.subscriboholic.banking.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TransactionsPageResponse(
        List<TransactionDTO> transactions,
        @JsonProperty("continuation_key") String continuationKey
) {}
