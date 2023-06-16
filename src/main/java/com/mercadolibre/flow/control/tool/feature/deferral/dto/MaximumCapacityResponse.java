package com.mercadolibre.flow.control.tool.feature.deferral.dto;

import java.util.List;

public record MaximumCapacityResponse(
    String logisticCenterId,
    List<MaximumCapacityDataDto> maxCapacity
) {
}
