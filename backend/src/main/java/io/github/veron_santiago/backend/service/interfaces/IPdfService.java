package io.github.veron_santiago.backend.service.interfaces;

import io.github.veron_santiago.backend.persistence.entity.Bill;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.nio.file.AccessDeniedException;

public interface IPdfService {
    String generateBillPdf(Bill bill, HttpServletRequest request, boolean includeQr) throws IOException;
    byte[] getPdfByBillId(Long billId, HttpServletRequest request) throws AccessDeniedException;
}
