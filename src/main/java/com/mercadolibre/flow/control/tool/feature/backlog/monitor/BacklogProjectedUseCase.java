package com.mercadolibre.flow.control.tool.feature.backlog.monitor;

import com.mercadolibre.flow.control.tool.exception.NoUnitsPerOrderRatioFound;
import com.mercadolibre.flow.control.tool.feature.backlog.genericgateway.UnitsPerOrderRatioGateway;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.domain.PlannedBacklog;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.BacklogMonitor;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class BacklogProjectedUseCase {

  private static final double MIN_VALUE_FOR_UNIT_PER_ORDER_RATIO = 1;

  private final BacklogGateway backlogApiGateway;

  private final PlannedEntitiesGateway plannedEntitiesGateway;

  private final BacklogProjectionGateway backlogProjectionGateway;

  private final UnitsPerOrderRatioGateway unitsPerOrderRatioGateway;

  public List<BacklogMonitor> getBacklogProjected(
      final Instant dateFrom,
      final Instant dateTo,
      final String logisticCenterId,
      final Workflow workflow,
      final Set<ProcessName> processes,
      final Instant viewDate) {
    final var currentBacklog = backlogApiGateway.getBacklogTotalsByProcessAndPPandSla(logisticCenterId, workflow, processes, dateFrom);

    final var tph = plannedEntitiesGateway.getThroughput(workflow, logisticCenterId, dateFrom, dateTo, processes);

    final var plannedBacklog = plannedEntitiesGateway.getPlannedBacklog(workflow, logisticCenterId, dateFrom, dateTo);

    final var backlogProjection =
        backlogProjectionGateway.executeBacklogProjection(dateFrom, dateTo, processes, currentBacklog, tph, plannedBacklog);

    final Optional<Double> getUnitsPerOrderRatio =
        unitsPerOrderRatioGateway.getUnitsPerOrderRatio(workflow, logisticCenterId, viewDate);

    final Double unitsPerOrderRatio = getUnitsPerOrderRatio
        .filter(ratio -> ratio >= MIN_VALUE_FOR_UNIT_PER_ORDER_RATIO)
        .orElseThrow(
            () -> new NoUnitsPerOrderRatioFound(logisticCenterId)
        );

    List<BacklogMonitor> backlogMonitors = BacklogProjectionUtil.sumBacklogProjection(backlogProjection, unitsPerOrderRatio);

    BacklogProjectionUtil.order(backlogMonitors);

    return backlogMonitors;
  }


  /**
   * Interface for methods used across the Backlog Status.
   */
  public interface BacklogGateway {

    /**
     * The implementation should return the backlog given by process and its total units|orders.
     *
     * @param logisticCenterId logistic center id.
     * @param workflow         outbound
     * @param processes        list of processes to be requested.
     * @param viewDate         base date to backlog.
     * @return map with amount of units by each process.
     */
    Map<ProcessName, Map<ProcessPathName, Map<Instant, Integer>>> getBacklogTotalsByProcessAndPPandSla(
        String logisticCenterId,
        Workflow workflow,
        Set<ProcessName> processes,
        Instant viewDate
    );
  }

  /**
   * Gateway planning api to obtain tph, plannedBacklog.
   */
  public interface PlannedEntitiesGateway {
    Map<Instant, Map<ProcessName, Integer>> getThroughput(Workflow workflow,
                                                          String logisticCenterId,
                                                          Instant dateFrom,
                                                          Instant dateTo,
                                                          Set<ProcessName> process);

    List<PlannedBacklog> getPlannedBacklog(Workflow workflow, String logisticCenterId, Instant dateFrom, Instant dateTo);
  }

  /**
   * Gateway planning api to execute backlog projection.
   */
  public interface BacklogProjectionGateway {
    Map<Instant, Map<ProcessName, Map<Instant, Map<ProcessPathName, Integer>>>> executeBacklogProjection(
        Instant dateFrom,
        Instant dateTo,
        Set<ProcessName> process,
        Map<ProcessName, Map<ProcessPathName, Map<Instant, Integer>>> currentBacklogs,
        Map<Instant, Map<ProcessName, Integer>> throughput,
        List<PlannedBacklog> plannedBacklogs);
  }

}
