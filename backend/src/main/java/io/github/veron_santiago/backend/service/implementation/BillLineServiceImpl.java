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
import io.github.veron_santiago.backend.service.interfaces.IBillLineService;
import io.github.veron_santiago.backend.util.AuthUtil;
import io.github.veron_santiago.backend.util.mapper.BillLineMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class BillLineServiceImpl implements IBillLineService {

    private final AuthUtil authUtil;
    private final ICompanyRepository companyRepository;
    private final IBillLineRepository billLineRepository;
    private final IBillRepository billRepository;
    private final BillLineMapper billLineMapper;

    public BillLineServiceImpl(AuthUtil authUtil,
                               ICompanyRepository companyRepository,
                               IBillLineRepository billLineRepository,
                               IBillRepository billRepository,
                               BillLineMapper billLineMapper) {
        this.authUtil = authUtil;
        this.companyRepository = companyRepository;
        this.billLineRepository = billLineRepository;
        this.billRepository = billRepository;
        this.billLineMapper = billLineMapper;
    }


    @Override
    public BillLine createBillLine(BillLineRequest billLineRequest, Bill bill, HttpServletRequest request) throws IOException {
        Long companyId = authUtil.getAuthenticatedCompanyId(request);
        if (!bill.getCompany().getId().equals(companyId)) {
            throw new AccessDeniedException(ErrorMessages.ACCESS_DENIED_UPDATE.getMessage());
        }

        String name = billLineRequest.name();
        String code = billLineRequest.code();
        int quantity = billLineRequest.quantity();
        BigDecimal price = billLineRequest.price();
        BigDecimal total = price.multiply(BigDecimal.valueOf(quantity));
        Product product = getProductByName(name, request);


        BillLine billLine = BillLine.builder()
                .code(code)
                .name(name)
                .quantity(quantity)
                .price(price)
                .total(total)
                .bill(bill)
                .product(product)
                .build();

        return billLineRepository.save(billLine);
    }

    @Override
    public BillLineDTO getBillLineById(Long id, HttpServletRequest request) {
        Long companyId = authUtil.getAuthenticatedCompanyId(request);
        BillLine billLine = billLineRepository.findById(id).orElseThrow(() -> new ObjectNotFoundException(ErrorMessages.BILL_LINE_NOT_FOUND.getMessage()));
        if (!billLine.getBill().getCompany().getId().equals(companyId)) throw new AccessDeniedException(ErrorMessages.ACCESS_DENIED_READ.getMessage());
        return billLineMapper.billLineToBillLineDTO(billLine, new BillLineDTO());
    }

    @Override
    public List<BillLineDTO> getAllBillLinesByBillId(Long id, HttpServletRequest request) {
        Bill bill = billRepository.findById(id).orElseThrow(() -> new ObjectNotFoundException(ErrorMessages.BILL_NOT_FOUND.getMessage()));
        Long companyId = authUtil.getAuthenticatedCompanyId(request);
        if (!bill.getCompany().getId().equals(companyId)) throw new AccessDeniedException(ErrorMessages.ACCESS_DENIED_READ.getMessage());
        return bill.getBillLines()
                .stream()
                .map(billLine -> billLineMapper.billLineToBillLineDTO(billLine, new BillLineDTO()))
                .collect(Collectors.toList());
    }

    private Product getProductByName(String name, HttpServletRequest request){
        Long companyId = authUtil.getAuthenticatedCompanyId(request);
        Company company = companyRepository.findById(companyId).orElseThrow(() -> new ObjectNotFoundException(ErrorMessages.COMPANY_NOT_FOUND.getMessage()));
        return company.getProducts()
                                    .stream()
                                    .filter( product -> product.getName().equalsIgnoreCase(name))
                                    .findFirst()
                                    .orElse(null);
    }

}
