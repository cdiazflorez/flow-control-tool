package com.mercadolibre.flow.control.tool.client.planningmodelapi.adapter;

import static com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.EntityFilter.ABILITY_LEVEL;
import static com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.EntityFilter.PROCESSING_TYPE;
import static com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.EntityType.HEADCOUNT;
import static com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.EntityType.PRODUCTIVITY;
import static com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.EntityType.THROUGHPUT;
import static com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.ProcessingType.EFFECTIVE_WORKERS;
import static com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.ProcessingType.EFFECTIVE_WORKERS_NS;
import static com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.Source.FORECAST;
import static com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.Source.SIMULATION;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.mercadolibre.fbm.wms.outbound.commons.rest.exception.ClientException;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.PlanningModelApiClient;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.EntityType;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.OutboundProcessName;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.PlanningWorkflow;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.ProcessingType;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.Source;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.EntityDataDto;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.EntityRequestDto;
import com.mercadolibre.flow.control.tool.exception.ForecastNotFoundException;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import com.mercadolibre.flow.control.tool.feature.staffing.constant.StaffingType;
import com.mercadolibre.flow.control.tool.feature.staffing.domain.StaffingPlannedData;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StaffingPlanAdapter {

  private static final String MAIN_ABILITY_LEVEL = "1";

  private static final Map<EntityType, Map<String, List<String>>> ENTITY_FILTERS = Map.of(
      HEADCOUNT, Map.of(
          PROCESSING_TYPE.getName(), List.of(
              EFFECTIVE_WORKERS.getName(),
              EFFECTIVE_WORKERS_NS.getName()
          )
      ),
      PRODUCTIVITY, Map.of(
          ABILITY_LEVEL.getName(), List.of(MAIN_ABILITY_LEVEL)
      )
  );

  private static final List<EntityType> ENTITY_TYPES = List.of(
      HEADCOUNT,
      PRODUCTIVITY,
      THROUGHPUT
  );

  private static final Map<EntityType, Function<List<EntityDataDto>, List<StaffingPlannedData>>> GROUPER_STRATEGY_BY_ENTITY_TYPE =
      Map.of(
          HEADCOUNT, StaffingPlanAdapter::headcountGrouper,
          PRODUCTIVITY, StaffingPlanAdapter::productivityGrouper,
          THROUGHPUT, StaffingPlanAdapter::nonGrouper
      );

  private static final Map<PlanningWorkflow, List<OutboundProcessName>> PROCESS_NAME_BY_PLANNING_WORKFLOW = Map.of(
      PlanningWorkflow.FBM_WMS_OUTBOUND, Arrays.stream(OutboundProcessName.values()).toList()
  );

  private final PlanningModelApiClient planningModelApiClient;

  public Map<StaffingType, List<StaffingPlannedData>> getStaffingPlanned(final Workflow workflow,
                                                                         final String logisticCenter,
                                                                         final Instant dateFrom,
                                                                         final Instant dateTo) {

    final PlanningWorkflow planningWorkflow = PlanningWorkflow.from(workflow.getName());

    final EntityRequestDto entityRequest = new EntityRequestDto(
        planningWorkflow,
        ENTITY_TYPES,
        logisticCenter,
        dateFrom,
        dateTo,
        PROCESS_NAME_BY_PLANNING_WORKFLOW.get(planningWorkflow),
        ENTITY_FILTERS
    );

    try {
      final Map<EntityType, List<EntityDataDto>> entityResponse = planningModelApiClient.searchEntities(entityRequest);

      return entityResponse.entrySet().stream()
          .collect(
              toMap(
                  k -> StaffingType.from(k.getKey().getName()),
                  v -> buildStaffingPlannedData(v.getKey(), v.getValue())
              )
          );
    } catch (ClientException ex) {
      if (forecastNotFound(ex)) {
        throw new ForecastNotFoundException(logisticCenter, workflow.getName(), ex);
      } else {
        throw ex;
      }
    }
  }

  private List<StaffingPlannedData> buildStaffingPlannedData(final EntityType entityType,
                                                             final List<EntityDataDto> entitiesData) {
    return GROUPER_STRATEGY_BY_ENTITY_TYPE.get(entityType).apply(entitiesData);
  }

  private static List<StaffingPlannedData> headcountGrouper(final List<EntityDataDto> entitiesData) {
    final var entityDataByDateAndProcess = groupEntitiesByDateAndProcess(entitiesData);
    return entityDataByDateAndProcess.entrySet().stream()
        .flatMap(outboundProcess -> outboundProcess.getValue().entrySet().stream()
            .map(date -> {
              final boolean isEffectiveWorkersSimulated = date.getValue().stream()
                  .anyMatch(
                      filterEntityDataByProcessingType(EFFECTIVE_WORKERS)
                          .and(filterEntityDataBySource(SIMULATION))
                  );
              final boolean isEffectiveWorkersNsSimulated = date.getValue().stream()
                  .anyMatch(
                      filterEntityDataByProcessingType(EFFECTIVE_WORKERS_NS)
                          .and(filterEntityDataBySource(SIMULATION))
                  );

              final var filterWorkers = isEffectiveWorkersSimulated
                  ? filterEntityDataByProcessingType(EFFECTIVE_WORKERS).and(filterEntityDataBySource(SIMULATION))
                  : filterEntityDataByProcessingType(EFFECTIVE_WORKERS);
              final var filterWorkersNs = isEffectiveWorkersNsSimulated
                  ? filterEntityDataByProcessingType(EFFECTIVE_WORKERS_NS).and(filterEntityDataBySource(SIMULATION))
                  : filterEntityDataByProcessingType(EFFECTIVE_WORKERS_NS);

              final long effectiveWorkers = getValueOfEntityData(date.getValue(), filterWorkers);
              final long effectiveWorkersNs = getValueOfEntityData(date.getValue(), filterWorkersNs);

              return new StaffingPlannedData(date.getKey(),
                                             outboundProcess.getKey().translateProcessName(),
                                             effectiveWorkers,
                                             effectiveWorkersNs,
                                             isEffectiveWorkersSimulated,
                                             isEffectiveWorkersNsSimulated);
            })
        ).toList();

  }

  private static List<StaffingPlannedData> productivityGrouper(final List<EntityDataDto> entitiesData) {
    final var entityDataByDateAndProcess = groupEntitiesByDateAndProcess(entitiesData);
    return entityDataByDateAndProcess.entrySet().stream()
        .flatMap(outboundProcess -> outboundProcess.getValue().entrySet().stream()
            .map(date -> {
              final boolean isSimulated = date.getValue().stream()
                  .anyMatch(filterEntityDataBySource(SIMULATION));
              final long productivity = isSimulated
                  ? getValueOfEntityData(date.getValue(), filterEntityDataBySource(SIMULATION))
                  : getValueOfEntityData(date.getValue(), filterEntityDataBySource(FORECAST));
              return new StaffingPlannedData(date.getKey(),
                                             outboundProcess.getKey().translateProcessName(),
                                             productivity,
                                             0,
                                             isSimulated,
                                             false
              );
            })
        ).toList();
  }

  private static List<StaffingPlannedData> nonGrouper(final List<EntityDataDto> entitiesData) {
    return entitiesData.stream()
        .map(entityData -> new StaffingPlannedData(entityData.getDate(),
                                                   entityData.getProcessName().translateProcessName(),
                                                   entityData.getValue(),
                                                   0,
                                                   false,
                                                   false)
        ).toList();
  }

  private static long getValueOfEntityData(final List<EntityDataDto> entitiesData, final Predicate<EntityDataDto> entityDataFilter) {
    return entitiesData.stream()
        .filter(entityDataFilter)
        .findFirst()
        .map(EntityDataDto::getValue)
        .orElse(0L);
  }

  private static Predicate<EntityDataDto> filterEntityDataBySource(final Source source) {
    return entityData -> entityData.getSource().equals(source);
  }

  private static Predicate<EntityDataDto> filterEntityDataByProcessingType(final ProcessingType processingType) {
    return entityData -> entityData.getType().equals(processingType);
  }

  private boolean forecastNotFound(ClientException ex) {
    return ex.getResponseStatus() == NOT_FOUND.value()
        && ex.getResponseBody().contains("forecast_not_found");
  }

  private static Map<OutboundProcessName, Map<Instant, List<EntityDataDto>>> groupEntitiesByDateAndProcess(
      final List<EntityDataDto> entities
  ) {
    return entities.stream()
        .collect(
            groupingBy(
                EntityDataDto::getProcessName,
                groupingBy(EntityDataDto::getDate)
            )
        );
  }

}
