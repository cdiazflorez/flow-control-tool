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
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.BacklogLimitsUseCase.GetBacklogLimitGateway;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BacklogLimitAdapter implements GetBacklogLimitGateway {

  public static final List<EntityType> ENTITY_TYPES = List.of(EntityType.BACKLOG_UPPER_LIMIT, EntityType.BACKLOG_LOWER_LIMIT);

  final PlanningModelApiClient planningModelApiClient;


  @Override
  public Map<Instant, Map<OutboundProcessName, Map<ProcessingType, Long>>> getBacklogLimitsEntityDataMap(
      final String logisticCenterId,
      final Workflow workflow,
      final Set<ProcessName> processes,
      final Instant dateFrom,
      final Instant dateTo
  ) {
    final List<OutboundProcessName> outboundProcessNames = processes.stream()
        .map(OutboundProcessName::fromProcessName)
        .sorted()
        .toList();

    final EntityRequestDto entityRequestDto =
        new EntityRequestDto(
            PlanningWorkflow.from(workflow.getName()),
            ENTITY_TYPES,
            logisticCenterId,
            dateFrom,
            dateTo,
            outboundProcessNames,
            Collections.emptyMap()
        );

    final Map<EntityType, List<EntityDataDto>> entityResponse = planningModelApiClient.searchEntities(entityRequestDto);

    return groupEntityDataByDateProcessAndType(entityResponse);
  }

  /**
   * Groups the entity data by date, process, and type.
   *
   * @param entityTypeListMap the entity response to be grouped
   * @return a map containing the grouped entity data by date, process, and type
   */
  private Map<Instant, Map<OutboundProcessName, Map<ProcessingType, Long>>> groupEntityDataByDateProcessAndType(
      final Map<EntityType, List<EntityDataDto>> entityTypeListMap
  ) {
    return entityTypeListMap.entrySet().stream()
        .flatMap(entityEntry -> entityEntry.getValue().stream())
        .collect(groupingBy(
            EntityDataDto::getDate,
            groupingBy(
                EntityDataDto::getProcessName,
                toMap(
                    EntityDataDto::getType,
                    EntityDataDto::getValue
                )
            )
        ));
  }
}
