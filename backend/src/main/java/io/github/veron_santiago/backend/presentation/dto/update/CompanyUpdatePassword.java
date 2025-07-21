package io.github.veron_santiago.backend.presentation.dto.update;

import jakarta.validation.constraints.NotBlank;

public record CompanyUpdatePassword (@NotBlank String actualPassword,
                                     @NotBlank String newPassword){
}
