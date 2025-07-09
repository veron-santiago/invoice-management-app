package io.github.veron_santiago.backend.presentation.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"companyName", "message", "jwt", "status"})
public record AuthResponse (String companyName,
                            String message,
                            String jwt,
                            boolean status){

}