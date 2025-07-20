package io.github.veron_santiago.backend.presentation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record BillRequest(@NotBlank String customerName,
                          String customerEmail,
                          @NotNull @Valid List<BillLineRequest> billLineRequests) {
}
