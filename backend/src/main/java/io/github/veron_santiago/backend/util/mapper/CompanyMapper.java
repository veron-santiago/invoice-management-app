package io.github.veron_santiago.backend.util.mapper;

import io.github.veron_santiago.backend.persistence.entity.Bill;
import io.github.veron_santiago.backend.persistence.entity.Company;
import io.github.veron_santiago.backend.persistence.entity.Customer;
import io.github.veron_santiago.backend.persistence.entity.Product;
import io.github.veron_santiago.backend.persistence.repository.IBillRepository;
import io.github.veron_santiago.backend.persistence.repository.ICustomerRepository;
import io.github.veron_santiago.backend.persistence.repository.IProductRepository;
import io.github.veron_santiago.backend.presentation.dto.response.CompanyDTO;
import org.mapstruct.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CompanyMapper {

    @Mapping(target = "bills", ignore = true)
    @Mapping(target = "customers", ignore = true)
    @Mapping(target = "products", ignore = true)
    CompanyDTO companyToCompanyDTO(Company company,
                                   @MappingTarget CompanyDTO companyDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "bills", ignore = true)
    @Mapping(target = "customers", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "mpAccessToken", ignore = true)
    @Mapping(target = "mpRefreshToken", ignore = true)
    @Mapping(target = "mpTokenExpiration", ignore = true)
    Company companyDTOToCompany(CompanyDTO companyDTO,
                                @MappingTarget Company company,
                                @Context IBillRepository billRepository,
                                @Context ICustomerRepository customerRepository,
                                @Context IProductRepository productRepository);

    @AfterMapping
    default void afterCompanyToCompanyDTO(Company company,
                                          @MappingTarget CompanyDTO companyDTO){
        if (companyDTO.getBills() == null){
            companyDTO.setBills(
                    company.getBills() == null ? new ArrayList<>() :
                            company.getBills()
                                    .stream()
                                    .map(Bill::getId)
                                    .collect(Collectors.toList())
            );
        }
        if (companyDTO.getCustomers() == null){
            companyDTO.setCustomers(
                    company.getCustomers() == null ? new HashSet<>() :
                            company.getCustomers()
                                    .stream()
                                    .map(Customer::getId)
                                    .collect(Collectors.toSet())
            );
        }
        if (companyDTO.getProducts() == null){
            companyDTO.setProducts(
                    company.getProducts() == null ? new HashSet<>() :
                            company.getProducts()
                                    .stream()
                                    .map(Product::getId)
                                    .collect(Collectors.toSet())
            );
        }
    }

    @AfterMapping
    default void afterCompanyDTOToCompany(CompanyDTO companyDTO,
                                          @MappingTarget Company company,
                                          @Context IBillRepository billRepository,
                                          @Context ICustomerRepository customerRepository,
                                          @Context IProductRepository productRepository){
        if ( company.getBills() == null ){
            company.setBills(new ArrayList<>(billRepository.findAllById(companyDTO.getBills())));
        }
        if ( company.getCustomers() == null ){
            company.setCustomers(new HashSet<>(customerRepository.findAllById(companyDTO.getCustomers())));
        }
        if ( company.getProducts() == null ){
            company.setProducts(new HashSet<>(productRepository.findAllById(companyDTO.getProducts())));
        }
    }
}