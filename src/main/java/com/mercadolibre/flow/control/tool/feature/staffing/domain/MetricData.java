package com.mercadolibre.flow.control.tool.feature.staffing.domain;

import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import java.time.Instant;

public record MetricData(
    ProcessName processName,
    Instant date,
    long productivity,
    long throughput) {
}
