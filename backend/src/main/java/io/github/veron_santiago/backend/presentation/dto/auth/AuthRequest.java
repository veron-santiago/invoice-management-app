package io.github.veron_santiago.backend.presentation.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record AuthRequest(@NotBlank String companyName,
                          @NotBlank String password,
                          Boolean stayLogged) {
}
