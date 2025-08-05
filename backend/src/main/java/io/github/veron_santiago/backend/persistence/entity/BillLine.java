package io.github.veron_santiago.backend.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 6)
    private String code;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String name;

    @NotNull
    @Min(1)
    @Column(nullable = false)
    private int quantity;

    @NotNull
    @DecimalMin(value = "0.01")
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    @NotNull
    @DecimalMin(value = "0.01")
    @Column(precision = 14, scale = 2, nullable = false)
    private BigDecimal total;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id")
    private Bill bill;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

}
