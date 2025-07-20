package io.github.veron_santiago.backend.presentation.controller;

import io.github.veron_santiago.backend.persistence.repository.IBillRepository;
import io.github.veron_santiago.backend.presentation.dto.request.BillRequest;
import io.github.veron_santiago.backend.presentation.dto.response.BillDTO;
import io.github.veron_santiago.backend.service.interfaces.IBillService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/bills")
public class BillController {

    private final IBillService billService;
    private final IBillRepository billRepository;

    public BillController(IBillService billService, IBillRepository billRepository) {
        this.billService = billService;
        this.billRepository = billRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<BillDTO> getBillById(@PathVariable Long id, HttpServletRequest request) {
        return ResponseEntity.ok(billService.getBillById(id, request));
    }

    @GetMapping
    public ResponseEntity<List<BillDTO>> getAllBills(HttpServletRequest request) {
        return ResponseEntity.ok(billService.getAllBills(request));
    }

    @PostMapping
    public ResponseEntity<BillDTO> createBill(@RequestBody BillRequest billRequest, HttpServletRequest request) throws IOException {
        return ResponseEntity.ok(billService.createBill(billRequest, request));
    }

}
