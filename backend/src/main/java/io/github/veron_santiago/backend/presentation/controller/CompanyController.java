package io.github.veron_santiago.backend.presentation.controller;

import io.github.veron_santiago.backend.presentation.dto.response.CompanyDTO;
import io.github.veron_santiago.backend.service.interfaces.ICompanyService;
import org.springframework.core.io.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;

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

    @GetMapping("/logo")
    public ResponseEntity<Resource> getLogo(HttpServletRequest request) throws MalformedURLException {
        Resource resource = companyService.getLogo(request);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(resource);
    }

    @PostMapping("/logo")
    public ResponseEntity<Void> uploadLogo(HttpServletRequest request, @RequestParam("file") MultipartFile file) throws IOException {
        companyService.uploadLogo(file, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteCompany(HttpServletRequest request) {
        companyService.deleteCompany(request);
        return ResponseEntity.noContent().build();
    }
}