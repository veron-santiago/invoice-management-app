package io.github.veron_santiago.backend.presentation.dto.update;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompanyUpdateEmail (@NotBlank(message = "El email es obligatorio")
                                  @Email(message = "Debe ingresar un email válido")
                                  @Size(max = 100, message = "El email no debe superar los 100 caracteres")
                                  String email){
}
