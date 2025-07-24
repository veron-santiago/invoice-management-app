package io.github.veron_santiago.backend.presentation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record BillRequest(@NotBlank(message = "El nombre del cliente no puede estar vacío")
                          @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
                          @Pattern(
                                  regexp = "^[\\p{L} .,'-]+$",
                                  message = "El nombre contiene caracteres no permitidos")
                          String customerName,

                          @Size(min = 5, max = 200, message = "La dirección debe tener entre 5 y 200 caracteres")
                          @Pattern(
                                  regexp = "^[\\p{L}0-9 .,'°/#-]+$",
                                  message = "La dirección contiene caracteres no permitidos")
                          String customerAddress,

                          @Size(min = 5, max = 200, message = "La dirección debe tener entre 5 y 200 caracteres")
                          @Pattern(
                                  regexp = "^[\\p{L}0-9 .,'°/#-]+$",
                                  message = "La dirección contiene caracteres no permitidos")
                          String companyAddress,

                          @Email(message = "Debe ingresar un email válido")
                          @Size(max = 100, message = "El email no debe superar los 100 caracteres")
                          String customerEmail,

                          @NotNull(message = "Debe haber al menos una línea en la factura")
                          @Size(min = 1, message = "Debe haber al menos una línea en la factura")
                          @Valid
                          List<BillLineRequest> billLineRequests
) {
}
