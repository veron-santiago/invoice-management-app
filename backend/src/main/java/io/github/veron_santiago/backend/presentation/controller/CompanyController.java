package io.github.veron_santiago.backend.presentation.controller;

import io.github.veron_santiago.backend.presentation.dto.response.CompanyDTO;
import io.github.veron_santiago.backend.service.interfaces.ICompanyService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/companies")
public class CompanyController {

    private final ICompanyService companyService;

    public CompanyController(ICompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping
    public ResponseEntity<CompanyDTO> getCompany(HttpServletRequest request){
        return ResponseEntity.ok(companyService.getCompany(request));
    }
    @DeleteMapping
    public ResponseEntity<Void> deleteCompany(HttpServletRequest request) {
        companyService.deleteCompany(request);
        return ResponseEntity.noContent().build();
    }
}