package com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto;

import java.time.Instant;
import java.util.List;

public record BacklogLimit(
    Instant date,
    List<ProcessLimit> processes
) {
}
