package com.mercadolibre.flow.control.tool.client.planningmodelapi.adapter;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

import com.mercadolibre.flow.control.tool.client.planningmodelapi.PlanningModelApiClient;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.EntityType;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.OutboundProcessName;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.PlanningWorkflow;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.ProcessingType;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.EntityDataDto;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.EntityRequestDto;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.GetBacklogLimitGatewayAux;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.BacklogLimit;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.ProcessLimit;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BacklogLimitAdapter implements GetBacklogLimitGatewayAux {

  public static final List<EntityType> ENTITY_TYPES = List.of(EntityType.BACKLOG_UPPER_LIMIT, EntityType.BACKLOG_LOWER_LIMIT);
  final PlanningModelApiClient planningModelApiClient;

  @Override
  public List<BacklogLimit> getBacklogLimits(final String logisticCenterId,
                                             final PlanningWorkflow workflow,
                                             final List<OutboundProcessName> processes,
                                             final Instant dateFrom,
                                             final Instant dateTo) {


    final EntityRequestDto entityRequestDto =
        new EntityRequestDto(workflow, ENTITY_TYPES, logisticCenterId, dateFrom, dateTo, processes, Collections.emptyMap());

    final Map<EntityType, List<EntityDataDto>> entityResponse = planningModelApiClient.searchEntities(entityRequestDto);

    return entityResponse.entrySet()
        .stream()
        .flatMap(entity -> entity.getValue().stream())
        .collect(
            groupingBy(
                EntityDataDto::getDate,
                groupingBy(
                    EntityDataDto::getProcessName,
                    toMap(EntityDataDto::getType, EntityDataDto::getValue)
                )
            )
        )
        .entrySet()
        .stream()
        .map(entity -> new BacklogLimit(
                entity.getKey(),
                entity.getValue().entrySet().stream()
                    .map(process ->
                        new ProcessLimit(
                            process.getKey().translateProcessName(),
                            process.getValue().get(ProcessingType.BACKLOG_LOWER_LIMIT),
                            process.getValue().get(ProcessingType.BACKLOG_UPPER_LIMIT)
                        )
                    ).toList()
            )
        ).toList();
  }
}
