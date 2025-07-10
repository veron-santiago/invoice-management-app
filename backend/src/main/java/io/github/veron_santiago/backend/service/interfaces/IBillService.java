package io.github.veron_santiago.backend.service.interfaces;

import io.github.veron_santiago.backend.presentation.dto.request.BillRequest;
import io.github.veron_santiago.backend.presentation.dto.response.BillDTO;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.List;

public interface IBillService {
    BillDTO createBill(BillRequest billRequest, HttpServletRequest request) throws IOException;
    BillDTO getBillById(Long id, HttpServletRequest request);
    List<BillDTO> getAllBills(HttpServletRequest request);
}
