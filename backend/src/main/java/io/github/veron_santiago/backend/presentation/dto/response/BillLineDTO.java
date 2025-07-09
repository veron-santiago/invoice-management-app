package io.github.veron_santiago.backend.presentation.dto.response;

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
public class BillLineDTO {

    private String code;

    @NotBlank
    private String name;
    @NotNull
    private int quantity;
    @NotNull
    private BigDecimal price;
    @NotNull
    private BigDecimal total;
    @NotNull
    private Long billId;
    private Long productId;


}
