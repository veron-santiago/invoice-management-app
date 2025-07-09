package io.github.veron_santiago.backend.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record ProductRequest(@NotBlank String name,
                             String code,
                             @NotBlank BigDecimal price) {
}
