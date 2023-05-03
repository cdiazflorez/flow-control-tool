package com.mercadolibre.flow.control.tool.feature.backlog.monitor;

import com.mercadolibre.flow.control.tool.feature.backlog.genericgateway.BacklogGateway;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.BacklogMonitor;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessPath;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class BacklogProjectedUseCase {

  private final BacklogGateway backlogApiGateway;

  private final PlanningEntitiesGateway planningApiGateway;

  private final BacklogProjectionGateway backlogProjectionGateway;

  public List<BacklogMonitor> getBacklogProjected(
      final Instant dateFrom,
      final Instant dateTo,
      final String logisticCenterId,
      final Workflow workflow,
      final Set<ProcessName> processes) {

    final var currentBacklog = backlogApiGateway.getBacklogTotalsByProcess(logisticCenterId, workflow, processes, dateFrom);

    final var tph = planningApiGateway.getThroughput(workflow, logisticCenterId, dateFrom, dateTo, processes);

    final var plannedBacklog = planningApiGateway.getPlannedBacklog(workflow, logisticCenterId, dateFrom, dateTo);

    final var backlogProjection =
        backlogProjectionGateway.executeBacklogProjection(dateFrom, dateTo, processes, currentBacklog, tph, plannedBacklog);

    List<BacklogMonitor> backlogMonitors = BacklogProjectionUtil.sumBacklogProjection(backlogProjection);

    BacklogProjectionUtil.order(backlogMonitors);

    return backlogMonitors;
  }

  /**
   * Gateway planning api to obtain tph, plannedBacklog.
   */
  public interface PlanningEntitiesGateway {
    List<Throughput> getThroughput(Workflow workflow, String logisticCenterId, Instant dateFrom, Instant dateTo, Set<ProcessName> process);

    List<PlannedBacklog> getPlannedBacklog(Workflow workflow, String logisticCenterId, Instant dateFrom, Instant dateTo);
  }

  /**
   * Gateway planning api to execute backlog projection.
   */
  public interface BacklogProjectionGateway {
    Map<Instant, Map<ProcessName, Map<Instant, Map<ProcessPath, Integer>>>> executeBacklogProjection(
        Instant dateFrom,
        Instant dateTo,
        Set<ProcessName> process,
        Map<ProcessName, Integer> currentBacklogs,
        List<Throughput> throughput,
        List<PlannedBacklog> plannedBacklogs);
  }

  public record Throughput(
      Instant date,
      ProcessPath processPath,
      ProcessName processName,
      Integer quantity
  ) {
  }

  public record PlannedBacklog(
      Instant dateIn,
      Instant dateOut,
      Integer quantity
  ) {
  }
}
