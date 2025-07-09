package io.github.veron_santiago.backend.util.mapper;

import io.github.veron_santiago.backend.persistence.entity.Bill;
import io.github.veron_santiago.backend.persistence.entity.Customer;
import io.github.veron_santiago.backend.persistence.repository.IBillRepository;
import io.github.veron_santiago.backend.persistence.repository.ICompanyRepository;
import io.github.veron_santiago.backend.presentation.dto.response.CustomerDTO;
import org.mapstruct.*;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CustomerMapper {

    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "bills", ignore = true)
    CustomerDTO customerToCustomerDTO(Customer customer,
                                      @MappingTarget CustomerDTO customerDTO);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "bills", ignore = true)
    Customer customerDTOToCustomer(CustomerDTO customerDTO,
                                   @MappingTarget Customer customer,
                                   @Context ICompanyRepository companyRepository,
                                   @Context IBillRepository billRepository);

    @AfterMapping
    default void afterCustomerToCustomerDTO(Customer customer,
                                            @MappingTarget CustomerDTO customerDTO){
        if (customerDTO.getCompanyId() == null){
            customerDTO.setCompanyId(customer.getCompany().getId());
        }
        if (customerDTO.getBills() == null){
            if (customer.getBills() == null) customerDTO.setBills(new ArrayList<>());
            else customerDTO.setBills(
                    customer.getBills()
                            .stream()
                            .map(Bill::getId)
                            .collect(Collectors.toList())
            );
        }
    }

    @AfterMapping
    default void afterCustomerDTOToCustomer(CustomerDTO customerDTO,
                                            @MappingTarget Customer customer,
                                            @Context ICompanyRepository companyRepository,
                                            @Context IBillRepository billRepository){
        if (customer.getCompany() == null){
            customer.setCompany(companyRepository.findById(customerDTO.getCompanyId()).orElse(null));
        }
        if (customer.getBills() == null){
            customer.setBills(new ArrayList<>(billRepository.findAllById(customerDTO.getBills())));
        }
    }

}