package io.github.veron_santiago.backend.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record BillRequest(@NotBlank String customerName,
                          String customerEmail,
                          @NotNull BigDecimal totalAmount
                          ) {
}
