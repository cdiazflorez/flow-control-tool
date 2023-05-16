package com.mercadolibre.flow.control.tool.feature.staffing.domain;

import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.staffing.constant.StaffingMetricType;
import java.time.Instant;
import java.util.Map;

public record StaffingOperation(
    Instant lastModifiedDate,
    Map<StaffingMetricType, Map<ProcessName, StaffingOperationValues>> values) {
}
