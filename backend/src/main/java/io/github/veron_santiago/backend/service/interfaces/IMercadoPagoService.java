package io.github.veron_santiago.backend.service.interfaces;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public interface IMercadoPagoService {
    String buildAuthUrl(HttpServletRequest request);
    void redirectToAuth(HttpServletResponse response, HttpServletRequest request) throws IOException;
    void exchangeCodeForTokens(String code, String state, HttpServletRequest request);
}
