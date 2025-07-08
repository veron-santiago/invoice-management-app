package io.github.veron_santiago.backend.persistence.repository;

import io.github.veron_santiago.backend.persistence.entity.Company;
import io.github.veron_santiago.backend.persistence.entity.Product;
import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;


@DataJpaTest
public class ProductRepositoryTest {

    @Autowired
    private IProductRepository productRepository;
    @Autowired
    private ICompanyRepository companyRepository;
    @Autowired
    private EntityManager entityManager;

    private Company savedCompany;
    private Product savedProduct;

    @BeforeEach
    void setUp() {
        companyRepository.deleteAll();
        savedCompany = companyRepository.saveAndFlush(
                Company.builder()
                        .companyName("compañia")
                        .email("email@mail.com")
                        .password("12345")
                        .build()
        );
        productRepository.deleteAll();
        savedProduct = productRepository.saveAndFlush(
                Product.builder()
                        .name("product")
                        .code("CODE1")
                        .price(BigDecimal.valueOf(10))
                        .company(savedCompany)
                        .build()
        );
    }

    @Test
    void shouldSaveProductSuccessfully() {
        assertThat(savedProduct.getId()).isNotNull();
        assertThat(savedProduct.getCode()).isEqualTo("CODE1");
        assertThat(savedProduct.getName()).isEqualTo("product");
        assertThat(savedProduct.getPrice()).isEqualByComparingTo("10.00");
        assertThat(savedProduct.getCompany().getId()).isEqualTo(savedCompany.getId());
    }

    @Test
    void shouldNotAllowDuplicateName() {
        Product dup = Product.builder()
                .code("CODE2")
                .name("product")
                .price(BigDecimal.valueOf(5))
                .company(savedCompany)
                .build();

        assertThatThrownBy(() -> productRepository.saveAndFlush(dup))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldAllowDuplicateNameInDifferentCompanies() {
        Company other = companyRepository.saveAndFlush(
                Company.builder()
                        .companyName("otra")
                        .email("otra@mail.com")
                        .password("pwd")
                        .build()
        );

        Product dup = Product.builder()
                .code("CODE3")
                .name("product")
                .price(BigDecimal.valueOf(20))
                .company(other)
                .build();

        Product saved = productRepository.saveAndFlush(dup);
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void shouldNotAllowDuplicateCode() {
        Product dup = Product.builder()
                .code("CODE1")
                .name("nuevo")
                .price(BigDecimal.valueOf(15))
                .company(savedCompany)
                .build();

        assertThatThrownBy(() -> productRepository.saveAndFlush(dup))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldAllowDuplicateCodeInDifferentCompanies() {
        Company other = companyRepository.saveAndFlush(
                Company.builder()
                        .companyName("otra2")
                        .email("otra2@mail.com")
                        .password("pwd2")
                        .build()
        );

        Product dup = Product.builder()
                .code("CODE1")
                .name("otroProducto")
                .price(BigDecimal.valueOf(25))
                .company(other)
                .build();

        Product saved = productRepository.saveAndFlush(dup);
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void shouldNotAllowMissingRequiredFields() {
        Product noName = Product.builder()
                .code("C10")
                .price(BigDecimal.valueOf(5))
                .company(savedCompany)
                .build();
        assertThatThrownBy(() -> productRepository.saveAndFlush(noName))
                .isInstanceOf(ConstraintViolationException.class);

        Product noPrice = Product.builder()
                .code("C11")
                .name("sinPrecio")
                .company(savedCompany)
                .build();
        assertThatThrownBy(() -> productRepository.saveAndFlush(noPrice))
                .isInstanceOf(ConstraintViolationException.class);

        Product noCompany = Product.builder()
                .code("C12")
                .name("sinEmpresa")
                .price(BigDecimal.valueOf(5))
                .build();
        assertThatThrownBy(() -> productRepository.saveAndFlush(noCompany))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void findByCompanyIdReturnsProducts() {
        List<Product> products = productRepository.findByCompanyId(savedCompany.getId());
        assertThat(products).hasSize(1);
        assertThat(products.getFirst().getName()).isEqualTo("product");
    }



}
