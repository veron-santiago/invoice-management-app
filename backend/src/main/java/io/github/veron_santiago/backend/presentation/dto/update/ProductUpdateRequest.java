package io.github.veron_santiago.backend.presentation.dto.update;

import java.math.BigDecimal;

public record ProductUpdateRequest(String name,
                                   String code,
                                   BigDecimal price) {
}
