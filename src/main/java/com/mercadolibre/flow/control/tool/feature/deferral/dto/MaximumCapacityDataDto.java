package com.mercadolibre.flow.control.tool.feature.deferral.dto;

import java.time.Instant;

public record MaximumCapacityDataDto(
    Instant date,
    Long quantity
) {
}
