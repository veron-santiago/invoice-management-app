package io.github.veron_santiago.backend.presentation.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record AuthCreateCompany(@NotBlank String companyName,
                                @NotBlank String password,
                                @NotBlank String email) {
}
