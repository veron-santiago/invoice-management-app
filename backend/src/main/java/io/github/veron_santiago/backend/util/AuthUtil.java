package io.github.veron_santiago.backend.util;

import io.github.veron_santiago.backend.persistence.entity.Company;
import io.github.veron_santiago.backend.persistence.repository.ICompanyRepository;
import io.github.veron_santiago.backend.service.exception.ErrorMessages;
import io.github.veron_santiago.backend.service.exception.ObjectNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class AuthUtil {
    private final JwtUtil jwtUtil;
    private final ICompanyRepository companyRepository;

    public AuthUtil(ICompanyRepository companyRepository, JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
        this.companyRepository = companyRepository;
    }

    public Long getAuthenticatedCompanyId(HttpServletRequest request) {
        String token = extractTokenFromHeader(request);
        return Long.valueOf(jwtUtil.extractSubject(token));
    }

    public Company getCompanyByRequest(HttpServletRequest request){
        Long companyId = getAuthenticatedCompanyId(request);
        return companyRepository.findById(companyId)
                .orElseThrow( () -> new ObjectNotFoundException(ErrorMessages.COMPANY_NOT_FOUND.getMessage()));
    }

    public Company getCompanyById(Long id){
        return companyRepository.findById(id)
                .orElseThrow( () -> new ObjectNotFoundException(ErrorMessages.COMPANY_NOT_FOUND.getMessage()));
    }

    private String extractTokenFromHeader(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        } else {
            throw new RuntimeException("Token no encontrado en el header");
        }
    }
}