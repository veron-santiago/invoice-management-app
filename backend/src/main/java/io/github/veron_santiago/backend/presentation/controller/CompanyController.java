package io.github.veron_santiago.backend.presentation.controller;

import io.github.veron_santiago.backend.presentation.dto.response.CompanyDTO;
import io.github.veron_santiago.backend.presentation.dto.update.CompanyUpdateAddress;
import io.github.veron_santiago.backend.presentation.dto.update.CompanyUpdateEmail;
import io.github.veron_santiago.backend.presentation.dto.update.CompanyUpdateName;
import io.github.veron_santiago.backend.presentation.dto.update.CompanyUpdatePassword;
import io.github.veron_santiago.backend.service.interfaces.ICompanyService;
import jakarta.validation.Valid;
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
        if (resource == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(resource);
    }

    @PostMapping("/logo")
    public ResponseEntity<Void> uploadLogo(HttpServletRequest request, @RequestParam("file") MultipartFile file) throws IOException {
        companyService.uploadLogo(file, request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/email")
    public ResponseEntity<Void> updateEmail(@Valid @RequestBody CompanyUpdateEmail dto, HttpServletRequest request){
        companyService.updateEmail(dto, request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/name")
    public ResponseEntity<Void> updateName(@Valid @RequestBody CompanyUpdateName dto, HttpServletRequest request){
        companyService.updateName(dto, request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/address")
    public ResponseEntity<Void> updateAddress(@Valid @RequestBody CompanyUpdateAddress dto, HttpServletRequest request){
        companyService.updateAddress(dto, request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/password")
    public ResponseEntity<Void> updatePassword(@Valid @RequestBody CompanyUpdatePassword dto, HttpServletRequest request){
        companyService.updatePassword(dto, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteCompany(HttpServletRequest request) {
        companyService.deleteCompany(request);
        return ResponseEntity.noContent().build();
    }
}