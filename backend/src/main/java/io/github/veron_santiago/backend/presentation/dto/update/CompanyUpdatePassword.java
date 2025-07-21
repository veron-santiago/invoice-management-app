package io.github.veron_santiago.backend.presentation.dto.update;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CompanyUpdatePassword (@NotBlank(message = "La contraseña actual no puede estar vacía")
                                     String actualPassword,

                                     @NotBlank(message = "La nueva contraseña no puede estar vacía")
                                     @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
                                     @Size(max = 64, message = "La contraseña debe tener como máximo 64 caracteres")
                                     @Pattern(
                                             regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s]).$",
                                             message = "La contraseña debe tener al menos una mayúscula, una minúscula, un número y un símbolo")
                                     String newPassword){
}
