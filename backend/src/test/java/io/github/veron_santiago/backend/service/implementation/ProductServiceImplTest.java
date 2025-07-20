package io.github.veron_santiago.backend.service.implementation;

import io.github.veron_santiago.backend.persistence.entity.Company;
import io.github.veron_santiago.backend.persistence.entity.Product;
import io.github.veron_santiago.backend.persistence.repository.ICompanyRepository;
import io.github.veron_santiago.backend.persistence.repository.IProductRepository;
import io.github.veron_santiago.backend.presentation.dto.request.ProductRequest;
import io.github.veron_santiago.backend.presentation.dto.response.ProductDTO;
import io.github.veron_santiago.backend.presentation.dto.update.ProductUpdateRequest;
import io.github.veron_santiago.backend.service.exception.ErrorMessages;
import io.github.veron_santiago.backend.service.exception.InvalidFieldException;
import io.github.veron_santiago.backend.service.exception.ObjectNotFoundException;
import io.github.veron_santiago.backend.util.AuthUtil;
import io.github.veron_santiago.backend.util.mapper.ProductMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProductServiceImplTest {

    @InjectMocks private ProductServiceImpl productService;
    @Mock private IProductRepository productRepository;
    @Mock private ICompanyRepository companyRepository;
    @Mock private ProductMapper productMapper;
    @Mock private AuthUtil authUtil;
    @Mock private HttpServletRequest request;

    private Long companyId;
    private Company company;
    private Product product1, product2;
    private ProductDTO dto1, dto2;

    @BeforeEach
    void setUp() {
        companyId = 1L;
        company = new Company();
        company.setId(companyId);
        when(authUtil.getAuthenticatedCompanyId(request))
                .thenReturn(companyId);

        product1 = Product.builder()
                .id(1L)
                .name("producto1")
                .code("code1")
                .price(new BigDecimal("10.00"))
                .company(company)
                .build();
        product2 = Product.builder()
                .id(2L)
                .name("producto2")
                .code("code2")
                .price(new BigDecimal("20.00"))
                .company(company)
                .build();

        dto1 = new ProductDTO(1L, "code1", "producto1", new BigDecimal("10.00"), companyId);
        dto2 = new ProductDTO(2L, "code2", "producto2", new BigDecimal("20.00"), companyId);
    }

    @Test
    void createProduct_successful() {
        ProductRequest req = new ProductRequest(product1.getCode(), product1.getName(), product1.getPrice());

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(productRepository.save(any(Product.class))).thenReturn(product1);
        when(productMapper.productToProductDTO(eq(product1), any(ProductDTO.class))).thenReturn(dto1);

        ProductDTO result = productService.createProduct(req, request);

        assertEquals("producto1", result.getName());
        assertEquals("code1", result.getCode());
        assertEquals(new BigDecimal("10.00"), result.getPrice());
        assertEquals(companyId, result.getCompanyId());
    }

    @Test
    void createProduct_companyNotFound_throws() {
        ProductRequest req = new ProductRequest(product1.getCode(), product1.getName(), product1.getPrice());
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        ObjectNotFoundException ex = assertThrows(ObjectNotFoundException.class,
                () -> productService.createProduct(req, request));
        assertEquals(ErrorMessages.COMPANY_NOT_FOUND.getMessage(), ex.getMessage());
    }

    @Test
    void createProduct_invalidFields_throws() {
        ProductRequest req = new ProductRequest("codeX", "", BigDecimal.ZERO);
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        InvalidFieldException ex = assertThrows(InvalidFieldException.class,
                () -> productService.createProduct(req, request));
        assertEquals("El nombre y el precio del producto deben estar declarados", ex.getMessage());
    }

    @Test
    void getProductById_successful() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(productMapper.productToProductDTO(eq(product1), any(ProductDTO.class))).thenReturn(dto1);

        ProductDTO result = productService.getProductById(1L, request);
        assertEquals("producto1", result.getName());
    }

    @Test
    void getProductById_notFound_throws() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        ObjectNotFoundException ex = assertThrows(ObjectNotFoundException.class,
                () -> productService.getProductById(1L, request));
        assertEquals(ErrorMessages.PRODUCT_NOT_FOUND.getMessage(), ex.getMessage());
    }

    @Test
    void getProductById_accessDenied_throws() {
        Company other = new Company();
        other.setId(99L);
        Product productoOtro = Product.builder()
                .id(3L)
                .name("otro")
                .code("code3")
                .price(BigDecimal.ONE)
                .company(other)
                .build();

        when(productRepository.findById(3L)).thenReturn(Optional.of(productoOtro));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> productService.getProductById(3L, request));
        assertEquals(ErrorMessages.ACCESS_DENIED_READ.getMessage(), ex.getMessage());
    }

    @Test
    void getAllProducts_successful() {
        when(productRepository.findByCompanyId(companyId))
                .thenReturn(List.of(product1, product2));
        when(productMapper.productToProductDTO(eq(product1), any(ProductDTO.class))).thenReturn(dto1);
        when(productMapper.productToProductDTO(eq(product2), any(ProductDTO.class))).thenReturn(dto2);

        List<ProductDTO> list = productService.getAllProducts(request);
        assertEquals(2, list.size());
        assertEquals("producto1", list.get(0).getName());
        assertEquals("producto2", list.get(1).getName());
    }

    @Test
    void updateProduct_successful() {
        ProductUpdateRequest req = new ProductUpdateRequest("producto1_nuevo", "code1_nuevo", new BigDecimal("15.00"));
        Product updated = Product.builder()
                .id(1L)
                .name("producto1_nuevo")
                .code("code1_nuevo")
                .price(new BigDecimal("15.00"))
                .company(company)
                .build();
        ProductDTO dtoUpdated = new ProductDTO(1L, "code1_nuevo", "producto1_nuevo", new BigDecimal("15.00"), companyId);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(productRepository.save(product1)).thenReturn(updated);
        when(productMapper.productToProductDTO(eq(updated), any(ProductDTO.class))).thenReturn(dtoUpdated);

        ProductDTO result = productService.updateProduct(1L, req, request);
        assertEquals("producto1_nuevo", result.getName());
        assertEquals("code1_nuevo", result.getCode());
        assertEquals(new BigDecimal("15.00"), result.getPrice());
    }

    @Test
    void updateProduct_notFound_throws() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());
        ProductUpdateRequest req = new ProductUpdateRequest("x", "y", BigDecimal.ONE);

        ObjectNotFoundException ex = assertThrows(ObjectNotFoundException.class,
                () -> productService.updateProduct(1L, req, request));
        assertEquals(ErrorMessages.PRODUCT_NOT_FOUND.getMessage(), ex.getMessage());
    }

    @Test
    void updateProduct_accessDenied_throws() {
        Company other = new Company(); other.setId(5L);
        Product productoOtro = Product.builder()
                .id(4L)
                .name("otro")
                .code("code4")
                .price(BigDecimal.ONE)
                .company(other)
                .build();
        when(productRepository.findById(4L)).thenReturn(Optional.of(productoOtro));
        ProductUpdateRequest req = new ProductUpdateRequest("x", "y", BigDecimal.ONE);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> productService.updateProduct(4L, req, request));
        assertEquals(ErrorMessages.ACCESS_DENIED_UPDATE.getMessage(), ex.getMessage());
    }

    @Test
    void updateProduct_emptyFields_throws() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        ProductUpdateRequest req = new ProductUpdateRequest(null, null, null);

        InvalidFieldException ex = assertThrows(InvalidFieldException.class,
                () -> productService.updateProduct(1L, req, request));
        assertEquals(ErrorMessages.PRODUCT_EMPTY_FIELDS.getMessage(), ex.getMessage());
    }

    @Test
    void updateProduct_invalidName_throws() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        ProductUpdateRequest req = new ProductUpdateRequest(" ", null, null);

        InvalidFieldException ex = assertThrows(InvalidFieldException.class,
                () -> productService.updateProduct(1L, req, request));
        assertEquals(ErrorMessages.PRODUCT_NAME_IS_EMPTY.getMessage(), ex.getMessage());
    }

    @Test
    void updateProduct_invalidCode_throws() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        ProductUpdateRequest req = new ProductUpdateRequest(null, " ", null);

        InvalidFieldException ex = assertThrows(InvalidFieldException.class,
                () -> productService.updateProduct(1L, req, request));
        assertEquals(ErrorMessages.PRODUCT_CODE_IS_EMPTY.getMessage(), ex.getMessage());
    }

    @Test
    void updateProduct_invalidPrice_throws() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        ProductUpdateRequest req = new ProductUpdateRequest(null, null, BigDecimal.ZERO);

        InvalidFieldException ex = assertThrows(InvalidFieldException.class,
                () -> productService.updateProduct(1L, req, request));
        assertEquals(ErrorMessages.PRODUCT_PRICE_MUST_BE_POSITIVE.getMessage(), ex.getMessage());
    }

    @Test
    void deleteProduct_successful() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));

        productService.deleteProduct(1L, request);
        verify(productRepository).deleteById(1L);
    }

    @Test
    void deleteProduct_notFound_throws() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        ObjectNotFoundException ex = assertThrows(ObjectNotFoundException.class,
                () -> productService.deleteProduct(1L, request));
        assertEquals(ErrorMessages.PRODUCT_NOT_FOUND.getMessage(), ex.getMessage());
    }

    @Test
    void deleteProduct_accessDenied_throws() {
        Company other = new Company(); other.setId(5L);
        Product productoOtro = Product.builder()
                .id(5L)
                .name("otro")
                .code("code5")
                .price(BigDecimal.ONE)
                .company(other)
                .build();
        when(productRepository.findById(5L)).thenReturn(Optional.of(productoOtro));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> productService.deleteProduct(5L, request));
        assertEquals(ErrorMessages.ACCESS_DENIED_DELETE.getMessage(), ex.getMessage());
    }
}
