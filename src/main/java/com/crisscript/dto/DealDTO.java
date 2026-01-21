package com.crisscript.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DealDTO(
    String id, 
    String title, 
    String link, 
    @JsonProperty("temperature") Double temperature, 
    @JsonProperty("price") Double price, 
    @JsonProperty("merchant") String merchant,
    @JsonProperty("isExpired") Boolean isExpired
) {}