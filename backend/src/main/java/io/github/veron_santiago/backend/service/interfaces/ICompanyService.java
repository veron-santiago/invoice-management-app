package io.github.veron_santiago.backend.service.interfaces;

import io.github.veron_santiago.backend.presentation.dto.auth.AuthCreateCompany;
import io.github.veron_santiago.backend.presentation.dto.auth.AuthResponse;
import io.github.veron_santiago.backend.presentation.dto.response.CompanyDTO;
import io.github.veron_santiago.backend.presentation.dto.update.CompanyUpdateAddress;
import io.github.veron_santiago.backend.presentation.dto.update.CompanyUpdateEmail;
import io.github.veron_santiago.backend.presentation.dto.update.CompanyUpdateName;
import io.github.veron_santiago.backend.presentation.dto.update.CompanyUpdatePassword;
import org.springframework.core.io.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;

public interface ICompanyService {
    AuthResponse createCompany(AuthCreateCompany authCreateCompany);
    CompanyDTO getCompany(HttpServletRequest request);
    CompanyDTO updateAddress(CompanyUpdateAddress companyUpdateAddress, HttpServletRequest request);
    CompanyDTO updateName(CompanyUpdateName companyUpdateName, HttpServletRequest request);
    CompanyDTO updateEmail(CompanyUpdateEmail companyUpdateEmail, HttpServletRequest request);
    void updatePassword(CompanyUpdatePassword updatePassword, HttpServletRequest request);
    Resource getLogo(HttpServletRequest request) throws MalformedURLException;
    void uploadLogo(MultipartFile file, HttpServletRequest request) throws IOException;
    void deleteCompany(HttpServletRequest request);
    boolean verifyEmail(String token);
}
