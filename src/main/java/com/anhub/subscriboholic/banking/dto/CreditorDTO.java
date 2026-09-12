package com.anhub.subscriboholic.banking.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CreditorDTO(
        String name
) {}
