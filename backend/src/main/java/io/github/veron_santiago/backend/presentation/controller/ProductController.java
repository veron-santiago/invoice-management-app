package io.github.veron_santiago.backend.presentation.controller;

import io.github.veron_santiago.backend.presentation.dto.request.ProductRequest;
import io.github.veron_santiago.backend.presentation.dto.response.ProductDTO;
import io.github.veron_santiago.backend.presentation.dto.update.ProductUpdateRequest;
import io.github.veron_santiago.backend.service.interfaces.IProductService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class  ProductController {

    private final IProductService productService;

    public ProductController(IProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductRequest productRequest, HttpServletRequest request){
        return ResponseEntity.ok(productService.createProduct(productRequest, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id, HttpServletRequest request){
        return ResponseEntity.ok(productService.getProductById(id, request));
    }

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts(HttpServletRequest request){
        return ResponseEntity.ok(productService.getAllProducts(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductUpdateRequest productRequest, HttpServletRequest request){
        return ResponseEntity.ok(productService.updateProduct(id, productRequest, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id, HttpServletRequest request){
        productService.deleteProduct(id, request);
        return ResponseEntity.noContent().build();
    }
}
