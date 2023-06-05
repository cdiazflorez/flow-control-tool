package com.mercadolibre.flow.control.tool.client.planningmodelapi.adapter;

import static com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.PlannedGrouper.DATE_IN;
import static com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.PlannedGrouper.DATE_OUT;
import static com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.PlannedGrouper.PROCESS_PATH;

import com.mercadolibre.fbm.wms.outbound.commons.rest.exception.ClientException;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.PlanningModelApiClient;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.OutboundProcessName;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.PlannedGrouper;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.PlanningWorkflow;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.BacklogPlannedRequest;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.BacklogPlannedResponse;
import com.mercadolibre.flow.control.tool.exception.ProjectionInputsNotFoundException;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.PlannedEntitiesGateway;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Adapter for Planning Model API Client entities.  This class is used to get the throughput and planned backlog.
 */
@AllArgsConstructor
@Component
public class PlannedEntitiesAdapter implements PlannedEntitiesGateway {

  private static final Set<ProcessPathName> PROCESS_PATH_GLOBAL = Set.of(ProcessPathName.GLOBAL);

  private static final Set<PlannedGrouper> PLANNED_GROUPERS = Set.of(
      PROCESS_PATH,
      DATE_IN,
      DATE_OUT
  );

  private static final Set<ProcessPathName> PROCESS_PATH_NAMES = new HashSet<>(ProcessPathName.allPaths());

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
  public Map<Instant, Map<ProcessName, Integer>> getThroughputByDateAndProcess(
      final Workflow workflow,
      final String logisticCenterId,
      final Instant dateFrom,
      final Instant dateTo,
      final Set<ProcessName> process
  ) {

    try {
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
    } catch (ClientException ce) {
      final String throughput = "Throughput";
      throw new ProjectionInputsNotFoundException(throughput, logisticCenterId, workflow.getName(), ce);
    }
  }

  @Override
  public Map<ProcessPathName, Map<Instant, Map<Instant, Integer>>> getPlannedUnitByPPDateInAndDateOut(
      final Workflow workflow,
      final String logisticCenterId,
      final Instant dateFrom,
      final Instant dateTo
  ) {

    try {
      final List<BacklogPlannedResponse> backlogPlannedResponses = planningModelApiClient.getBacklogPlanned(new BacklogPlannedRequest(
          logisticCenterId,
          PlanningWorkflow.from(workflow.getName()),
          PROCESS_PATH_NAMES,
          dateFrom,
          dateTo,
          Optional.empty(),
          Optional.empty(),
          PLANNED_GROUPERS));

      return backlogPlannedResponses.stream().collect(
          Collectors.groupingBy(
              grouper -> grouper.group().processPath(),
              Collectors.groupingBy(
                  grouper -> grouper.group().dateIn(),
                  Collectors.groupingBy(
                      grouper -> grouper.group().dateOut(),
                      Collectors.summingInt(response -> Math.toIntExact(Math.round(response.total()))
                      )
                  )
              )
          )
      );
    } catch (ClientException ce) {
      throw new ProjectionInputsNotFoundException("Forecast sales", logisticCenterId, workflow.getName(), ce);
    }
  }

  public record ThroughputAux(
      Instant date,
      ProcessName processName,
      Integer total
  ) {
  }
}
