package io.github.veron_santiago.backend.service.implementation;

import io.github.veron_santiago.backend.persistence.entity.*;
import io.github.veron_santiago.backend.persistence.repository.IBillRepository;
import io.github.veron_santiago.backend.persistence.repository.ICompanyRepository;
import io.github.veron_santiago.backend.persistence.repository.IProductRepository;
import io.github.veron_santiago.backend.presentation.dto.request.BillLineRequest;
import io.github.veron_santiago.backend.presentation.dto.request.BillRequest;
import io.github.veron_santiago.backend.presentation.dto.request.CustomerRequest;
import io.github.veron_santiago.backend.presentation.dto.response.BillDTO;
import io.github.veron_santiago.backend.presentation.dto.response.CustomerDTO;
import io.github.veron_santiago.backend.service.exception.ErrorMessages;
import io.github.veron_santiago.backend.service.exception.InvalidFieldException;
import io.github.veron_santiago.backend.service.exception.ObjectNotFoundException;
import io.github.veron_santiago.backend.service.exception.ResourceConflictException;
import io.github.veron_santiago.backend.service.interfaces.IBillLineService;
import io.github.veron_santiago.backend.service.interfaces.IBillService;
import io.github.veron_santiago.backend.service.interfaces.ICustomerService;
import io.github.veron_santiago.backend.service.interfaces.IPdfService;
import io.github.veron_santiago.backend.util.AuthUtil;
import io.github.veron_santiago.backend.util.mapper.BillMapper;
import io.github.veron_santiago.backend.util.mapper.CustomerMapper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.access.AccessDeniedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
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
    private final IPdfService pdfService;
    private final JavaMailSender javaMailSender;
    private final IProductRepository productRepository;

    public BillServiceImpl(IBillRepository billRepository, ICompanyRepository companyRepository, BillMapper billMapper, AuthUtil authUtil, ICustomerService customerService, CustomerMapper customerMapper, IBillLineService billLineService, IPdfService pdfService, JavaMailSender javaMailSender, IProductRepository productRepository) {
        this.billRepository = billRepository;
        this.companyRepository = companyRepository;
        this.billMapper = billMapper;
        this.authUtil = authUtil;
        this.customerService = customerService;
        this.customerMapper = customerMapper;
        this.billLineService = billLineService;
        this.pdfService = pdfService;
        this.javaMailSender = javaMailSender;
        this.productRepository = productRepository;
    }


    @Override
    public BillDTO createBill(BillRequest billRequest, HttpServletRequest request) throws IOException {

        Company company = getCompany(request);

        verifyEmptyCustomerName(billRequest);
        verifyDuplicateProductOrCode(billRequest, company.getId());

        AtomicReference<BigDecimal> total = calculateTotal(billRequest);
        Customer customer = getCustomerOrCreate(company, billRequest, request);

        Bill bill = Bill.builder()
                .billNumber((long) (company.getBills().size() + 1))
                .issueDate(LocalDate.now())
                .dueDate( billRequest.includeQr() ? LocalDate.now().plusDays(30) : null)
                .totalAmount(total.get())
                .companyName(company.getCompanyName())
                .companyEmail(company.getEmail())
                .companyAddress(company.getAddress())
                .customerName(billRequest.customerName())
                .customerEmail(billRequest.customerEmail())
                .customerAddress(billRequest.customerAddress())
                .company(company)
                .customer(customer)
                .build();

        Bill saved = billRepository.save(bill);

        List<BillLine> billLines = createBillLines(billRequest, saved, request);
        saved.setBillLines(new ArrayList<>(billLines));
        saved = billRepository.save(saved);

        String path = pdfService.generateBillPdf(saved, request, billRequest.includeQr());
        saved.setPdfPath(path);
        Bill finalSaved = billRepository.save(saved);

        String email = finalSaved.getCustomerEmail();
        if (billRequest.sendEmail() && email != null && !email.isEmpty()){
            byte[] pdf = pdfService.getPdfByBillId(saved.getId(), request);
            sendPdfToEmail(email, pdf, company.getCompanyName(), finalSaved.getId());
        }

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

    private Company getCompany(HttpServletRequest request){
        Long companyId = authUtil.getAuthenticatedCompanyId(request);
        return companyRepository.findById(companyId)
                .orElseThrow( () -> new ObjectNotFoundException(ErrorMessages.COMPANY_NOT_FOUND.getMessage()));
    }
    private void verifyDuplicateProductOrCode(BillRequest billRequest, Long companyId){
        Set<String> names = new HashSet<>();
        Set<String> codes = new HashSet<>();
        for (BillLineRequest line : billRequest.billLineRequests()) {
            String name = line.name().toLowerCase();
            String code = line.code();
            if (!names.add(name)) {
                throw new InvalidFieldException(ErrorMessages.DUPLICATE_PRODUCT_IN_BILL.getMessage());
            }
            if (code != null && !codes.add(code.toLowerCase())){
                throw new InvalidFieldException(ErrorMessages.DUPLICATE_CODE_IN_BILL.getMessage());
            }
            Product product = productRepository.findByCodeAndCompanyId(code, companyId).orElse(null);
            if (product != null && !product.getName().equalsIgnoreCase(name)){
                throw new ResourceConflictException("El código " + code + " ya está en uso.\nAsignado a: " + product.getName());
            }
        }
    }
    private void verifyEmptyCustomerName(BillRequest billRequest){
        if (billRequest.customerName() == null || billRequest.customerName().isBlank()) {
            throw new InvalidFieldException(ErrorMessages.CUSTOMER_NAME_IS_EMPTY.getMessage());
        }
    }
    private void sendPdfToEmail(String email, byte[] pdf, String companyName, Long billId){
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("Factura de " + companyName);
            helper.setText("Adjunto encontrarás tu factura en PDF.");

            ByteArrayResource resource = new ByteArrayResource(pdf);
            helper.addAttachment("factura-" + billId + ".pdf", resource);

            javaMailSender.send(message);
        } catch (MessagingException | MailException e) {
            throw new RuntimeException("Error al enviar el correo: " + e.getMessage(), e);
        }
    }

    private AtomicReference<BigDecimal> calculateTotal(BillRequest billRequest){
        AtomicReference<BigDecimal> total = new AtomicReference<>(BigDecimal.ZERO);
        billRequest.billLineRequests()
                .forEach(req -> {
                    BigDecimal lineTotal = req.price().multiply(BigDecimal.valueOf(req.quantity()));
                    total.updateAndGet(t -> t.add(lineTotal));
                });
        return total;
    }
    private Customer getCustomerOrCreate(Company company, BillRequest billRequest, HttpServletRequest request){
        return company.getCustomers().stream()
                .filter(c -> c.getName().equalsIgnoreCase(billRequest.customerName()))
                .findFirst()
                .orElseGet(() -> {
                    CustomerDTO dto = customerService.createCustomer(
                            new CustomerRequest(billRequest.customerName(), company.getAddress(), billRequest.customerEmail()), request);
                    return customerMapper.customerDTOToCustomer(dto, new Customer(), companyRepository, billRepository);
                });
    }
    private List<BillLine> createBillLines(BillRequest billRequest, Bill bill, HttpServletRequest request){
        return billRequest.billLineRequests()
                .stream()
                .map( billLineRequest -> {
                    try {
                        return billLineService.createBillLine(billLineRequest, bill, request);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();
    }

}
