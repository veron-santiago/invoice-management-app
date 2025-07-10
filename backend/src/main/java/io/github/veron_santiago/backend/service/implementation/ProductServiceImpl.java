package io.github.veron_santiago.backend.service.implementation;

import io.github.veron_santiago.backend.persistence.entity.Company;
import io.github.veron_santiago.backend.persistence.entity.Product;
import io.github.veron_santiago.backend.persistence.repository.ICompanyRepository;
import io.github.veron_santiago.backend.persistence.repository.IProductRepository;
import io.github.veron_santiago.backend.presentation.dto.request.ProductRequest;
import io.github.veron_santiago.backend.presentation.dto.response.ProductDTO;
import io.github.veron_santiago.backend.presentation.dto.update.ProductUpdateRequest;
import io.github.veron_santiago.backend.service.interfaces.IProductService;
import io.github.veron_santiago.backend.util.mapper.ProductMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements IProductService {

    private final IProductRepository productRepository;
    private final ICompanyRepository companyRepository;
    private final ProductMapper productMapper;
    private final AuthUtil authUtil;

    private final String PRODUCT_NOT_FOUND = "Producto no encontrado";

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
                .orElseThrow(() -> new RuntimeException("Compañia no encontrada"));

        String name = productRequest.name();
        BigDecimal price = productRequest.price();
        String code = productRequest.code();

        if ((name == null || name.isBlank()) || (price == null || price.compareTo(BigDecimal.ZERO) <= 0)){
            throw new RuntimeException("El nombre y el precio del producto deben estar declarados");
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
                .orElseThrow( () -> new RuntimeException(PRODUCT_NOT_FOUND) );
        if (!product.getCompany().getId().equals(companyId)) throw new AccessDeniedException("No tienes permiso para acceder a este producto");
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
    public ProductDTO updateProduct(Long id, ProductUpdateRequest productRequest, HttpServletRequest request){
        Long companyId = authUtil.getAuthenticatedCompanyId(request);
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(PRODUCT_NOT_FOUND));

        if (!existingProduct.getCompany().getId().equals(companyId)) {
            throw new AccessDeniedException("No tienes permiso para modificar este producto");
        }

        String name = productRequest.name();
        String code = productRequest.code();
        BigDecimal price = productRequest.price();

        if (name == null && code == null && price == null) throw new RuntimeException("Se debe declarar al menos un valor: nombre o precio");

        if (name != null){
            if (name.isBlank()) throw new RuntimeException("El nombre no puede estar vacío");
            existingProduct.setName(name);
        }
        if (code != null){
            if (code.isBlank()) throw new RuntimeException("El código no puede estar vacío");
            existingProduct.setCode(code);
        }
        if (price != null) {
            if (price.compareTo(BigDecimal.ZERO) <= 0) throw new RuntimeException("El precio debe ser mayor que cero");
            existingProduct.setPrice(price);
        }

        return productMapper.productToProductDTO(productRepository.save(existingProduct), new ProductDTO());
    }

    @Override
    public void deleteProduct(Long id, HttpServletRequest request) {
        Long companyId = authUtil.getAuthenticatedCompanyId(request);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(PRODUCT_NOT_FOUND));
        if (!product.getCompany().getId().equals(companyId)) throw new AccessDeniedException("No tienes permiso para eliminar este producto");
        productRepository.deleteById(id);
    }
}
