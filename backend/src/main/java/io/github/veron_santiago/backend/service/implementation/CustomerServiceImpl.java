package io.github.veron_santiago.backend.service.implementation;

import io.github.veron_santiago.backend.persistence.entity.Company;
import io.github.veron_santiago.backend.persistence.entity.Customer;
import io.github.veron_santiago.backend.persistence.repository.ICompanyRepository;
import io.github.veron_santiago.backend.persistence.repository.ICustomerRepository;
import io.github.veron_santiago.backend.presentation.dto.request.CustomerRequest;
import io.github.veron_santiago.backend.presentation.dto.response.CustomerDTO;
import io.github.veron_santiago.backend.presentation.dto.update.CustomerUpdateRequest;
import io.github.veron_santiago.backend.service.interfaces.ICustomerService;
import io.github.veron_santiago.backend.util.AuthUtil;
import io.github.veron_santiago.backend.util.mapper.CustomerMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerServiceImpl implements ICustomerService {

    private final ICustomerRepository customerRepository;
    private final ICompanyRepository companyRepository;
    private final CustomerMapper customerMapper;
    private final AuthUtil authUtil;

    public CustomerServiceImpl(ICustomerRepository customerRepository, ICompanyRepository companyRepository, CustomerMapper customerMapper, AuthUtil authUtil) {
        this.customerRepository = customerRepository;
        this.companyRepository = companyRepository;
        this.customerMapper = customerMapper;
        this.authUtil = authUtil;
    }

    private final String CUSTOMER_NOT_FOUND = "Cliente no encontrado";

    @Override
    public CustomerDTO createCustomer(CustomerRequest customerRequest, HttpServletRequest request){
        Long companyId = authUtil.getAuthenticatedCompanyId(request);
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Compañia no encontrada"));

        Customer customer = Customer.builder()
                .name(customerRequest.name())
                .email(customerRequest.email())
                .company(company)
                .build();

        Customer savedCustomer = customerRepository.save(customer);
        return customerMapper.customerToCustomerDTO(savedCustomer, new CustomerDTO());
    }

    @Override
    public CustomerDTO getCustomerById(Long id, HttpServletRequest request){
        Long companyId = authUtil.getAuthenticatedCompanyId(request);
        Customer customer = customerRepository.findById(id)
                .orElseThrow( () -> new RuntimeException(CUSTOMER_NOT_FOUND) );
        if (!customer.getCompany().getId().equals(companyId)) throw new AccessDeniedException("No tienes permiso para acceder a este cliente");
        return customerMapper.customerToCustomerDTO(customer, new CustomerDTO());
    }

    @Override
    public List<CustomerDTO> getAllCustomers(HttpServletRequest request) {
        Long companyId = authUtil.getAuthenticatedCompanyId(request);
        List<Customer> customers = customerRepository.findByCompanyId(companyId);
        return customers.stream()
                .map( customer -> customerMapper.customerToCustomerDTO(customer, new CustomerDTO()) )
                .collect(Collectors.toList());
    }

    @Override
    public CustomerDTO updateCustomer(Long id, CustomerUpdateRequest customerRequest, HttpServletRequest request){
        Long companyId = authUtil.getAuthenticatedCompanyId(request);
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(CUSTOMER_NOT_FOUND));

        if (!existingCustomer.getCompany().getId().equals(companyId)) throw new AccessDeniedException("No tienes permiso para modificar este cliente");

        String name = customerRequest.name();
        String email = customerRequest.email();

        if (name != null && !name.isBlank()){
            existingCustomer.setName(name);
        }
        if (email != null){
            existingCustomer.setEmail(email.isBlank() ? null : email);
        }

        return customerMapper.customerToCustomerDTO(customerRepository.save(existingCustomer), new CustomerDTO());
    }

    @Override
    public void deleteCustomer(Long id, HttpServletRequest request){
        Long companyId = authUtil.getAuthenticatedCompanyId(request);
        Customer customer = customerRepository.findById(id)
                .orElseThrow( () -> new RuntimeException(CUSTOMER_NOT_FOUND));
        if (!customer.getCompany().getId().equals(companyId)) throw new AccessDeniedException("No tienes permiso para eliminar este cliente");
        customerRepository.deleteById(id);
    }
}
