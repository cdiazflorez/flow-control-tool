package com.mercadolibre.flow.control.tool.feature.backlog.status;

import java.util.Map;

public record BacklogStatus(
    Map<String, Integer> backlogStatus,
    Double unitsPerOrderRatio
) {
}
