package io.github.veron_santiago.backend.util.mapper;

import io.github.veron_santiago.backend.persistence.entity.Bill;
import io.github.veron_santiago.backend.persistence.repository.ICompanyRepository;
import io.github.veron_santiago.backend.persistence.repository.ICustomerRepository;
import io.github.veron_santiago.backend.presentation.dto.response.BillDTO;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BillMapper {

    @Mapping(target = "customerId", ignore = true)
    @Mapping(target = "companyId", ignore = true)
    BillDTO billToBillDTO(Bill bill,
                          @MappingTarget BillDTO billDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "company", ignore = true)
    Bill billDTOToBill(BillDTO billDTO,
                       @MappingTarget Bill bill,
                       @Context ICustomerRepository customerRepository,
                       @Context ICompanyRepository companyRepository);

    @AfterMapping
    default void afterBillToBillDTO(Bill bill,
                                    @MappingTarget BillDTO billDTO){
        if (bill.getCustomer() != null){
            billDTO.setCustomerId(bill.getCustomer().getId());
        }
        billDTO.setCustomerName(bill.getCustomerName());
        billDTO.setCustomerEmail(bill.getCustomerEmail());
        billDTO.setCompanyId(bill.getCompany().getId());
    }

    @AfterMapping
    default void afterBillDTOToBill(BillDTO billDTO,
                                    @MappingTarget Bill bill,
                                    @Context ICustomerRepository customerRepository,
                                    @Context ICompanyRepository companyRepository){
        if (bill.getCustomer() == null && billDTO.getCustomerId() != null){
            bill.setCustomer(customerRepository.findById(billDTO.getCustomerId()).orElse(null));
        }
        if (bill.getCompany() == null){
            bill.setCompany(companyRepository.findById(billDTO.getCompanyId()).orElse(null));
        }
    }
}