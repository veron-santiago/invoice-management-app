package io.github.veron_santiago.backend.util;

import io.github.veron_santiago.backend.persistence.repository.ICompanyRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class AuthUtil {
    private final JwtUtil jwtUtil;

    public AuthUtil(ICompanyRepository companyRepository, JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public Long getAuthenticatedCompanyId(HttpServletRequest request) {
        String token = extractTokenFromHeader(request);
        return jwtUtil.extractCompanyIdFromToken(token);
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