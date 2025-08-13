package io.github.veron_santiago.backend.presentation.controller;

import io.github.veron_santiago.backend.persistence.repository.IBillRepository;
import io.github.veron_santiago.backend.presentation.dto.request.BillRequest;
import io.github.veron_santiago.backend.presentation.dto.response.BillDTO;
import io.github.veron_santiago.backend.service.interfaces.IBillService;
import io.github.veron_santiago.backend.service.interfaces.IPdfService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/bills")
public class BillController {

    private final IBillService billService;
    private final IPdfService pdfService;
    private final IBillRepository billRepository;

    public BillController(IBillService billService, IPdfService pdfService, IBillRepository billRepository) {
        this.billService = billService;
        this.pdfService = pdfService;
        this.billRepository = billRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<BillDTO> getBillById(@PathVariable Long id, HttpServletRequest request) {
        return ResponseEntity.ok(billService.getBillById(id, request));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> getPdfById(@PathVariable Long id, HttpServletRequest request) throws AccessDeniedException {
        byte[] pdf = pdfService.getPdfByBillId(id, request);
        Long billNumber = billRepository.getBillNumberById(id);
        int times = 8 - String.valueOf(billNumber).length();
        String name = "0".repeat(times) + billNumber;
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + name + ".pdf")
                .body(pdf);
    }

    @GetMapping
    public ResponseEntity<List<BillDTO>> getAllBills(HttpServletRequest request) {
        return ResponseEntity.ok(billService.getAllBills(request));
    }

    @PostMapping
    public ResponseEntity<BillDTO> createBill(@Valid @RequestBody BillRequest billRequest, HttpServletRequest request) throws IOException {
        return ResponseEntity.ok(billService.createBill(billRequest, request));
    }

}
