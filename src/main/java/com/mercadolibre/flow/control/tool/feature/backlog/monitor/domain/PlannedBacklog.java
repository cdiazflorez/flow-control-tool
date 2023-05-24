package com.mercadolibre.flow.control.tool.feature.backlog.monitor.domain;

import com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName;
import java.time.Instant;

public record PlannedBacklog(
    ProcessPathName processPath,
    Instant dateIn,
    Instant dateOut,
    int total
) {
}
