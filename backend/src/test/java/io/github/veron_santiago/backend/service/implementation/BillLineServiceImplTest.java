package io.github.veron_santiago.backend.service.implementation;

import io.github.veron_santiago.backend.persistence.entity.Bill;
import io.github.veron_santiago.backend.persistence.entity.BillLine;
import io.github.veron_santiago.backend.persistence.entity.Company;
import io.github.veron_santiago.backend.persistence.entity.Product;
import io.github.veron_santiago.backend.persistence.repository.IBillLineRepository;
import io.github.veron_santiago.backend.persistence.repository.IBillRepository;
import io.github.veron_santiago.backend.persistence.repository.ICompanyRepository;
import io.github.veron_santiago.backend.presentation.dto.request.BillLineRequest;
import io.github.veron_santiago.backend.presentation.dto.response.BillLineDTO;
import io.github.veron_santiago.backend.service.exception.ErrorMessages;
import io.github.veron_santiago.backend.service.exception.ObjectNotFoundException;
import io.github.veron_santiago.backend.util.AuthUtil;
import io.github.veron_santiago.backend.util.mapper.BillLineMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class BillLineServiceImplTest {

    @InjectMocks private BillLineServiceImpl billLineService;
    @Mock private AuthUtil authUtil;
    @Mock private ICompanyRepository companyRepository;
    @Mock private IBillLineRepository billLineRepository;
    @Mock private IBillRepository billRepository;
    @Mock private BillLineMapper billLineMapper;
    @Mock private HttpServletRequest request;

    private Long companyId;
    private Company company;
    private Bill bill;

    @BeforeEach
    void setUp() {
        companyId = 1L;
        company = new Company();
        company.setId(companyId);

        bill = new Bill();
        bill.setId(10L);
        bill.setCompany(company);

        when(authUtil.getAuthenticatedCompanyId(request)).thenReturn(companyId);
    }

    @Test
    void createBillLine_successful_withProduct() throws IOException {
        Product product = Product.builder().id(5L).name("Producto A").build();
        company.setProducts(Set.of(product));

        BillLineRequest req = new BillLineRequest("Producto A", "001", 2, new BigDecimal("100.00"));

        BillLine expected = BillLine.builder()
                .code("001")
                .name("Producto A")
                .quantity(2)
                .price(new BigDecimal("100.00"))
                .total(new BigDecimal("200.00"))
                .bill(bill)
                .product(product)
                .build();

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(billLineRepository.save(any())).thenReturn(expected);

        BillLine res = billLineService.createBillLine(req, bill, request);

        assertEquals("001", res.getCode());
        assertEquals("Producto A", res.getName());
        assertEquals(new BigDecimal("200.00"), res.getTotal());
        assertNotNull(res.getProduct());
    }

    @Test
    void createBillLine_successful_withoutProduct() throws IOException {
        company.setProducts(Set.of());

        BillLineRequest req = new BillLineRequest("Producto Inexistente", "002", 1, new BigDecimal("50.00"));

        BillLine expected = BillLine.builder()
                .code("002")
                .name("Producto Inexistente")
                .quantity(1)
                .price(new BigDecimal("50.00"))
                .total(new BigDecimal("50.00"))
                .bill(bill)
                .product(null)
                .build();

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(billLineRepository.save(any())).thenReturn(expected);

        BillLine res = billLineService.createBillLine(req, bill, request);

        assertEquals("002", res.getCode());
        assertEquals("Producto Inexistente", res.getName());
        assertEquals(new BigDecimal("50.00"), res.getTotal());
        assertNull(res.getProduct());
    }

    @Test
    void createBillLine_accessDenied_throws() {
        Company otherCompany = new Company();
        otherCompany.setId(99L);
        bill.setCompany(otherCompany);

        BillLineRequest req = new BillLineRequest("Prod", "999", 1, new BigDecimal("1"));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> billLineService.createBillLine(req, bill, request));

        assertEquals(ErrorMessages.ACCESS_DENIED_UPDATE.getMessage(), ex.getMessage());
    }

    @Test
    void getBillLineById_successful() {
        BillLine line = BillLine.builder().id(1L).name("Linea").bill(bill).build();
        BillLineDTO dto = new BillLineDTO();
        dto.setName("Linea");

        when(billLineRepository.findById(1L)).thenReturn(Optional.of(line));
        when(billLineMapper.billLineToBillLineDTO(eq(line), any(BillLineDTO.class))).thenReturn(dto);

        BillLineDTO res = billLineService.getBillLineById(1L, request);

        assertEquals("Linea", res.getName());
    }

    @Test
    void getBillLineById_notFound_throws() {
        when(billLineRepository.findById(1L)).thenReturn(Optional.empty());

        ObjectNotFoundException ex = assertThrows(ObjectNotFoundException.class,
                () -> billLineService.getBillLineById(1L, request));

        assertEquals(ErrorMessages.BILL_LINE_NOT_FOUND.getMessage(), ex.getMessage());
    }

    @Test
    void getBillLineById_accessDenied_throws() {
        Company other = new Company(); other.setId(999L);
        Bill otherBill = new Bill(); otherBill.setCompany(other);
        BillLine line = BillLine.builder().id(2L).bill(otherBill).build();

        when(billLineRepository.findById(2L)).thenReturn(Optional.of(line));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> billLineService.getBillLineById(2L, request));

        assertEquals(ErrorMessages.ACCESS_DENIED_READ.getMessage(), ex.getMessage());
    }

    @Test
    void getAllBillLinesByBillId_successful() {
        BillLine l1 = BillLine.builder().id(1L).name("L1").build();
        BillLine l2 = BillLine.builder().id(2L).name("L2").build();
        bill.setBillLines(List.of(l1, l2));

        BillLineDTO dto1 = new BillLineDTO();
        dto1.setName("L1");
        BillLineDTO dto2 = new BillLineDTO();
        dto2.setName("L2");

        when(billRepository.findById(bill.getId())).thenReturn(Optional.of(bill));
        when(billLineMapper.billLineToBillLineDTO(eq(l1), any(BillLineDTO.class))).thenReturn(dto1);
        when(billLineMapper.billLineToBillLineDTO(eq(l2), any(BillLineDTO.class))).thenReturn(dto2);

        List<BillLineDTO> result = billLineService.getAllBillLinesByBillId(bill.getId(), request);

        assertEquals(2, result.size());
        assertEquals("L1", result.get(0).getName());
        assertEquals("L2", result.get(1).getName());
    }

    @Test
    void getAllBillLinesByBillId_notFound_throws() {
        when(billRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());

        ObjectNotFoundException ex = assertThrows(ObjectNotFoundException.class,
                () -> billLineService.getAllBillLinesByBillId(1L, request));

        assertEquals(ErrorMessages.BILL_NOT_FOUND.getMessage(), ex.getMessage());
    }

    @Test
    void getAllBillLinesByBillId_accessDenied_throws() {
        Company other = new Company(); other.setId(999L);
        Bill otherBill = new Bill(); otherBill.setId(123L); otherBill.setCompany(other);

        when(billRepository.findById(otherBill.getId())).thenReturn(Optional.of(otherBill));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> billLineService.getAllBillLinesByBillId(otherBill.getId(), request));

        assertEquals(ErrorMessages.ACCESS_DENIED_READ.getMessage(), ex.getMessage());
    }


}
