package io.github.veron_santiago.backend.presentation.dto.update;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CompanyUpdateAddress(@Size(min = 5, max = 200, message = "La dirección debe tener entre 5 y 200 caracteres")
                                   @Pattern(
                                           regexp = "^[\\p{L}0-9 .,'°/#-]*$",
                                           message = "La dirección contiene caracteres no permitidos")
                                   String address) {
}
