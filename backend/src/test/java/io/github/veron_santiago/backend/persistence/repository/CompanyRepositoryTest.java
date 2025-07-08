package io.github.veron_santiago.backend.persistence.repository;

import io.github.veron_santiago.backend.persistence.entity.*;
import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
public class CompanyRepositoryTest {

    @Autowired
    private ICompanyRepository companyRepository;
    @Autowired
    private IBillRepository billRepository;
    @Autowired
    private IProductRepository productRepository;
    @Autowired
    private ICustomerRepository customerRepository;
    @Autowired
    private EntityManager entityManager;

    private Company savedCompany;

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
    }

    @Test
    void shouldSaveCompanySuccessfully(){
        assertThat(savedCompany.getId()).isNotNull();
        assertThat(savedCompany.getCompanyName()).isEqualTo("compañia");
        assertThat(savedCompany.getEmail()).isEqualTo("email@mail.com");
        assertThat(savedCompany.isVerified()).isFalse();
    }

    @Test
    void shouldNotAllowDuplicateCompanyName(){
        Company company2 = Company.builder()
                .companyName("compañia")
                .email("hola@mail.com")
                .password("123")
                .build();

        assertThatThrownBy(() -> companyRepository.saveAndFlush(company2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldNotAllowDuplicateEmail() {
        Company company2 = Company.builder()
                .companyName("compañia2")
                .email("email@mail.com")
                .password("123")
                .build();

        assertThatThrownBy(() -> companyRepository.saveAndFlush(company2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldNotAllowMissingRequiredFields() {
        Company missingName = Company.builder()
                .email("x@mail.com")
                .password("123")
                .build();

        assertThatThrownBy(() -> companyRepository.saveAndFlush(missingName))
                .isInstanceOf(ConstraintViolationException.class);

        Company missingPassword = Company.builder()
                .companyName("compañia")
                .email("x@mail.com")
                .build();

        assertThatThrownBy(() -> companyRepository.saveAndFlush(missingPassword))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void shouldNotAllowInvalidEmail() {
        Company invalidEmail = Company.builder()
                .companyName("compañia")
                .email("email")
                .password("123")
                .build();

        assertThatThrownBy(() -> companyRepository.saveAndFlush(invalidEmail))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void shouldCascadePersistAndDeleteRelations() {
        Company company = Company.builder()
                .companyName("CascadeCompany")
                .email("cascade@mail.com")
                .password("123")
                .build();

        Bill bill = Bill.builder()
                .billNumber(1L)
                .issueDate(LocalDate.now())
                .totalAmount(new BigDecimal("100.00"))
                .customerName("Client X")
                .company(company)
                .build();

        Customer customer = Customer.builder()
                .name("Client")
                .company(company)
                .build();

        Product product = Product.builder()
                .name("Product")
                .price(new BigDecimal("20.00"))
                .company(company)
                .build();

        company.setBills(List.of(bill));
        company.setCustomers(Set.of(customer));
        company.setProducts(Set.of(product));

        Company saved = companyRepository.saveAndFlush(company);
        Long id = saved.getId();

        companyRepository.deleteById(id);
        companyRepository.flush();

        assertThat(companyRepository.findById(id)).isEmpty();
    }

    @Test
    void findByCompanyNameReturnsCompany() {
        Optional<Company> found = companyRepository.findByCompanyName("compañia");
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("email@mail.com");
    }

    @Test
    void findByEmailReturnsCompany() {
        Optional<Company> found = companyRepository.findByEmail("email@mail.com");
        assertThat(found).isPresent();
        assertThat(found.get().getCompanyName()).isEqualTo("compañia");
    }

    @Test
    void findByCompanyNameOrEmailReturnsCompany() {
        Optional<Company> foundByName = companyRepository.findByCompanyNameOrEmail("compañia");
        Optional<Company> foundByEmail = companyRepository.findByCompanyNameOrEmail("email@mail.com");
        assertThat(foundByName).isPresent();
        assertThat(foundByEmail).isPresent();
        assertThat(foundByName.get().getId()).isEqualTo(savedCompany.getId());
        assertThat(foundByEmail.get().getId()).isEqualTo(savedCompany.getId());
    }

    @Test
    void findEmailByCompanyNameReturnsEmail() {
        Optional<String> email = companyRepository.findEmailByCompanyName("compañia");
        assertThat(email).isPresent();
        assertThat(email.get()).isEqualTo("email@mail.com");
    }

    @Test
    void existsByCompanyNameReturnsTrueIfExists() {
        assertThat(companyRepository.existsByCompanyName("compañia")).isTrue();
        assertThat(companyRepository.existsByCompanyName("otra")).isFalse();
    }

    @Test
    void existsByEmailReturnsTrueIfExists() {
        assertThat(companyRepository.existsByEmail("email@mail.com")).isTrue();
        assertThat(companyRepository.existsByEmail("otra@mail.com")).isFalse();
    }

    @Test
    void findIdByCompanyNameReturnsId() {
        Optional<Long> id = companyRepository.findIdByCompanyName("compañia");
        assertThat(id).isPresent();
        assertThat(id.get()).isEqualTo(savedCompany.getId());
    }

    @Test
    void findIdByEmailReturnsId() {
        Optional<Long> id = companyRepository.findIdByEmail("email@mail.com");
        assertThat(id).isPresent();
        assertThat(id.get()).isEqualTo(savedCompany.getId());
    }

    @Test
    void deletingCompanyDeletesBills(){
        assertThat(savedCompany.getBills()).isNull();

        Bill bill = Bill.builder()
                .billNumber(1L)
                .issueDate(LocalDate.now())
                .customerName("hola")
                .totalAmount(BigDecimal.valueOf(100))
                .company(savedCompany)
                .build();

        billRepository.save(bill);
        entityManager.flush();
        entityManager.clear();
        savedCompany = companyRepository.findById(savedCompany.getId())
                .orElseThrow();
        assertThat(savedCompany.getBills()).isNotNull();
        companyRepository.delete(savedCompany);
        assertThat(billRepository.findById(bill.getId())).isEmpty();

    }

    @Test
    void deletingCompanyDeletesCustomers(){
        assertThat(savedCompany.getCustomers()).isNull();

        Customer customer = Customer.builder()
                .name("customer")
                .company(savedCompany)
                .build();

        customerRepository.save(customer);
        entityManager.flush();
        entityManager.clear();
        savedCompany = companyRepository.findById(savedCompany.getId())
                .orElseThrow();
        assertThat(savedCompany.getCustomers()).isNotNull();
        companyRepository.delete(savedCompany);
        assertThat(customerRepository.findById(customer.getId())).isEmpty();
    }

    @Test
    void deletingCompanyDeletesProducts(){
        assertThat(savedCompany.getProducts()).isNull();

        Product product = Product.builder()
                .name("product")
                .price(BigDecimal.valueOf(100))
                .company(savedCompany)
                .build();

        productRepository.save(product);
        entityManager.flush();
        entityManager.clear();
        savedCompany = companyRepository.findById(savedCompany.getId())
                .orElseThrow();
        assertThat(savedCompany.getProducts()).isNotNull();
        companyRepository.delete(savedCompany);
        assertThat(productRepository.findById(product.getId())).isEmpty();
    }

}
