package com.mercadolibre.flow.control.tool.feature.staffing.domain;

import java.util.List;

public record StaffingOperationValues(StaffingOperationData staffingOperationTotal, List<StaffingOperationData> staffingOperationValues) {
}
