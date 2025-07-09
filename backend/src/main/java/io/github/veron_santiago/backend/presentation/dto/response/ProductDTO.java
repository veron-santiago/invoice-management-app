package io.github.veron_santiago.backend.presentation.dto.response;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

    @NotNull
    private Long id;
    private String code;
    @NotBlank
    private String name;
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal price;
    @NotNull
    private Long companyId;

}
