package com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto;

import com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName;

public record ProcessPathMonitor(
    ProcessPathName name,
    Integer quantity
) {
}
