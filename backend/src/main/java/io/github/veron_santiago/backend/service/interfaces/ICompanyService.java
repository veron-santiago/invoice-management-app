package io.github.veron_santiago.backend.service.interfaces;

import io.github.veron_santiago.backend.presentation.dto.AuthCreateCompany;
import io.github.veron_santiago.backend.presentation.dto.AuthResponse;
import io.github.veron_santiago.backend.presentation.dto.response.CompanyDTO;
import jakarta.servlet.http.HttpServletRequest;

public interface ICompanyService {
    AuthResponse createCompany(AuthCreateCompany authCreateCompany);
    CompanyDTO getCompany(HttpServletRequest request);
    void deleteCompany(HttpServletRequest request);
    boolean verifyEmail(String token);
}
