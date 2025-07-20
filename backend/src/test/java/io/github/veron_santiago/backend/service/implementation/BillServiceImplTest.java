package io.github.veron_santiago.backend.service.implementation;

import io.github.veron_santiago.backend.persistence.entity.Bill;
import io.github.veron_santiago.backend.persistence.entity.BillLine;
import io.github.veron_santiago.backend.persistence.entity.Company;
import io.github.veron_santiago.backend.persistence.entity.Customer;
import io.github.veron_santiago.backend.persistence.repository.IBillRepository;
import io.github.veron_santiago.backend.persistence.repository.ICompanyRepository;
import io.github.veron_santiago.backend.presentation.dto.request.BillLineRequest;
import io.github.veron_santiago.backend.presentation.dto.request.BillRequest;
import io.github.veron_santiago.backend.presentation.dto.response.BillDTO;
import io.github.veron_santiago.backend.presentation.dto.response.CustomerDTO;
import io.github.veron_santiago.backend.service.exception.ErrorMessages;
import io.github.veron_santiago.backend.service.exception.InvalidFieldException;
import io.github.veron_santiago.backend.service.exception.ObjectNotFoundException;
import io.github.veron_santiago.backend.service.interfaces.IBillLineService;
import io.github.veron_santiago.backend.service.interfaces.ICustomerService;
import io.github.veron_santiago.backend.util.AuthUtil;
import io.github.veron_santiago.backend.util.mapper.BillMapper;
import io.github.veron_santiago.backend.util.mapper.CustomerMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillServiceImplTest {

    @InjectMocks
    private BillServiceImpl billService;

    @Mock private IBillRepository billRepository;
    @Mock private ICompanyRepository companyRepository;
    @Mock private BillMapper billMapper;
    @Mock private AuthUtil authUtil;
    @Mock private ICustomerService customerService;
    @Mock private CustomerMapper customerMapper;
    @Mock private IBillLineService billLineService;
    @Mock private HttpServletRequest request;

    private Long companyId;
    private BillRequest billRequest;
    private BillLineRequest line1;
    private BillLineRequest line2;
    private BillDTO expectedDto;
    private Company company;

    @BeforeEach
    void setUp() {
        companyId = 1L;
        when(authUtil.getAuthenticatedCompanyId(request)).thenReturn(companyId);

        line1 = new BillLineRequest("ProdA", "98422", 2, BigDecimal.valueOf(10));
        line2 = new BillLineRequest("ProdB", "41251", 3, BigDecimal.valueOf(5));

        billRequest = new BillRequest(
                "Cliente",
                "cliente@mail.com",
                List.of(line1, line2)
        );

        expectedDto = new BillDTO();
        company = new Company();
        company.setId(companyId);
        company.setBills(Collections.emptyList());
        company.setCustomers(Collections.emptySet());
    }

    @Test
    void createBill_successful() throws IOException {

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        CustomerDTO custDto = new CustomerDTO();
        when(customerService.createCustomer(any(), eq(request))).thenReturn(custDto);
        Customer createdCustomer = new Customer();
        when(customerMapper.customerDTOToCustomer(eq(custDto), any(), any(), any())).thenReturn(createdCustomer);

        BillLine bl1 = new BillLine();
        BillLine bl2 = new BillLine();
        when(billLineService.createBillLine(eq(line1), any(), eq(request))).thenReturn(bl1);
        when(billLineService.createBillLine(eq(line2), any(), eq(request))).thenReturn(bl2);
        when(billRepository.save(any(Bill.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(billMapper.billToBillDTO(any(Bill.class), any(BillDTO.class))).thenReturn(expectedDto);

        BillDTO result = billService.createBill(billRequest, request);
        assertSame(expectedDto, result);
        verify(billRepository, times(2)).save(any(Bill.class));
        ArgumentCaptor<Bill> billCaptor = ArgumentCaptor.forClass(Bill.class);
        verify(billRepository, atLeastOnce()).save(billCaptor.capture());
        Bill actualSavedBill = billCaptor.getValue();
        verify(billLineService).createBillLine(line1, actualSavedBill, request);
        verify(billLineService).createBillLine(line2, actualSavedBill, request);
    }

    @Test
    void createBill_companyNotFound_throws() {
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        ObjectNotFoundException ex = assertThrows(ObjectNotFoundException.class,
                () -> billService.createBill(billRequest, request));

        assertEquals(ErrorMessages.COMPANY_NOT_FOUND.getMessage(), ex.getMessage());
    }

    @Test
    void createBill_duplicateProducts_throws() {
        BillLineRequest dup1 = new BillLineRequest("x", "1234", 1, BigDecimal.ONE);
        BillLineRequest dup2 = new BillLineRequest("X", "4321", 1, BigDecimal.ONE);
        billRequest = new BillRequest("C", "c@mail", List.of(dup1, dup2));
        when(authUtil.getAuthenticatedCompanyId(request)).thenReturn(companyId);
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        InvalidFieldException ex = assertThrows(InvalidFieldException.class,
                () -> billService.createBill(billRequest, request));

        assertEquals(ErrorMessages.DUPLICATE_PRODUCT_IN_BILL.getMessage(), ex.getMessage());
    }

    @Test
    void createBill_emptyCustomerName_throws() {
        billRequest = new BillRequest("   ", "e@mail", List.of(line1));
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        InvalidFieldException ex = assertThrows(InvalidFieldException.class,
                () -> billService.createBill(billRequest, request));

        assertEquals(ErrorMessages.CUSTOMER_NAME_IS_EMPTY.getMessage(), ex.getMessage());
    }

    @Test
    void getBillById_successful() {
        Bill bill = new Bill();
        bill.setCompany(company);
        when(billRepository.findById(5L)).thenReturn(Optional.of(bill));
        when(billMapper.billToBillDTO(eq(bill), any(BillDTO.class))).thenReturn(expectedDto);

        BillDTO dto = billService.getBillById(5L, request);
        assertSame(expectedDto, dto);
    }

    @Test
    void getBillById_billNotFound_throws() {
        when(billRepository.findById(anyLong())).thenReturn(Optional.empty());

        ObjectNotFoundException ex = assertThrows(ObjectNotFoundException.class,
                () -> billService.getBillById(1L, request));

        assertEquals(ErrorMessages.BILL_NOT_FOUND.getMessage(), ex.getMessage());
    }

    @Test
    void getBillById_accessDenied_throws() {
        Company other = new Company();
        other.setId(2L);
        Bill bill = new Bill();
        bill.setCompany(other);
        when(billRepository.findById(3L)).thenReturn(Optional.of(bill));
        when(authUtil.getAuthenticatedCompanyId(request)).thenReturn(1L);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> billService.getBillById(3L, request));

        assertEquals(ErrorMessages.ACCESS_DENIED_READ.getMessage(), ex.getMessage());
    }

    @Test
    void getAllBills_returnsList() {
        Bill b1 = new Bill();
        Bill b2 = new Bill();
        b1.setCompany(company);
        b2.setCompany(company);
        List<Bill> bills = List.of(b1, b2);

        when(billRepository.findByCompanyId(companyId)).thenReturn(bills);
        BillDTO dto1 = new BillDTO();
        BillDTO dto2 = new BillDTO();
        when(billMapper.billToBillDTO(eq(b1), any(BillDTO.class))).thenReturn(dto1);
        when(billMapper.billToBillDTO(eq(b2), any(BillDTO.class))).thenReturn(dto2);

        List<BillDTO> result = billService.getAllBills(request);

        assertEquals(2, result.size());
        assertSame(dto1, result.get(0));
        assertSame(dto2, result.get(1));
    }

    @Test
    void getAllBills_emptyList() {
        when(billRepository.findByCompanyId(companyId)).thenReturn(Collections.emptyList());

        List<BillDTO> result = billService.getAllBills(request);

        assertTrue(result.isEmpty());
    }

}