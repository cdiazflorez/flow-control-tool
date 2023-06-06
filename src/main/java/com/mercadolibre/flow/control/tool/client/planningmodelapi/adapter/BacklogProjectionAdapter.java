package com.mercadolibre.flow.control.tool.client.planningmodelapi.adapter;

import com.mercadolibre.fbm.wms.outbound.commons.rest.exception.ClientException;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.PlanningModelApiClient;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.OutboundProcessName;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.PlanningWorkflow;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.BacklogProjectionRequest;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.BacklogProjectionRequest.Backlog.Process;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.BacklogProjectionRequest.Backlog.Process.ProcessPathByDateOut;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.BacklogProjectionRequest.Backlog.Process.ProcessPathByDateOut.QuantityByDateOut;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.BacklogProjectionRequest.PlannedUnit.ProcessPathByDateInOut;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.BacklogProjectionRequest.PlannedUnit.ProcessPathByDateInOut.QuantityByDateInOut;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.BacklogProjectionRequest.Throughput;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.BacklogProjectionRequest.Throughput.QuantityByProcessName;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.BacklogProjectionResponse;
import com.mercadolibre.flow.control.tool.exception.ProjectionInputsNotFoundException;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.BacklogProjectedUseCase;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BacklogProjectionAdapter implements BacklogProjectedUseCase.BacklogProjectionGateway {

  private final PlanningModelApiClient planningModelApiClient;


  /**
   * This method use each given map to build a BacklogProjectionRequest DTO which is used to ask PAPI Client,
   * to obtain A Backlog Projection opened by Process.
   *
   * @param logisticCenterId Logistic center to be queried
   * @param dateFrom         date from to query
   * @param dateTo           date To to query
   * @param process          processes to be queried
   * @param currentBacklogs  current real backlog opened by Process, PP and date_out
   * @param plannedUnit      forecasted (planned) backlog opened by PP, date_in, and date_out
   * @param throughput       througput by operative_hour and process
   * @return A Backlog Map opened by operative_hour, process and date_out to build a BacklogMonitor
   */
  @Override
  public Map<Instant, Map<ProcessName, Map<Instant, Integer>>> executeBacklogProjection(
      final String logisticCenterId,
      final Instant dateFrom,
      final Instant dateTo,
      final Set<ProcessName> process,
      final Map<ProcessName, Map<ProcessPathName, Map<Instant, Integer>>> currentBacklogs,
      final Map<ProcessPathName, Map<Instant, Map<Instant, Integer>>> plannedUnit,
      final Map<Instant, Map<ProcessName, Integer>> throughput
  ) {

    final BacklogProjectionRequest.Backlog backlogForProjection = new BacklogProjectionRequest.Backlog(
        getProcessesSetForBacklogInProjectionRequest(currentBacklogs)
    );

    final BacklogProjectionRequest.PlannedUnit plannedUnitForProjection = new BacklogProjectionRequest.PlannedUnit(
        getProcessPathsSetForPlannedUnitInProjectionRequest(plannedUnit)
    );

    final Set<BacklogProjectionRequest.Throughput> throughputForProjection = getThroughputSetForProjectionRequest(throughput);

    try {
      final List<BacklogProjectionResponse> backlogProjection = planningModelApiClient.getBacklogProjection(
          logisticCenterId,
          new BacklogProjectionRequest(
              backlogForProjection,
              plannedUnitForProjection,
              throughputForProjection,
              dateFrom,
              dateTo,
              PlanningWorkflow.FBM_WMS_OUTBOUND
          )
      );

      if (backlogProjection == null) {
        return Collections.emptyMap();
      }

      return getMapByOperationHourProcessAndDateOutFromBacklogProjection(backlogProjection);
    } catch (ClientException ce) {
      throw new ProjectionInputsNotFoundException("Projection", logisticCenterId, PlanningWorkflow.FBM_WMS_OUTBOUND.getName(), ce);
    }
  }

  /**
   * This method creates a Set of Processes for (current) Backlog in BacklogProjectionRequest.
   *
   * @param currentBacklogs map of backlogs by process, pp and date_out
   * @return Set of BacklogProjectionRequest.Process
   */
  private static Set<Process> getProcessesSetForBacklogInProjectionRequest(
      final Map<ProcessName, Map<ProcessPathName, Map<Instant, Integer>>> currentBacklogs
  ) {

    return currentBacklogs.entrySet().stream().map(
        processNameMapEntry -> getProcessForBacklogInProjectionRequest(
            processNameMapEntry.getKey(),
            processNameMapEntry.getValue()
        )
    ).collect(Collectors.toSet());
  }

  /**
   * This method creates the BacklogProjectionRequest.Process needed to build BacklogProjectionRequest.Backlog (without Process.total),
   * based on PAPI contract definition.
   *
   * @param processName    ProcessName for Process.name
   * @param processPathMap map of backlogs by pp and date_out
   * @return BacklogProjectionRequest.Process where Process(total=null)
   */
  private static Process getProcessForBacklogInProjectionRequest(
      final ProcessName processName,
      final Map<ProcessPathName, Map<Instant, Integer>> processPathMap
  ) {
    return new BacklogProjectionRequest.Backlog.Process(
        OutboundProcessName.fromProcessName(processName),
        getProcessPathsSetForBacklogInProjectionRequest(processPathMap)
    );
  }

  /**
   * This method creates the Set of BacklogProjectionRequest.Backlog.Process.ProcessPathDetail,
   * needed to build BacklogProjectionRequest.Backlog,
   * based on PAPI contract definition.
   *
   * @param processPathMap map of backlog by PP Date out
   * @return Set of BacklogProjectionRequest.ProcessPath
   */
  private static Set<ProcessPathByDateOut> getProcessPathsSetForBacklogInProjectionRequest(
      final Map<ProcessPathName, Map<Instant, Integer>> processPathMap
  ) {
    return processPathMap.entrySet().stream().map(
        processPathNameEntry -> getProcessPathForProcessInProjectionRequest(
            processPathNameEntry.getKey(),
            processPathNameEntry.getValue()
        )
    ).collect(Collectors.toSet());
  }

  /**
   * This method creates BacklogProjectionRequest.Backlog.Process.ProcessPathDetail,
   * needed to build BacklogProjectionRequest.Backlog (date_in=null),
   * based on PAPI contract definition.
   *
   * @param processPathName name for BacklogProjectionRequest.ProcessPath
   * @param quantityMap     Map of backlog quantities by date_out
   * @return BacklogProjectionRequest.Backlog.Process.ProcessPathDetail
   */
  private static ProcessPathByDateOut getProcessPathForProcessInProjectionRequest(
      final ProcessPathName processPathName,
      final Map<Instant, Integer> quantityMap
  ) {
    return new ProcessPathByDateOut(
        processPathName,
        quantityMap.entrySet().stream().map(
            dateEntry -> getQuantityByDateInBacklogProjectionRequest(
                dateEntry.getKey(),
                dateEntry.getValue()
            )
        ).collect(Collectors.toSet())
    );
  }

  /**
   * This method creates a BacklogProjectionRequest.Backlog.Process.ProcessPathDetail.QuantityByDate
   *
   * @param dateOut date out
   * @param total   total
   * @return BacklogProjectionRequest.Backlog.Process.ProcessPathDetail.QuantityByDate
   */
  private static QuantityByDateOut getQuantityByDateInBacklogProjectionRequest(
      final Instant dateOut,
      final Integer total
  ) {
    return new QuantityByDateOut(
        dateOut,
        total
    );
  }

  /**
   * This method creates a Set of ProcessPaths for PlannedUnit in BacklogProjectionRequest.
   *
   * @param plannedUnit map of backlogs by pp, date_in and date_out
   * @return Set of BacklogProjectionRequest.PlannedUnit.ProcessPathRequest
   */
  private static Set<ProcessPathByDateInOut> getProcessPathsSetForPlannedUnitInProjectionRequest(
      final Map<ProcessPathName, Map<Instant, Map<Instant, Integer>>> plannedUnit
  ) {

    return plannedUnit.entrySet().stream().map(
        processPathNameEntry -> getProcessPathForPlannedUnitInProjectionRequest(
            processPathNameEntry.getKey(),
            processPathNameEntry.getValue()
        )
    ).collect(Collectors.toSet());
  }

  /**
   * This method creates BacklogProjectionRequest.PlannedUnit.ProcessPathRequest needed to build BacklogProjectionRequest.PlannedUnit,
   * based on PAPI contract definition.
   *
   * @param processPathName      name for BacklogProjectionRequest.PlannedUnit.ProcessPathRequest
   * @param dateInOutQuantityMap Map of backlog quantities by date_in and date_out
   * @return BacklogProjectionRequest.PlannedUnit.ProcessPathRequest
   */
  private static ProcessPathByDateInOut getProcessPathForPlannedUnitInProjectionRequest(
      final ProcessPathName processPathName,
      final Map<Instant, Map<Instant, Integer>> dateInOutQuantityMap
  ) {
    return new ProcessPathByDateInOut(
        processPathName,
        dateInOutQuantityMap.entrySet().stream()
            .flatMap(
                dataEntry -> dataEntry.getValue().entrySet().stream().map(
                    quantityEntry -> getQuantityInBacklogProjectionRequest(
                        dataEntry.getKey(),
                        quantityEntry.getKey(),
                        quantityEntry.getValue()
                    )
                )
            ).collect(Collectors.toSet())
    );
  }

  /**
   * This method creates a BacklogProjectionRequest.PlannedUnit.ProcessPathRequest.Quantity
   *
   * @param dateIn  date in
   * @param dateOut date out
   * @param total   total
   * @return BacklogProjectionRequest.PlannedUnit.ProcessPathRequest.Quantity
   */
  private static QuantityByDateInOut getQuantityInBacklogProjectionRequest(
      final Instant dateIn,
      final Instant dateOut,
      final Integer total
  ) {
    return new QuantityByDateInOut(
        dateIn,
        dateOut,
        total
    );
  }

  /**
   * This method creates a Set of Throughput for BacklogProjectionRequest.
   *
   * @param throughput map of backlogs tph by operation_hour and process
   * @return Set of BacklogProjectionRequest.Throughput
   */
  private static Set<Throughput> getThroughputSetForProjectionRequest(
      final Map<Instant, Map<ProcessName, Integer>> throughput
  ) {

    return throughput.entrySet().stream().map(
        tphEntry -> new Throughput(
            tphEntry.getKey(),
            getProcessesSetForThroughputInProjectionRequest(tphEntry.getValue())
        )
    ).collect(Collectors.toSet());
  }

  /**
   * This method creates a BacklogProjectionRequest.Throughput.QuantityByProcessName,
   * needed to build BacklogProjectionRequest.Throughput,
   * based on PAPI contract definition.
   *
   * @param tphByProcess map of tph by process
   * @return Set of BacklogProjectionRequest.Throughput.QuantityByProcessName
   */
  private static Set<QuantityByProcessName> getProcessesSetForThroughputInProjectionRequest(
      final Map<ProcessName, Integer> tphByProcess
  ) {
    return tphByProcess.entrySet().stream().map(
        processEntry -> new QuantityByProcessName(
            OutboundProcessName.fromProcessName(processEntry.getKey()),
            processEntry.getValue()
        )
    ).collect(Collectors.toSet());

  }

  /**
   * This method creates a map by Process and date_out. Transforming the Outbound process from Response to Process.
   * The method must change when the projection comes opened by PP, summarizing each PP quantity value if total is removed.
   *
   * @param projectionResponseBacklog List of BacklogProjectionResponse.Backlog
   * @return Map of processName and date_out
   */
  private static Map<ProcessName, Map<Instant, Integer>> getBacklogByProcessDateOutFromProjectionResponseBacklog(
      final List<BacklogProjectionResponse.Backlog> projectionResponseBacklog
  ) {
    return projectionResponseBacklog.stream()
        .flatMap(backlog -> backlog.process().stream())
        .collect(Collectors.toMap(
            processEntry -> processEntry.name().translateProcessName(),
            processEntry -> processEntry.sla().stream()
                .collect(Collectors.toMap(
                    BacklogProjectionResponse.Sla::dateOut,
                    BacklogProjectionResponse.Sla::quantity
                ))
        ));
  }

  /**
   * This method creates a Map by operation_hour, process and date_out, used to build a BacklogMonitor.
   *
   * @param backlogProjection List of BacklogProjectionResponse
   * @return Map of operationHour, processName and date_out
   */
  private Map<Instant, Map<ProcessName, Map<Instant, Integer>>> getMapByOperationHourProcessAndDateOutFromBacklogProjection(
      final List<BacklogProjectionResponse> backlogProjection
  ) {

    return backlogProjection.stream()
        .collect(Collectors.toMap(
            BacklogProjectionResponse::operationHour,
            backlogEntry -> getBacklogByProcessDateOutFromProjectionResponseBacklog(backlogEntry.backlog())
        ));
  }
}
