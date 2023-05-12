package com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto;

import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;

public record ProcessLimit(
    ProcessName name,
    Long lower,
    Long upper
) {

}
