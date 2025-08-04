package io.github.veron_santiago.backend.presentation.dto.update;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CompanyUpdateAddress(@Size(max = 200, message = "La dirección debe tener como máximo 200 caracteres")
                                   @Pattern(
                                           regexp = "^[\\p{L}0-9\\s.,'°/#-]*$",
                                           message = "La dirección contiene caracteres inválidos")
                                   String address) {
}
