package io.github.veron_santiago.backend.presentation.dto.response;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BillDTO {

    @NotNull
    private Long id;
    @NotNull
    private Long billNumber;
    @NotEmpty
    @PastOrPresent
    private LocalDate issueDate;
    private LocalDate dueDate;
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal totalAmount;
    private String companyName;
    private String companyEmail;
    private String companyAddress;
    @NotBlank
    private String customerName;
    private String customerEmail;
    private String customerAddress;
    private String pdfPath;
    @NotNull
    private Long companyId;
    private List<Long> billLines;
    private Long customerId;

}
