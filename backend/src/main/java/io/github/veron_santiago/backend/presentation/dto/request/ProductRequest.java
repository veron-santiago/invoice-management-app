package io.github.veron_santiago.backend.presentation.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProductRequest(@NotBlank(message = "El nombre del producto no puede estar vacío")
                             @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
                             @Pattern(
                                     regexp = "^[\\p{L}0-9 .,'-)(/+%:]*$",
                                     message = "El nombre contiene caracteres inválidos")
                             String name,

                             @Size(max = 6, message = "El código no puede tener más de 6 caracteres")
                             @Pattern(
                                     regexp = "^[a-zA-Z0-9]*$",
                                     message = "El código solo puede contener números y letras")
                             String code,

                             @NotNull(message = "El precio es obligatorio")
                             @DecimalMin(value = "0.01", message = "El precio mínimo es 0.01")
                             @DecimalMax(value = "10000000.00", message = "El precio máximo es 10.000.000")
                             BigDecimal price) {
}
