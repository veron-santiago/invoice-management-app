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
import io.github.veron_santiago.backend.service.exception.ResourceConflictException;
import io.github.veron_santiago.backend.service.interfaces.IProductService;
import io.github.veron_santiago.backend.util.AuthUtil;
import io.github.veron_santiago.backend.util.mapper.ProductMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements IProductService {

    private final IProductRepository productRepository;
    private final ICompanyRepository companyRepository;
    private final ProductMapper productMapper;
    private final AuthUtil authUtil;

    public ProductServiceImpl(IProductRepository productRepository, ICompanyRepository companyRepository, ProductMapper productMapper, AuthUtil authUtil) {
        this.productRepository = productRepository;
        this.companyRepository = companyRepository;
        this.productMapper = productMapper;
        this.authUtil = authUtil;
    }


    @Override
    public ProductDTO createProduct(ProductRequest productRequest, HttpServletRequest request){
        Long companyId = authUtil.getAuthenticatedCompanyId(request);

        Company company = companyRepository.findById(companyId)
                .orElseThrow( () -> new ObjectNotFoundException(ErrorMessages.COMPANY_NOT_FOUND.getMessage()));

        String name = productRequest.name();
        BigDecimal price = productRequest.price();
        String code = productRequest.code();
        if (code != null && code.isEmpty()) code = null;

        if ((name == null || name.isBlank()) || (price == null || price.compareTo(BigDecimal.ZERO) <= 0)){
            throw new InvalidFieldException("El nombre y el precio del producto deben estar declarados");
        }

        if (productRepository.existsByNameAndCompanyId(name, companyId)){
            throw new ResourceConflictException("Ya existe un producto con ese nombre");
        }

        if (code != null && productRepository.existsByCodeAndCompanyId(code, companyId)){
            throw new ResourceConflictException("Ya existe un producto con ese código");
        }

        Product product = Product.builder()
                .code(code)
                .name(name)
                .price(price)
                .company(company)
                .build();

        Product savedProduct = productRepository.save(product);
        return productMapper.productToProductDTO(savedProduct, new ProductDTO());
    }

    @Override
    public ProductDTO getProductById(Long id, HttpServletRequest request) {
        Long companyId = authUtil.getAuthenticatedCompanyId(request);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(ErrorMessages.PRODUCT_NOT_FOUND.getMessage()));
        if (!product.getCompany().getId().equals(companyId)) throw new AccessDeniedException(ErrorMessages.ACCESS_DENIED_READ.getMessage());
        return productMapper.productToProductDTO(product, new ProductDTO());
    }

    @Override
    public List<ProductDTO> getAllProducts(HttpServletRequest request) {
        Long companyId = authUtil.getAuthenticatedCompanyId(request);
        List<Product> products = productRepository.findByCompanyId(companyId);
        return products.stream()
                .map(product -> productMapper.productToProductDTO(product, new ProductDTO()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductDTO updateProduct(Long id, ProductUpdateRequest request, HttpServletRequest httpRequest) {
        Long companyId = authUtil.getAuthenticatedCompanyId(httpRequest);
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(ErrorMessages.PRODUCT_NOT_FOUND.getMessage()));

        if (!existing.getCompany().getId().equals(companyId)) {
            throw new AccessDeniedException(ErrorMessages.ACCESS_DENIED_UPDATE.getMessage());
        }

        String name = request.name();
        String code = request.code();
        if (code != null && code.isBlank()) code = null;
        BigDecimal price = request.price();

        if (name == null || name.isBlank()) {
            throw new InvalidFieldException(ErrorMessages.PRODUCT_NAME_IS_EMPTY.getMessage());
        }

        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidFieldException(ErrorMessages.PRODUCT_PRICE_MUST_BE_POSITIVE.getMessage());
        }

        if (productRepository.existsByNameAndCompanyIdAndIdNot(name, companyId, id)) {
            throw new ResourceConflictException(ErrorMessages.PRODUCT_NAME_ALREADY_EXISTS.getMessage());
        }

        if (code != null && productRepository.existsByCodeAndCompanyIdAndIdNot(code, companyId, id)) {
            throw new ResourceConflictException(ErrorMessages.PRODUCT_CODE_ALREADY_EXISTS.getMessage());
        }

        existing.setName(name);
        existing.setCode(code);
        existing.setPrice(price);

        return productMapper.productToProductDTO(productRepository.save(existing), new ProductDTO());
    }

    @Override
    public void deleteProduct(Long id, HttpServletRequest request) {
        Long companyId = authUtil.getAuthenticatedCompanyId(request);
        Product product = productRepository.findById(id)
                .orElseThrow( () -> new ObjectNotFoundException(ErrorMessages.PRODUCT_NOT_FOUND.getMessage()));
        if (!product.getCompany().getId().equals(companyId)) throw new AccessDeniedException(ErrorMessages.ACCESS_DENIED_DELETE.getMessage());
        productRepository.deleteById(id);
    }
}
