package io.github.veron_santiago.backend.service.implementation;

import io.github.veron_santiago.backend.persistence.entity.Bill;
import io.github.veron_santiago.backend.persistence.entity.BillLine;
import io.github.veron_santiago.backend.persistence.entity.Company;
import io.github.veron_santiago.backend.persistence.entity.Customer;
import io.github.veron_santiago.backend.persistence.repository.IBillRepository;
import io.github.veron_santiago.backend.persistence.repository.ICompanyRepository;
import io.github.veron_santiago.backend.presentation.dto.request.BillRequest;
import io.github.veron_santiago.backend.presentation.dto.request.CustomerRequest;
import io.github.veron_santiago.backend.presentation.dto.response.BillDTO;
import io.github.veron_santiago.backend.presentation.dto.response.CustomerDTO;
import io.github.veron_santiago.backend.service.exception.ErrorMessages;
import io.github.veron_santiago.backend.service.exception.InvalidFieldException;
import io.github.veron_santiago.backend.service.exception.ObjectNotFoundException;
import io.github.veron_santiago.backend.service.interfaces.IBillLineService;
import io.github.veron_santiago.backend.service.interfaces.IBillService;
import io.github.veron_santiago.backend.service.interfaces.ICustomerService;
import io.github.veron_santiago.backend.util.AuthUtil;
import io.github.veron_santiago.backend.util.mapper.BillMapper;
import io.github.veron_santiago.backend.util.mapper.CustomerMapper;
import org.springframework.security.access.AccessDeniedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class BillServiceImpl implements IBillService {

    private final IBillRepository billRepository;
    private final ICompanyRepository companyRepository;
    private final BillMapper billMapper;
    private final AuthUtil authUtil;
    private final ICustomerService customerService;
    private final CustomerMapper customerMapper;
    private final IBillLineService billLineService;


    public BillServiceImpl(IBillRepository billRepository, ICompanyRepository companyRepository, BillMapper billMapper, AuthUtil authUtil, ICustomerService customerService, CustomerMapper customerMapper, IBillLineService billLineService) {
        this.billRepository = billRepository;
        this.companyRepository = companyRepository;
        this.billMapper = billMapper;
        this.authUtil = authUtil;
        this.customerService = customerService;
        this.customerMapper = customerMapper;
        this.billLineService = billLineService;
    }


    @Override
    public BillDTO createBill(BillRequest billRequest, HttpServletRequest request) throws IOException {
        Long companyId = authUtil.getAuthenticatedCompanyId(request);
        Company company = companyRepository.findById(companyId)
                .orElseThrow( () -> new ObjectNotFoundException(ErrorMessages.COMPANY_NOT_FOUND.getMessage()));

        if (billRequest.customerName() == null || billRequest.customerName().isBlank()) {
            throw new InvalidFieldException(ErrorMessages.CUSTOMER_NAME_IS_EMPTY.getMessage());
        }

        Customer customer = company.getCustomers().stream()
                .filter(c -> c.getName().equalsIgnoreCase(billRequest.customerName()))
                .findFirst()
                .orElseGet(() -> {
                    CustomerDTO dto = customerService.createCustomer(
                            new CustomerRequest(billRequest.customerName(), billRequest.customerEmail()), request);
                    return customerMapper.customerDTOToCustomer(dto, new Customer(), companyRepository, billRepository);
                });

        AtomicReference<BigDecimal> total = new AtomicReference<>(BigDecimal.ZERO);
        billRequest.billLineRequests()
                .forEach(req -> {
                    BigDecimal lineTotal = req.price().multiply(BigDecimal.valueOf(req.quantity()));
                    total.updateAndGet(t -> t.add(lineTotal));
                });


        Bill bill = Bill.builder()
                .billNumber((long) (company.getBills().size() + 1))
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))
                .totalAmount(BigDecimal.valueOf(Float.parseFloat(String.valueOf(total))))
                .customerName(billRequest.customerName())
                .customerEmail(billRequest.customerEmail())
                .company(company)
                .customer(customer)
                .build();

        Bill saved = billRepository.save(bill);

        List<BillLine> billLines = billRequest.billLineRequests()
                .stream()
                .map( billLineRequest -> {
                    try {
                        return billLineService.createBillLine(billLineRequest, bill, request);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();

        saved.setBillLines(billLines);
        Bill finalSaved = billRepository.save(saved);
        return billMapper.billToBillDTO(finalSaved, new BillDTO());
    }

    @Override
    public BillDTO getBillById(Long id, HttpServletRequest request){
        Long companyId = authUtil.getAuthenticatedCompanyId(request);
        Bill bill = billRepository.findById(id)
                .orElseThrow( () -> new ObjectNotFoundException(ErrorMessages.BILL_NOT_FOUND.getMessage()));

        if(!bill.getCompany().getId().equals(companyId)) throw new AccessDeniedException(ErrorMessages.ACCESS_DENIED_READ.getMessage());

        return billMapper.billToBillDTO(bill, new BillDTO());
    }

    @Override
    public List<BillDTO> getAllBills(HttpServletRequest request) {
        Long companyId = authUtil.getAuthenticatedCompanyId(request);

        List<Bill> bills = billRepository.findByCompanyId(companyId);

        return bills.stream()
                .map( bill -> billMapper.billToBillDTO(bill, new BillDTO()) )
                .collect(Collectors.toList());
    }


}
