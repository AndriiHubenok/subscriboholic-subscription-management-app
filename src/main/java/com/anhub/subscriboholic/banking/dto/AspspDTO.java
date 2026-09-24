package com.anhub.subscriboholic.banking.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AspspDTO(
        String name,
        String country,
        String logo,
        @JsonProperty("psu_types") List<String> psuTypes,
        String bic
) {
}
