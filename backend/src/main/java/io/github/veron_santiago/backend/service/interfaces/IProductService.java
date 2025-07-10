package io.github.veron_santiago.backend.service.interfaces;

import io.github.veron_santiago.backend.presentation.dto.request.ProductRequest;
import io.github.veron_santiago.backend.presentation.dto.response.ProductDTO;
import io.github.veron_santiago.backend.presentation.dto.update.ProductUpdateRequest;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface IProductService {

    ProductDTO createProduct(ProductRequest productRequest, HttpServletRequest request);
    ProductDTO getProductById(Long id, HttpServletRequest request);
    List<ProductDTO> getAllProducts(HttpServletRequest request);
    ProductDTO updateProduct(Long id, ProductUpdateRequest productRequest, HttpServletRequest request);
    void deleteProduct(Long id, HttpServletRequest request);

}