package io.github.veron_santiago.backend.service.interfaces;

import io.github.veron_santiago.backend.presentation.dto.request.CustomerRequest;
import io.github.veron_santiago.backend.presentation.dto.response.CustomerDTO;
import io.github.veron_santiago.backend.presentation.dto.update.CustomerUpdateRequest;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface ICustomerService {
    CustomerDTO createCustomer(CustomerRequest customerRequest, HttpServletRequest request);
    CustomerDTO getCustomerById(Long id, HttpServletRequest request);
    List<CustomerDTO> getAllCustomers(HttpServletRequest request);
    CustomerDTO updateCustomer(Long id, CustomerUpdateRequest customerRequest, HttpServletRequest request);
    void deleteCustomer(Long id, HttpServletRequest request);
}
