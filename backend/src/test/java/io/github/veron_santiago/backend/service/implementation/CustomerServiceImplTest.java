package io.github.veron_santiago.backend.service.implementation;

import io.github.veron_santiago.backend.persistence.entity.Company;
import io.github.veron_santiago.backend.persistence.entity.Customer;
import io.github.veron_santiago.backend.persistence.repository.ICompanyRepository;
import io.github.veron_santiago.backend.persistence.repository.ICustomerRepository;
import io.github.veron_santiago.backend.presentation.dto.request.CustomerRequest;
import io.github.veron_santiago.backend.presentation.dto.response.CustomerDTO;
import io.github.veron_santiago.backend.presentation.dto.update.CustomerUpdateRequest;
import io.github.veron_santiago.backend.service.exception.ErrorMessages;
import io.github.veron_santiago.backend.service.exception.ObjectNotFoundException;
import io.github.veron_santiago.backend.util.AuthUtil;
import io.github.veron_santiago.backend.util.mapper.CustomerMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceImplTest {

    @InjectMocks private CustomerServiceImpl customerService;
    @Mock private ICustomerRepository customerRepository;
    @Mock private ICompanyRepository companyRepository;
    @Mock private CustomerMapper customerMapper;
    @Mock private AuthUtil authUtil;
    @Mock private HttpServletRequest request;

    Long companyId;
    CustomerRequest customerRequest;
    CustomerDTO expectedDto;

    @BeforeEach
    void setUp(){

        companyId = 1L;

        when(authUtil.getAuthenticatedCompanyId(request)).thenReturn(companyId);

/*
        customerRequest = new CustomerRequest("Juan","juan@mail.com");
        expectedDto = new CustomerDTO(1L, "Juan", "juan@mail.com", companyId, new ArrayList<>());
*/

    }


    /*@Test
    void createCustomer_successful() {
        Company company = new Company();
        company.setId(companyId);

        Customer savedCustomer = Customer.builder()
                .id(1L)
                .name("Juan")
                .email("juan@mail.com")
                .company(company)
                .build();

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);
        when(customerMapper.customerToCustomerDTO(eq(savedCustomer), any(CustomerDTO.class))).thenReturn(expectedDto);

        CustomerDTO result = customerService.createCustomer(customerRequest, request);

        assertEquals("Juan", result.getName());
        assertEquals("juan@mail.com", result.getEmail());
        assertEquals(1L, result.getId());
    }

    @Test
    void createCustomer_companyNotFound_throws() {
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        ObjectNotFoundException e = assertThrows(ObjectNotFoundException.class,
                () -> customerService.createCustomer(customerRequest, request));

        assertEquals(ErrorMessages.COMPANY_NOT_FOUND.getMessage(), e.getMessage());
    }

    @Test
    void getCustomerById_successful(){
        Company company = new Company();
                company.setId(companyId);

        Customer customer = new Customer(1L, "Juan","juan@mail.com", company, new ArrayList<>());

        when( customerRepository.findById(1L) ).thenReturn(Optional.of(customer));
        when(customerMapper.customerToCustomerDTO(eq(customer), any(CustomerDTO.class)))
                .thenReturn(expectedDto);

        CustomerDTO res = customerService.getCustomerById(1L, request);
        assertEquals(1L, res.getId());
        assertEquals("Juan", res.getName());
        assertEquals("juan@mail.com", res.getEmail());
        assertEquals(1L, res.getCompanyId());
        assertEquals(new ArrayList<>(), res.getBills());
    }

    @Test
    void getCustomerById_customerNotFound_throws(){
        when(customerRepository.findById(anyLong())).thenReturn(Optional.empty());

        ObjectNotFoundException e = assertThrows(ObjectNotFoundException.class,
                () -> customerService.getCustomerById(1L, request));

        assertEquals(ErrorMessages.CUSTOMER_NOT_FOUND.getMessage(), e.getMessage());
    }

    @Test
    void getCustomerById_accessDenied_throws(){
        Company otherCompany = new Company();
        otherCompany.setId(6L);

        Customer savedCustomer = Customer.builder()
                .id(2L)
                .name("Hola")
                .email("hola@mail.com")
                .company(otherCompany)
                .build();

        when(customerRepository.findById(savedCustomer.getId())).thenReturn(Optional.of(savedCustomer));

        AccessDeniedException e = assertThrows(AccessDeniedException.class,
                () -> customerService.getCustomerById(savedCustomer.getId(), request));

        assertEquals(ErrorMessages.ACCESS_DENIED_READ.getMessage(), e.getMessage());
    }

    @Test
    void getAllCustomers_returnsList() {
        Company company = new Company();
        company.setId(companyId);
        Customer c1 = new Customer(1L, "Juan", "juan@mail.com", company, new ArrayList<>());
        Customer c2 = new Customer(2L, "Ana", "ana@mail.com", company, new ArrayList<>());
        List<Customer> customers = List.of(c1, c2);

        when(customerRepository.findByCompanyId(companyId)).thenReturn(customers);
        when(customerMapper.customerToCustomerDTO(eq(c1), any(CustomerDTO.class))).thenReturn(expectedDto);
        CustomerDTO dto2 = new CustomerDTO(2L, "Ana", "ana@mail.com", companyId, new ArrayList<>());
        when(customerMapper.customerToCustomerDTO(eq(c2), any(CustomerDTO.class))).thenReturn(dto2);

        List<CustomerDTO> result = customerService.getAllCustomers(request);

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
    }

    @Test
    void updateCustomer_successful() {
        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest("Juanito", "juanito@mail.com");
        Company company = new Company(); company.setId(companyId);
        Customer existing = new Customer(1L, "Juan", "juan@mail.com", company, new ArrayList<>());

        when(customerRepository.findById(1L)).thenReturn(Optional.of(existing));
        Customer updatedEntity = new Customer(1L, "Juanito", "juanito@mail.com", company, new ArrayList<>());
        when(customerRepository.save(existing)).thenReturn(updatedEntity);
        CustomerDTO updatedDto = new CustomerDTO(1L, "Juanito", "juanito@mail.com", companyId, new ArrayList<>());
        when(customerMapper.customerToCustomerDTO(eq(updatedEntity), any(CustomerDTO.class))).thenReturn(updatedDto);

        CustomerDTO result = customerService.updateCustomer(1L, updateRequest, request);

        assertEquals("Juanito", result.getName());
        assertEquals("juanito@mail.com", result.getEmail());
    }

    @Test
    void updateCustomer_customerNotFound_throws() {
        when(customerRepository.findById(anyLong())).thenReturn(Optional.empty());

        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest("X", "x@mail.com");
        ObjectNotFoundException e = assertThrows(ObjectNotFoundException.class,
                () -> customerService.updateCustomer(1L, updateRequest, request));

        assertEquals(ErrorMessages.CUSTOMER_NOT_FOUND.getMessage(), e.getMessage());
    }

    @Test
    void updateCustomer_accessDenied_throws() {
        Company otherCompany = new Company(); otherCompany.setId(2L);
        Customer existing = new Customer(1L, "Juan", "juan@mail.com", otherCompany, new ArrayList<>());
        when(customerRepository.findById(1L)).thenReturn(Optional.of(existing));

        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest("X", "x@mail.com");
        AccessDeniedException e = assertThrows(AccessDeniedException.class,
                () -> customerService.updateCustomer(1L, updateRequest, request));

        assertEquals(ErrorMessages.ACCESS_DENIED_UPDATE.getMessage(), e.getMessage());
    }

    @Test
    void deleteCustomer_successful() {
        Company company = new Company(); company.setId(companyId);
        Customer existing = new Customer(1L, "Juan", "juan@mail.com", company, new ArrayList<>());
        when(customerRepository.findById(1L)).thenReturn(Optional.of(existing));

        customerService.deleteCustomer(1L, request);

        verify(customerRepository).deleteById(1L);
    }

    @Test
    void deleteCustomer_customerNotFound_throws() {
        when(customerRepository.findById(anyLong())).thenReturn(Optional.empty());

        ObjectNotFoundException e = assertThrows(ObjectNotFoundException.class,
                () -> customerService.deleteCustomer(1L, request));

        assertEquals(ErrorMessages.CUSTOMER_NOT_FOUND.getMessage(), e.getMessage());
    }

    @Test
    void deleteCustomer_accessDenied_throws() {
        Company otherCompany = new Company(); otherCompany.setId(2L);
        Customer existing = new Customer(1L, "Juan", "juan@mail.com", otherCompany, new ArrayList<>());
        when(customerRepository.findById(1L)).thenReturn(Optional.of(existing));

        AccessDeniedException e = assertThrows(AccessDeniedException.class,
                () -> customerService.deleteCustomer(1L, request));

        assertEquals(ErrorMessages.ACCESS_DENIED_DELETE.getMessage(), e.getMessage());
    }*/

}
