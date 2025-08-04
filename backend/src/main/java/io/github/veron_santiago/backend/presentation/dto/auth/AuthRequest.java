package io.github.veron_santiago.backend.presentation.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AuthRequest(@NotBlank(message = "Ingrese un nombre o email")
                          @Size(min = 3, message = "El nombre o email debe tener al menos 3 caracteres")
                          @Size(max = 100, message = "El nombre o email debe tener como máximo 100 caracteres")
                          String companyName,

                          @NotBlank(message = "La contraseña es obligatoria")
                          @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
                          @Size(max = 64, message = "La contraseña debe tener como máximo 64 caracteres")
                          String password,

                          Boolean stayLogged) {
}
