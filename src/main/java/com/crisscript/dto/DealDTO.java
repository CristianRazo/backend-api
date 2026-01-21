package com.crisscript.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DealDTO(
    String id,
    String title,
    String link,
    Integer temperature,
    String category
) {}