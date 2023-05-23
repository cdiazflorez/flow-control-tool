package com.mercadolibre.flow.control.tool.client;

import com.mercadolibre.flow.control.tool.client.planningmodelapi.PlanningModelApiClient;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.OutboundProcessName;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.BacklogProjectedUseCase;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.BacklogProjectedUseCase.PlannedEntitiesGateway;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Adapter for Planning Model API Client entities.  This class is used to get the throughput and planned backlog.
 */
@AllArgsConstructor
@Component
public class PlannedEntitiesAdapter
    implements PlannedEntitiesGateway, BacklogProjectedUseCase.BacklogProjectionGateway {

  private static final Set<ProcessPathName> PROCESS_PATH_GLOBAL = Set.of(ProcessPathName.GLOBAL);

  private final PlanningModelApiClient planningModelApiClient;

  /**
   * Retrieves the sum of the throughput of the selected Process Paths grouped by Process Name and Date.
   *
   * @param workflow         The workflow for which to retrieve the throughput.
   * @param logisticCenterId The ID of the logistic center to which the throughput belongs.
   * @param dateFrom         The start date of the time period.
   * @param dateTo           The end date of the time period.
   * @param process          The set of process names for which to retrieve the throughput.
   * @return A map containing the throughput by date, process name, and the total quantity.
   */
  @Override
  public Map<Instant, Map<ProcessName, Integer>> getThroughput(
      final Workflow workflow,
      final String logisticCenterId,
      final Instant dateFrom,
      final Instant dateTo,
      final Set<ProcessName> process
  ) {

    final Map<ProcessPathName, Map<OutboundProcessName, Map<Instant, PlanningModelApiClient.Throughput>>> throughput =
        planningModelApiClient.getThroughputByPPAndProcessAndDate(
            workflow,
            logisticCenterId,
            dateFrom,
            dateTo,
            process,
            PROCESS_PATH_GLOBAL
        );

    return throughput.get(ProcessPathName.GLOBAL)
        .entrySet()
        .stream()
        .flatMap(tphSlaByProcessNameEntry -> tphSlaByProcessNameEntry.getValue()
            .entrySet()
            .stream()
            .map(tphSla -> new ThroughputAux(
                tphSla.getKey(),
                tphSlaByProcessNameEntry.getKey().translateProcessName(),
                tphSla.getValue().quantity())
            )
        )
        .collect(Collectors.groupingBy(ThroughputAux::date, Collectors.groupingBy(
            ThroughputAux::processName, Collectors.summingInt(ThroughputAux::total)))
        );
  }

  @Override
  public List<BacklogProjectedUseCase.PlannedBacklog> getPlannedBacklog(Workflow workflow, String logisticCenterId, Instant dateFrom,
                                                                        Instant dateTo) {
    return Collections.emptyList();
  }

  @Override
  public Map<Instant, Map<ProcessName, Map<Instant, Map<ProcessPathName, Integer>>>> executeBacklogProjection(
      Instant dateFrom,
      Instant dateTo,
      Set<ProcessName> process,
      Map<ProcessName, Map<ProcessPathName, Map<Instant, Integer>>> currentBacklogs,
      Map<Instant, Map<ProcessName, Integer>> throughput,
      List<BacklogProjectedUseCase.PlannedBacklog> plannedBacklogs) {
    return Collections.emptyMap();
  }

  public record ThroughputAux(
      Instant date,
      ProcessName processName,
      Integer total
  ) {
  }

}
