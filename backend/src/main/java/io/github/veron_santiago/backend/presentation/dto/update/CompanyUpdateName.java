package io.github.veron_santiago.backend.presentation.dto.update;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CompanyUpdateName(@NotBlank(message = "El nombre no puede estar vacío")
                                @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
                                @Pattern(
                                        regexp = "^[\\p{L}0-9 .,'-_]+$",
                                        message = "El nombre contiene caracteres no permitidos")
                                String name) {
}
