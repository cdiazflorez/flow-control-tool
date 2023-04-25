package com.mercadolibre.flow.control.tool.feature.backlog.monitor;

import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.BacklogMonitor;
import com.mercadolibre.flow.control.tool.feature.entity.Grouper;
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
      final Set<ProcessName> process) {

    final var currentBacklog = backlogApiGateway.getCurrentBacklog(workflow, logisticCenterId, dateFrom, Grouper.PROCESS_NAME);

    final var tph = planningApiGateway.getThroughput(workflow, logisticCenterId, dateFrom, dateTo, process);

    final var plannedBacklog = planningApiGateway.getPlannedBacklog(workflow, logisticCenterId, dateFrom, dateTo);

    final var backlogProjection =
        backlogProjectionGateway.executeBacklogProjection(dateFrom, dateTo, process, currentBacklog, tph, plannedBacklog);

    List<BacklogMonitor> backlogMonitors = BacklogProjectionUtil.sumBacklogProjection(backlogProjection);

    BacklogProjectionUtil.order(backlogMonitors);

    return backlogMonitors;
  }

  /**
   * Gateway backlog api to obtain current backlog.
   */
  public interface BacklogGateway {
    List<CurrentBacklog> getCurrentBacklog(Workflow workflow, String logisticCenterId, Instant viewDate, Grouper grouper);
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
        List<CurrentBacklog> currentBacklogs,
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

  public record CurrentBacklog(
      ProcessName processName,
      Integer quantity
  ) {
  }
}
