package com.mercadolibre.flow.control.tool.feature.backlog.monitor;

import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.BacklogMonitor;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.ProcessPathMonitor;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.ProcessesMonitor;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.SlasMonitor;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessPath;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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

    return backlogHistorical.entrySet().stream().map(
        backlogHistoricalEntry ->
            new BacklogMonitor(
                backlogHistoricalEntry.getKey(),
                mapToProcessesMonitorList(backlogHistoricalEntry.getValue())
            )
    ).collect(Collectors.toList());
  }

  private List<ProcessesMonitor> mapToProcessesMonitorList(
      final Map<ProcessName, Map<Instant, Map<ProcessPath, Integer>>> backlogByProcess) {
    return backlogByProcess.entrySet().stream()
        .map(backlogByProcessEntry -> new ProcessesMonitor(
            backlogByProcessEntry.getKey(),
            sumProcessMonitorQuantity(backlogByProcessEntry.getValue()),
            mapToSlasMonitorList(backlogByProcessEntry.getValue())
        ))
        .collect(Collectors.toList());
  }

  private Integer sumProcessMonitorQuantity(final Map<Instant, Map<ProcessPath, Integer>> backlogBySla) {
    return backlogBySla.values().stream()
        .map(Map::values)
        .flatMap(Collection::stream)
        .mapToInt(Integer::intValue)
        .sum();
  }

  private List<SlasMonitor> mapToSlasMonitorList(final Map<Instant, Map<ProcessPath, Integer>> backlogBySla) {
    return backlogBySla.entrySet().stream()
        .map(backlogBySlaEntry -> new SlasMonitor(
                backlogBySlaEntry.getKey(),
                sumProcessPathQuantity(backlogBySlaEntry.getValue()),
                mapToProcessPathMonitorList(backlogBySlaEntry.getValue())
            )
        ).collect(Collectors.toList());
  }

  private Integer sumProcessPathQuantity(final Map<ProcessPath, Integer> backlogByProcessPath) {
    return backlogByProcessPath.values().stream()
        .mapToInt(Integer::intValue)
        .sum();
  }

  private List<ProcessPathMonitor> mapToProcessPathMonitorList(final Map<ProcessPath, Integer> backlogByProcessPath) {
    return backlogByProcessPath.entrySet().stream()
        .map(backlogByProcessPathEntry -> new ProcessPathMonitor(
            backlogByProcessPathEntry.getKey(),
            backlogByProcessPathEntry.getValue())
        ).collect(Collectors.toList());
  }

  /**
   * Get map backlog by date, process, sla and process path.
   */
  public interface BacklogGateway {
    Map<Instant, Map<ProcessName, Map<Instant, Map<ProcessPath, Integer>>>> getBacklogByDateProcessAndPP(
        Workflow workflow, String logisticCenter, Set<ProcessName> processes, Instant dateFrom, Instant dateTo);
  }
}
