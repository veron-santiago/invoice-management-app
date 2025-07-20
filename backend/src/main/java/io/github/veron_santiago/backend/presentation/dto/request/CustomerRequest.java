package io.github.veron_santiago.backend.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CustomerRequest(@NotBlank String name,
                              String address,
                              String email) {
}
