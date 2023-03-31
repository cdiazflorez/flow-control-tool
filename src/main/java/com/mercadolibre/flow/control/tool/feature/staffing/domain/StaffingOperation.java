package com.mercadolibre.flow.control.tool.feature.staffing.domain;

import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.staffing.constant.StaffingType;
import java.time.Instant;
import java.util.Map;

public record StaffingOperation(
    Instant lastModifiedDate,
    Map<StaffingType, Map<ProcessName, StaffingOperationValues>> values) {
}
