package com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto;

import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import java.util.List;

public record ProcessesMonitor(
    ProcessName name,
    Integer quantity,
    List<SlasMonitor> slas
) {
}
