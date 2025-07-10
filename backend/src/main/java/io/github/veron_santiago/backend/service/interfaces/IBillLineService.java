package io.github.veron_santiago.backend.service.interfaces;

import io.github.veron_santiago.backend.persistence.entity.Bill;
import io.github.veron_santiago.backend.persistence.entity.BillLine;
import io.github.veron_santiago.backend.presentation.dto.request.BillLineRequest;
import io.github.veron_santiago.backend.presentation.dto.response.BillLineDTO;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.List;

public interface IBillLineService {
    BillLine createBillLine(BillLineRequest billLineRequest, Bill bill, HttpServletRequest request)throws IOException;
    BillLineDTO getBillLineById(Long id, HttpServletRequest request);
    List<BillLineDTO> getAllBillLinesByBillId(Long id, HttpServletRequest request);
}
