package com.mercadolibre.flow.control.tool.feature.backlog.monitor;

import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.BacklogMonitor;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.ProcessPathMonitor;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.ProcessesMonitor;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.SlasMonitor;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessPath;
import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class BacklogProjectionUtil {

  private BacklogProjectionUtil() {
  }

  public static void order(List<BacklogMonitor> backlogMonitors) {
    backlogMonitors.sort(Comparator.comparing(BacklogMonitor::date));

    backlogMonitors.stream()
        .map(backlogMonitor -> orderProcess(backlogMonitor.processes()))
        .map(backlogMonitor -> backlogMonitor.stream()
            .map(processesMonitor -> orderSla(processesMonitor.slas())))
        .forEach(backlogMonitor -> backlogMonitor
            .forEach(processesMonitor -> processesMonitor
                .forEach(slaMonitor -> orderProcessPath(slaMonitor.processPaths()))));
  }

  private static List<ProcessesMonitor> orderProcess(List<ProcessesMonitor> processesMonitors) {
    processesMonitors.sort(Comparator.comparing(ProcessesMonitor::name));
    return processesMonitors;
  }

  private static List<SlasMonitor> orderSla(List<SlasMonitor> slasMonitors) {
    slasMonitors.sort(Comparator.comparing(SlasMonitor::date));
    return slasMonitors;
  }

  private static void orderProcessPath(List<ProcessPathMonitor> processPaths) {
    processPaths.sort(Comparator.comparing(ProcessPathMonitor::name));
  }

  public static List<BacklogMonitor> sumBacklogProjection(
      final Map<Instant, Map<ProcessName, Map<Instant, Map<ProcessPath, Integer>>>> backlogProjection) {
    return backlogProjection.entrySet().stream().map(
            backlogHistoricalEntry ->
                new BacklogMonitor(
                    backlogHistoricalEntry.getKey(),
                    mapToProcessesMonitorList(backlogHistoricalEntry.getValue())
                )
        ).sorted(Comparator.comparing(BacklogMonitor::date))
        .collect(Collectors.toList());
  }

  private static List<ProcessesMonitor> mapToProcessesMonitorList(
      final Map<ProcessName, Map<Instant, Map<ProcessPath, Integer>>> backlogByProcess) {
    return backlogByProcess.entrySet().stream()
        .map(backlogByProcessEntry -> new ProcessesMonitor(
            backlogByProcessEntry.getKey(),
            sumProcessMonitorQuantity(backlogByProcessEntry.getValue()),
            mapToSlasMonitorList(backlogByProcessEntry.getValue())
        ))
        .collect(Collectors.toList());
  }

  private static Integer sumProcessMonitorQuantity(final Map<Instant, Map<ProcessPath, Integer>> backlogBySla) {
    return backlogBySla.values().stream()
        .map(Map::values)
        .flatMap(Collection::stream)
        .mapToInt(Integer::intValue)
        .sum();
  }

  private static List<SlasMonitor> mapToSlasMonitorList(final Map<Instant, Map<ProcessPath, Integer>> backlogBySla) {
    return backlogBySla.entrySet().stream()
        .map(backlogBySlaEntry -> new SlasMonitor(
                backlogBySlaEntry.getKey(),
                sumProcessPathQuantity(backlogBySlaEntry.getValue()),
                mapToProcessPathMonitorList(backlogBySlaEntry.getValue())
            )
        ).collect(Collectors.toList());
  }

  private static Integer sumProcessPathQuantity(final Map<ProcessPath, Integer> backlogByProcessPath) {
    return backlogByProcessPath.values().stream()
        .mapToInt(Integer::intValue)
        .sum();
  }

  private static List<ProcessPathMonitor> mapToProcessPathMonitorList(final Map<ProcessPath, Integer> backlogByProcessPath) {
    return backlogByProcessPath.entrySet().stream()
        .map(backlogByProcessPathEntry -> new ProcessPathMonitor(
            backlogByProcessPathEntry.getKey(),
            backlogByProcessPathEntry.getValue())
        ).collect(Collectors.toList());
  }

}
