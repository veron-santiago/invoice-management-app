package io.github.veron_santiago.backend.presentation.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CustomerRequest(@NotBlank(message = "El nombre del cliente no puede estar vacío")
                              @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
                              @Pattern(
                                      regexp = "^[\\p{L} .,'-]*$",
                                      message = "El nombre contiene caracteres inválidos")
                              String name,

                              @Size(min = 5, max = 200, message = "La dirección debe tener entre 5 y 200 caracteres")
                              @Pattern(
                                      regexp = "^[\\p{L}0-9 .,'°/#-]*$",
                                      message = "La dirección contiene caracteres inválidos")
                              String address,

                              @Email(message = "Debe ingresar un email válido")
                              @Size(max = 100, message = "El email no debe superar los 100 caracteres")
                              String email) {
}
