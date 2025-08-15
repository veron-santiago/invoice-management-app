package io.github.veron_santiago.backend.service.interfaces;

import io.github.veron_santiago.backend.persistence.entity.Company;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;

public interface IMercadoPagoService {
    String buildAuthUrl(HttpServletRequest request);
    void exchangeCodeForTokens(String code, String state, HttpServletRequest request);
    String createPaymentLink(Company company, BigDecimal amount);
}
