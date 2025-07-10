package io.github.veron_santiago.backend.presentation.controller;

import io.github.veron_santiago.backend.presentation.dto.request.CustomerRequest;
import io.github.veron_santiago.backend.presentation.dto.response.CustomerDTO;
import io.github.veron_santiago.backend.presentation.dto.update.CustomerUpdateRequest;
import io.github.veron_santiago.backend.service.interfaces.ICustomerService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final ICustomerService customerService;

    public CustomerController(ICustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<CustomerDTO> createCustomer(@RequestBody CustomerRequest customerRequest, HttpServletRequest request){
        return ResponseEntity.ok(customerService.createCustomer(customerRequest, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDTO> getCustomerById(@PathVariable Long id, HttpServletRequest request){
        return ResponseEntity.ok(customerService.getCustomerById(id, request));
    }

    @GetMapping
    public ResponseEntity<List<CustomerDTO>> getCustomers(HttpServletRequest request){
        return ResponseEntity.ok(customerService.getAllCustomers(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerDTO> updateCustomer(@PathVariable Long id, @RequestBody CustomerUpdateRequest customerRequest, HttpServletRequest request){
        return ResponseEntity.ok(customerService.updateCustomer(id, customerRequest, request));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id, HttpServletRequest request){
        customerService.deleteCustomer(id, request);
        return ResponseEntity.noContent().build();
    }

}
