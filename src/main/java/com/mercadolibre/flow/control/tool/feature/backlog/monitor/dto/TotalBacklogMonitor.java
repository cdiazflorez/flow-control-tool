package com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto;

import java.time.Instant;
import java.util.List;

public record TotalBacklogMonitor(
    Instant date,
    Integer quantity,
    List<SlasMonitor> slas
) {
}
