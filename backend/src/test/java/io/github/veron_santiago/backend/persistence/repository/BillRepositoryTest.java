package io.github.veron_santiago.backend.persistence.repository;

import io.github.veron_santiago.backend.persistence.entity.Bill;
import io.github.veron_santiago.backend.persistence.entity.BillLine;
import io.github.veron_santiago.backend.persistence.entity.Company;
import io.github.veron_santiago.backend.persistence.entity.Customer;
import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@DataJpaTest
public class BillRepositoryTest {

    @Autowired
    private IBillRepository billRepository;
    @Autowired
    private IBillLineRepository billLineRepository;
    @Autowired
    private ICompanyRepository companyRepository;
    @Autowired
    private ICustomerRepository customerRepository;
    @Autowired
    private EntityManager entityManager;

    private Company savedCompany;
    private Customer savedCustomer;
    private Bill savedBill;

    @BeforeEach
    void setUp() {
        billLineRepository.deleteAll();
        billRepository.deleteAll();
        customerRepository.deleteAll();
        companyRepository.deleteAll();

        savedCompany = companyRepository.saveAndFlush(
                Company.builder()
                        .companyName("compañia")
                        .email("email@mail.com")
                        .password("12345")
                        .build()
        );

        savedCustomer = customerRepository.saveAndFlush(
                Customer.builder()
                        .name("cliente")
                        .email("cliente@mail.com")
                        .company(savedCompany)
                        .build()
        );

        savedBill = billRepository.saveAndFlush(
                Bill.builder()
                        .billNumber(100L)
                        .issueDate(LocalDate.now())
                        .totalAmount(BigDecimal.valueOf(150.75))
                        .customerName(savedCustomer.getName())
                        .customerEmail(savedCustomer.getEmail())
                        .company(savedCompany)
                        .customer(savedCustomer)
                        .build()
        );
    }

    @Test
    void shouldSaveBillSuccessfully() {
        assertThat(savedBill.getId()).isNotNull();
        assertThat(savedBill.getBillNumber()).isEqualTo(100L);
        assertThat(savedBill.getIssueDate()).isEqualTo(LocalDate.now());
        assertThat(savedBill.getTotalAmount()).isEqualByComparingTo("150.75");
        assertThat(savedBill.getCustomerName()).isEqualTo("cliente");
        assertThat(savedBill.getCustomerEmail()).isEqualTo("cliente@mail.com");
        assertThat(savedBill.getCompany().getId()).isEqualTo(savedCompany.getId());
        assertThat(savedBill.getCustomer().getId()).isEqualTo(savedCustomer.getId());
    }

    @Test
    void shouldNotAllowDuplicateBillNumber() {
        Bill dup = Bill.builder()
                .billNumber(100L)
                .issueDate(LocalDate.now())
                .totalAmount(BigDecimal.valueOf(50))
                .customerName("otro")
                .company(savedCompany)
                .build();

        assertThatThrownBy(() -> billRepository.saveAndFlush(dup))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldAllowDuplicateBillNumberInDifferentCompanies() {
        Company other = companyRepository.saveAndFlush(
                Company.builder()
                        .companyName("otra")
                        .email("otra@mail.com")
                        .password("pwd")
                        .build()
        );

        Bill dup = Bill.builder()
                .billNumber(100L)
                .issueDate(LocalDate.now())
                .totalAmount(BigDecimal.valueOf(200))
                .customerName("clienteX")
                .company(other)
                .build();

        Bill saved = billRepository.saveAndFlush(dup);
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void shouldNotAllowMissingRequiredFields() {
        Bill noNumber = Bill.builder()
                .issueDate(LocalDate.now())
                .totalAmount(BigDecimal.valueOf(10))
                .customerName("c")
                .company(savedCompany)
                .build();
        assertThatThrownBy(() -> billRepository.saveAndFlush(noNumber))
                .isInstanceOf(ConstraintViolationException.class);

        Bill noDate = Bill.builder()
                .billNumber(101L)
                .totalAmount(BigDecimal.valueOf(10))
                .customerName("c")
                .company(savedCompany)
                .build();
        assertThatThrownBy(() -> billRepository.saveAndFlush(noDate))
                .isInstanceOf(ConstraintViolationException.class);

        Bill noTotal = Bill.builder()
                .billNumber(102L)
                .issueDate(LocalDate.now())
                .customerName("c")
                .company(savedCompany)
                .build();
        assertThatThrownBy(() -> billRepository.saveAndFlush(noTotal))
                .isInstanceOf(ConstraintViolationException.class);

        Bill noName = Bill.builder()
                .billNumber(103L)
                .issueDate(LocalDate.now())
                .totalAmount(BigDecimal.valueOf(10))
                .company(savedCompany)
                .build();
        assertThatThrownBy(() -> billRepository.saveAndFlush(noName))
                .isInstanceOf(ConstraintViolationException.class);

        Bill noCompany = Bill.builder()
                .billNumber(104L)
                .issueDate(LocalDate.now())
                .totalAmount(BigDecimal.valueOf(10))
                .customerName("c")
                .build();
        assertThatThrownBy(() -> billRepository.saveAndFlush(noCompany))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void findByCompanyIdReturnsBills() {
        List<Bill> bills = billRepository.findByCompanyId(savedCompany.getId());
        assertThat(bills).hasSize(1);
        assertThat(bills.getFirst().getBillNumber()).isEqualTo(100L);
    }

    @Test
    void findByCustomerIdReturnsBills() {
        List<Bill> bills = billRepository.findByCustomerId(savedCustomer.getId());
        assertThat(bills).hasSize(1);
        assertThat(bills.getFirst().getCustomerName()).isEqualTo("cliente");
    }

    @Test
    void deletingBillCascadesBillLines() {
        BillLine line = billLineRepository.saveAndFlush(
                BillLine.builder()
                        .name("product")
                        .quantity(2)
                        .price(BigDecimal.valueOf(10))
                        .total(BigDecimal.valueOf(20))
                        .bill(savedBill)
                        .build()
        );
        entityManager.flush();
        entityManager.clear();

        billRepository.deleteById(savedBill.getId());
        entityManager.flush();
        entityManager.clear();

        assertThat(billLineRepository.findById(line.getId())).isEmpty();
    }


}
