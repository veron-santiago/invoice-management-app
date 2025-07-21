package io.github.veron_santiago.backend.presentation.dto.update;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProductUpdateRequest(@Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
                                   @Pattern(
                                           regexp = "^[\\p{L}0-9 .,'-)(/+%:]$",
                                           message = "El nombre solo puede contener letras, números y ciertos símbolos")
                                   String name,

                                   @Size(max = 8, message = "El código no puede tener más de 8 caracteres")
                                   @Pattern(
                                           regexp = "^[a-zA-Z0-9_.-]$",
                                           message = "El código solo puede contener letras, números, puntos, guiones y guiones bajos")
                                   String code,

                                   @DecimalMin(value = "0.01", message = "El precio mínimo es 0.01")
                                   @DecimalMax(value = "10000000.00", message = "El precio máximo es 10.000.000")
                                   BigDecimal price) {
}
