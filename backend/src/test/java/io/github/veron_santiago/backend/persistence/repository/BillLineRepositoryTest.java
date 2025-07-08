package io.github.veron_santiago.backend.persistence.repository;

import io.github.veron_santiago.backend.persistence.entity.Bill;
import io.github.veron_santiago.backend.persistence.entity.BillLine;
import io.github.veron_santiago.backend.persistence.entity.Company;
import io.github.veron_santiago.backend.persistence.entity.Product;
import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@DataJpaTest
public class BillLineRepositoryTest {

    @Autowired
    private IBillLineRepository billLineRepository;
    @Autowired
    private IBillRepository billRepository;
    @Autowired
    private IProductRepository productRepository;
    @Autowired
    private ICompanyRepository companyRepository;
    @Autowired
    private EntityManager entityManager;

    private Company company;
    private Product product;
    private Bill bill;
    private BillLine savedLine;

    @BeforeEach
    void setUp() {
        billLineRepository.deleteAll();
        billRepository.deleteAll();
        productRepository.deleteAll();
        companyRepository.deleteAll();

        company = companyRepository.saveAndFlush(
                Company.builder()
                        .companyName("compañia")
                        .email("email@mail.com")
                        .password("12345")
                        .build()
        );

        product = productRepository.saveAndFlush(
                Product.builder()
                        .code("P001")
                        .name("producto")
                        .price(BigDecimal.valueOf(50))
                        .company(company)
                        .build()
        );

        bill = billRepository.saveAndFlush(
                Bill.builder()
                        .billNumber(1L)
                        .issueDate(LocalDate.now())
                        .totalAmount(BigDecimal.valueOf(200))
                        .customerName("Cliente Genérico")
                        .customerEmail("cliente@mail.com")
                        .company(company)
                        .build()
        );

        savedLine = billLineRepository.saveAndFlush(
                BillLine.builder()
                        .code("L001")
                        .name("línea 1")
                        .quantity(2)
                        .price(BigDecimal.valueOf(25))
                        .total(BigDecimal.valueOf(50))
                        .bill(bill)
                        .product(product)
                        .build()
        );
    }

    @Test
    void shouldSaveBillLineSuccessfully() {
        assertThat(savedLine.getId()).isNotNull();
        assertThat(savedLine.getCode()).isEqualTo("L001");
        assertThat(savedLine.getName()).isEqualTo("línea 1");
        assertThat(savedLine.getQuantity()).isEqualTo(2);
        assertThat(savedLine.getPrice()).isEqualByComparingTo("25.00");
        assertThat(savedLine.getTotal()).isEqualByComparingTo("50.00");
        assertThat(savedLine.getBill().getId()).isEqualTo(bill.getId());
        assertThat(savedLine.getProduct().getId()).isEqualTo(product.getId());
    }

    @Test
    void shouldAllowNullCode() {
        BillLine noCode = BillLine.builder()
                .name("sin código")
                .quantity(1)
                .price(BigDecimal.valueOf(10))
                .total(BigDecimal.valueOf(10))
                .bill(bill)
                .build();

        BillLine saved = billLineRepository.saveAndFlush(noCode);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCode()).isNull();
    }

    @Test
    void shouldNotAllowMissingRequiredFields() {
        BillLine noName = BillLine.builder()
                .quantity(1)
                .price(BigDecimal.valueOf(10))
                .total(BigDecimal.valueOf(10))
                .bill(bill)
                .build();
        assertThatThrownBy(() -> billLineRepository.saveAndFlush(noName))
                .isInstanceOf(ConstraintViolationException.class);

        BillLine badQty = BillLine.builder()
                .name("item")
                .quantity(0)
                .price(BigDecimal.valueOf(10))
                .total(BigDecimal.valueOf(0))
                .bill(bill)
                .build();
        assertThatThrownBy(() -> billLineRepository.saveAndFlush(badQty))
                .isInstanceOf(ConstraintViolationException.class);

        BillLine noPrice = BillLine.builder()
                .name("item")
                .quantity(1)
                .total(BigDecimal.valueOf(10))
                .bill(bill)
                .build();
        assertThatThrownBy(() -> billLineRepository.saveAndFlush(noPrice))
                .isInstanceOf(ConstraintViolationException.class);

        BillLine noTotal = BillLine.builder()
                .name("item")
                .quantity(1)
                .price(BigDecimal.valueOf(10))
                .bill(bill)
                .build();
        assertThatThrownBy(() -> billLineRepository.saveAndFlush(noTotal))
                .isInstanceOf(ConstraintViolationException.class);

        BillLine noBill = BillLine.builder()
                .name("item")
                .quantity(1)
                .price(BigDecimal.valueOf(10))
                .total(BigDecimal.valueOf(10))
                .build();
        assertThatThrownBy(() -> billLineRepository.saveAndFlush(noBill))
                .isInstanceOf(ConstraintViolationException.class);
    }

}
