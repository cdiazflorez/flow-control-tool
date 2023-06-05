package com.mercadolibre.flow.control.tool.feature.backlog.monitor;

import static com.mercadolibre.flow.control.tool.feature.backlog.monitor.BacklogProjectionUtil.fillBacklogMonitorsMissingDatesAndProcesses;
import static com.mercadolibre.flow.control.tool.feature.backlog.monitor.BacklogProjectionUtil.validateUnitsPerOrderRatio;

import com.mercadolibre.flow.control.tool.feature.backlog.genericgateway.UnitsPerOrderRatioGateway;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.BacklogMonitor;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class BacklogProjectedUseCase {


  private final BacklogProjectedGateway backlogProjectedGateway;

  private final PlannedEntitiesGateway plannedEntitiesGateway;

  private final BacklogProjectionGateway backlogProjectionGateway;

  private final UnitsPerOrderRatioGateway unitsPerOrderRatioGateway;

  public List<BacklogMonitor> getBacklogProjected(
      final Instant dateFrom,
      final Instant dateTo,
      final String logisticCenterId,
      final Workflow workflow,
      final Set<ProcessName> processes,
      final Instant viewDate
  ) {
    final var plannedBacklog = plannedEntitiesGateway.getPlannedUnitByPPDateInAndDateOut(
        workflow,
        logisticCenterId,
        dateFrom,
        dateTo
    );

    final var currentBacklog = backlogProjectedGateway.getBacklogTotalsByProcessAndPPandSla(
        logisticCenterId,
        workflow,
        processes,
        dateFrom
    );

    final var tph = plannedEntitiesGateway.getThroughputByDateAndProcess(
        workflow,
        logisticCenterId,
        dateFrom,
        dateTo,
        processes
    );

    final var backlogProjection = backlogProjectionGateway.executeBacklogProjection(
        logisticCenterId,
        dateFrom,
        dateTo,
        processes,
        currentBacklog,
        plannedBacklog,
        tph
    );

    final Optional<Double> getUnitsPerOrderRatio = unitsPerOrderRatioGateway.getUnitsPerOrderRatio(
        workflow,
        logisticCenterId,
        viewDate
    );

    final Double unitsPerOrderRatio = validateUnitsPerOrderRatio(getUnitsPerOrderRatio, logisticCenterId);

    final List<BacklogMonitor> backlogMonitors = fillBacklogMonitorsMissingDatesAndProcesses(
        BacklogProjectionUtil.sumBacklogProjectionEmptyPP(backlogProjection, unitsPerOrderRatio),
        dateFrom.truncatedTo(ChronoUnit.HOURS).plus(Duration.ofHours(1)),
        dateTo
    );

    return BacklogProjectionUtil.orderMonitorsByDate(backlogMonitors);
  }

  /**
   * Gateway planning api to execute backlog projection.
   */
  public interface BacklogProjectionGateway {

    Map<Instant, Map<ProcessName, Map<Instant, Integer>>> executeBacklogProjection(
        String logisticCenterId,
        Instant dateFrom,
        Instant dateTo,
        Set<ProcessName> process,
        Map<ProcessName, Map<ProcessPathName, Map<Instant, Integer>>> currentBacklogs,
        Map<ProcessPathName, Map<Instant, Map<Instant, Integer>>> plannedBacklogs,
        Map<Instant, Map<ProcessName, Integer>> throughput
    );
  }

}
