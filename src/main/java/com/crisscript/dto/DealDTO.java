package com.crisscript.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DealDTO(
    String id, 
    String title, 
    String link, 
    Double temperature, // Debe ser Double para que acepte decimales
    Double price, 
    String merchant
) {}