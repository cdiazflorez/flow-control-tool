package com.mercadolibre.flow.control.tool.feature.staffing.domain;

import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import java.time.Instant;

public record StaffingPlannedData(
    Instant date,
    ProcessName processName,
    long planned,
    long plannedNonSystemic,
    boolean plannedEdited,
    boolean plannedNonSystemicEdited) {
}
