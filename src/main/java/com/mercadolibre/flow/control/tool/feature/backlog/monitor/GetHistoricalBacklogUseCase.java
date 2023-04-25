package com.mercadolibre.flow.control.tool.feature.backlog.monitor;

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
public class GetHistoricalBacklogUseCase {

  private final BacklogGateway backlogGateway;

  public List<BacklogMonitor> backlogHistoricalMonitor(
      final Workflow workflow,
      final String logisticCenterId,
      final Set<ProcessName> processes,
      final Instant dateFrom,
      final Instant dateTo) {
    final var backlogHistorical = backlogGateway.getBacklogByDateProcessAndPP(
        workflow, logisticCenterId, processes, dateFrom, dateTo);

    List<BacklogMonitor> response = BacklogProjectionUtil.sumBacklogProjection(backlogHistorical);

    BacklogProjectionUtil.order(response);

    return response;
  }

  /**
   * Get map backlog by date, process, sla and process path.
   */
  public interface BacklogGateway {
    Map<Instant, Map<ProcessName, Map<Instant, Map<ProcessPath, Integer>>>> getBacklogByDateProcessAndPP(
        Workflow workflow, String logisticCenter, Set<ProcessName> processes, Instant dateFrom, Instant dateTo);
  }
}
