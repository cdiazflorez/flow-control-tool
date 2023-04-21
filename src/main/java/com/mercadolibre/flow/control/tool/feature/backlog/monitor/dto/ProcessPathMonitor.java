package com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto;

import com.mercadolibre.flow.control.tool.feature.entity.ProcessPath;

public record ProcessPathMonitor(
    ProcessPath name,
    Integer quantity
) {
}
