package io.github.veron_santiago.backend.presentation.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AuthRequest(@NotBlank(message = "El nombre es obligatorio")
                          @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
                          @Pattern(
                                  regexp = "^[\\p{L} .,'-_]$",
                                  message = "El nombre contiene caracteres no permitidos")
                          String companyName,

                          @NotBlank(message = "La contraseña es obligatoria")
                          @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
                          @Size(max = 64, message = "La contraseña debe tener como máximo 64 caracteres")
                          @Pattern(
                                  regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s]).$",
                                  message = "La contraseña debe tener al menos una mayúscula, una minúscula, un número y un símbolo")
                          String password,

                          Boolean stayLogged) {
}
