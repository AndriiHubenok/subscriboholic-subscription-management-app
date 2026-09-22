package com.anhub.subscriboholic.banking.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TransactionDTO(
        @JsonProperty("entry_reference") String entryReference,
        @JsonProperty("credit_debit_indicator") String creditDebitIndicator,
        @JsonProperty("booking_date") LocalDate bookingDate,
        @JsonProperty("transaction_amount") AmountDTO transactionAmount,
        CreditorDTO creditor,
        @JsonProperty("remittance_information") List<String> remittanceInformation,
        String status
) {}
