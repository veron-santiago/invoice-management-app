package io.github.veron_santiago.backend.presentation.dto.response;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDTO {

    @NotNull
    private Long id;
    @Size(min = 3, max = 50)
    private String companyName;
    @Email
    @NotBlank
    private String email;
    private String address;
    private String logoPath;
    private boolean verified;
    private List<Long> bills;
    private Set<Long> customers;
    private Set<Long> products;

}
