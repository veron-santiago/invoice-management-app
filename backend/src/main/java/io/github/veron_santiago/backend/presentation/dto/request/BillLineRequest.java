package io.github.veron_santiago.backend.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record BillLineRequest (@NotBlank String name,
                               String code,
                               @NotNull int quantity,
                               @NotNull BigDecimal price){
}
