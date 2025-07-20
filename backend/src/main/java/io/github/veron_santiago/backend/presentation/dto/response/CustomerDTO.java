package io.github.veron_santiago.backend.presentation.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDTO {

    @NotNull
    private Long id;
    @NotBlank
    private String name;
    private String email;
    private String address;
    @NotBlank
    private Long companyId;
    private List<Long> bills;

}
