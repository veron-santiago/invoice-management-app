package io.github.veron_santiago.backend.persistence.repository;

import io.github.veron_santiago.backend.persistence.entity.Bill;
import io.github.veron_santiago.backend.persistence.entity.Company;
import io.github.veron_santiago.backend.persistence.entity.Customer;
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

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
public class CustomerRepositoryTest {

    @Autowired
    private ICustomerRepository customerRepository;
    @Autowired
    private ICompanyRepository companyRepository;
    @Autowired
    private IBillRepository billRepository;
    @Autowired
    private EntityManager entityManager;

    private Company savedCompany;
    private Customer savedCustomer;

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

        customerRepository.deleteAll();
        savedCustomer= customerRepository.saveAndFlush(
                Customer.builder()
                        .name("cliente")
                        .email("cliente@mail.com")
                        .company(savedCompany)
                        .build()
        );
    }

    @Test
    void shouldSaveCustomerSuccessfully() {
        assertThat(savedCustomer.getId()).isNotNull();
        assertThat(savedCustomer.getName()).isEqualTo("cliente");
        assertThat(savedCustomer.getEmail()).isEqualTo("cliente@mail.com");
        assertThat(savedCustomer.getCompany().getId()).isEqualTo(savedCompany.getId());
    }

    @Test
    void shouldNotAllowDuplicateName() {
        Customer duplicate = Customer.builder()
                .name("cliente")
                .email("otro@mail.com")
                .company(savedCompany)
                .build();

        assertThatThrownBy(() -> customerRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldAllowDuplicateNameInDifferentCompanies() {
        Company otherCompany = companyRepository.saveAndFlush(
                Company.builder()
                        .companyName("otra")
                        .email("otra@mail.com")
                        .password("123")
                        .build()
        );

        Customer duplicateName = Customer.builder()
                .name("cliente")
                .email("nuevo@mail.com")
                .company(otherCompany)
                .build();

        Customer saved = customerRepository.saveAndFlush(duplicateName);
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void shouldNotAllowMissingRequiredFields() {
        Customer noName = Customer.builder()
                .email("x@mail.com")
                .company(savedCompany)
                .build();

        assertThatThrownBy(() -> customerRepository.saveAndFlush(noName))
                .isInstanceOf(ConstraintViolationException.class);

        Customer noCompany = Customer.builder()
                .name("sinEmpresa")
                .email("x@mail.com")
                .build();

        assertThatThrownBy(() -> customerRepository.saveAndFlush(noCompany))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void shouldNotAllowInvalidEmail() {
        Customer invalidEmail = Customer.builder()
                .name("cliente2")
                .email("invalido")
                .company(savedCompany)
                .build();

        assertThatThrownBy(() -> customerRepository.saveAndFlush(invalidEmail))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void findByCompanyIdReturnsCustomers() {
        List<Customer> customers = customerRepository.findByCompanyId(savedCompany.getId());
        assertThat(customers).hasSize(1);
        assertThat(customers.getFirst().getName()).isEqualTo("cliente");
    }

    @Test
    void deletingCustomerNullifiesCustomerFieldInBills() {
        assertThat(savedCustomer.getBills()).isNull();

        Bill bill = Bill.builder()
                .billNumber(1L)
                .issueDate(LocalDate.now())
                .totalAmount(BigDecimal.valueOf(100))
                .customerName("cliente")
                .company(savedCompany)
                .customer(savedCustomer)
                .build();

        billRepository.saveAndFlush(bill);
        entityManager.flush();
        entityManager.clear();
        savedCustomer = customerRepository.findById(savedCustomer.getId())
                .orElseThrow();
        assertThat(savedCustomer.getBills()).isNotNull();
        customerRepository.delete(savedCustomer);
        entityManager.flush();
        entityManager.clear();
        assertThat(billRepository.findById(bill.getId())).isNotNull();
    }


}
