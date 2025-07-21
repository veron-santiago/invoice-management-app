package io.github.veron_santiago.backend.presentation.dto.update;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CompanyUpdateEmail (@NotBlank @Email String email){
}
