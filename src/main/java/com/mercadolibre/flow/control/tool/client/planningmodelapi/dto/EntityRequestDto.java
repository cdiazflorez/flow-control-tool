package com.mercadolibre.flow.control.tool.client.planningmodelapi.dto;

import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.EntityType;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.OutboundProcessName;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.PlanningWorkflow;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public record EntityRequestDto(
    PlanningWorkflow workflow,
    List<EntityType> entityTypes,
    String warehouseId,
    Instant dateFrom,
    Instant dateTo,
    List<OutboundProcessName> processName,
    Map<EntityType, Map<String, List<String>>> entityFilters
) {
}
