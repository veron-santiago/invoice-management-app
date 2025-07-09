package io.github.veron_santiago.backend.util.mapper;

import io.github.veron_santiago.backend.persistence.entity.Product;
import io.github.veron_santiago.backend.persistence.repository.ICompanyRepository;
import io.github.veron_santiago.backend.presentation.dto.response.ProductDTO;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

    @Mapping(target = "companyId", ignore = true)
    ProductDTO productToProductDTO(Product product,
                                   @MappingTarget ProductDTO productDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    Product productDTOToProduct(ProductDTO productDTO,
                                @MappingTarget Product product,
                                @Context ICompanyRepository companyRepository);

    @AfterMapping
    default void afterProductToProductDTO(Product product,
                                          @MappingTarget ProductDTO productDTO){
        if (productDTO.getCompanyId() == null) {
            productDTO.setCompanyId(product.getCompany().getId());
        }
    }

    @AfterMapping
    default void afterProductDTOToProduct(ProductDTO productDTO,
                                          @MappingTarget Product product,
                                          @Context ICompanyRepository companyRepository){
        if (product.getCompany() == null){
            product.setCompany(companyRepository.findById(productDTO.getCompanyId()).orElse(null));
        }
    }

}