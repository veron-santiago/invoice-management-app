package io.github.veron_santiago.backend.presentation.dto.update;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CustomerUpdateRequest(@Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
                                    @Pattern(
                                            regexp = "^[\\p{L} .,'-]+$",
                                            message = "El nombre contiene caracteres no permitidos")
                                    String name,

                                    @Email(message = "Debe ingresar un email válido")
                                    @Size(max = 100, message = "El email no debe superar los 100 caracteres")
                                    String email,

                                    @Size(min = 5, max = 200, message = "La dirección debe tener entre 5 y 200 caracteres")
                                    @Pattern(
                                            regexp = "^[\\p{L}0-9 .,'°/#-]+$",
                                            message = "La dirección contiene caracteres no permitidos")
                                    String address) {
}
