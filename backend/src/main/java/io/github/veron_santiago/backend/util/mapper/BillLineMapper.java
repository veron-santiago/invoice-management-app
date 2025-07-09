package io.github.veron_santiago.backend.util.mapper;

import io.github.veron_santiago.backend.persistence.entity.BillLine;
import io.github.veron_santiago.backend.persistence.repository.IBillRepository;
import io.github.veron_santiago.backend.persistence.repository.IProductRepository;
import io.github.veron_santiago.backend.presentation.dto.response.BillLineDTO;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BillLineMapper {

    @Mapping(target = "billId", ignore = true)
    @Mapping(target = "productId", ignore = true)
    BillLineDTO billLineToBillLineDTO(BillLine billLine,
                                      @MappingTarget BillLineDTO billLineDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "bill", ignore = true)
    @Mapping(target = "product", ignore = true)
    BillLine billLineDTOToBillLine(BillLineDTO billLineDTO,
                                   @MappingTarget BillLine billLine,
                                   @Context IBillRepository billRepository,
                                   @Context IProductRepository productRepository);

    @AfterMapping
    default void afterBillLineToBillLineDTO(BillLine billLine,
                                            @MappingTarget BillLineDTO billLineDTO) {
        if (billLine.getBill() != null) {
            billLineDTO.setBillId(billLine.getBill().getId());
        }
        if (billLine.getProduct() != null) {
            billLineDTO.setProductId(billLine.getProduct().getId());
        }
    }

    @AfterMapping
    default void afterBillLineDTOToBillLine(BillLineDTO billLineDTO,
                                            @MappingTarget BillLine billLine,
                                            @Context IBillRepository billRepository,
                                            @Context IProductRepository productRepository) {
        if (billLine.getBill() == null && billLineDTO.getBillId() != null) {
            billRepository.findById(billLineDTO.getBillId()).ifPresent(billLine::setBill);
        }
        if (billLine.getProduct() == null && billLineDTO.getProductId() != null) {
            productRepository.findById(billLineDTO.getProductId()).ifPresent(billLine::setProduct);
        }
    }
}