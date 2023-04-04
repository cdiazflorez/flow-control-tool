package com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto;

import java.util.List;

public record ProcessesMonitor(
    String name,
    Integer quantity,
    List<SlasMonitor> slas
) {
}
