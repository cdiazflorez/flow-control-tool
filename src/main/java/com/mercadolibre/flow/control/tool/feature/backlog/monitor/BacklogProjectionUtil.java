package com.mercadolibre.flow.control.tool.feature.backlog.monitor;

import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.getShippingProcess;

import com.mercadolibre.flow.control.tool.exception.NoUnitsPerOrderRatioFound;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.BacklogMonitor;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.ProcessPathMonitor;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.ProcessesMonitor;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.SlasMonitor;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class BacklogProjectionUtil {

  private static final double MIN_VALUE_FOR_UNIT_PER_ORDER_RATIO = 1;

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
                .forEach(slaMonitor -> {
                  final List<ProcessPathMonitor> processPaths = Optional.ofNullable(slaMonitor.processPaths())
                      .orElse(Collections.emptyList());
                  if (!processPaths.isEmpty()) {
                    orderProcessPath(processPaths);
                  }
                })
            )
        );
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


  public static List<BacklogMonitor> sumBacklogProjectionNullPP(
      final Map<Instant, Map<ProcessName, Map<Instant, Integer>>> backlogProjection,
      final Double unitsPerOrderRatio
  ) {
    return backlogProjection.entrySet().stream().map(
            backlogHistoricalEntry ->
                new BacklogMonitor(
                    backlogHistoricalEntry.getKey(),
                    mapToProcessesMonitorListNullPP(backlogHistoricalEntry.getValue(), unitsPerOrderRatio)
                )
        ).sorted(Comparator.comparing(BacklogMonitor::date))
        .collect(Collectors.toList());
  }

  public static Double validateUnitsPerOrderRatio(final Optional<Double> unitsPerOrderRatio, final String logisticCenterId) {
    return unitsPerOrderRatio.filter(ratio -> ratio >= MIN_VALUE_FOR_UNIT_PER_ORDER_RATIO)
        .orElseThrow(
            () -> new NoUnitsPerOrderRatioFound(logisticCenterId)
        );
  }

  private static List<ProcessesMonitor> mapToProcessesMonitorListNullPP(
      final Map<ProcessName, Map<Instant, Integer>> backlogByProcess,
      final Double unitsPerOrderRatio
  ) {
    return backlogByProcess.entrySet().stream()
        .map(backlogByProcessEntry -> resultProcessesMonitorNullPP(backlogByProcessEntry, unitsPerOrderRatio))
        .collect(Collectors.toList());
  }

  private static ProcessesMonitor resultProcessesMonitorNullPP(
      final Map.Entry<ProcessName, Map<Instant, Integer>> backlogByProcess,
      final Double unitsPerOrderRatio
  ) {
    final var process = backlogByProcess.getKey();
    final var quantities = sumProcessMonitorQuantityNullPP(backlogByProcess.getValue());

    return new ProcessesMonitor(
        process,
        getShippingProcess().contains(process)
            ? (int) (quantities / unitsPerOrderRatio)
            : quantities,
        mapToSlasMonitorNullPPList(backlogByProcess.getValue())
    );
  }

  private static Integer sumProcessMonitorQuantityNullPP(final Map<Instant, Integer> backlogBySla) {
    return backlogBySla.values().stream()
        .mapToInt(Integer::intValue)
        .sum();
  }

  private static List<SlasMonitor> mapToSlasMonitorNullPPList(final Map<Instant, Integer> backlogBySla) {
    return backlogBySla.entrySet().stream()
        .map(backlogBySlaEntry -> new SlasMonitor(
                backlogBySlaEntry.getKey(),
                backlogBySlaEntry.getValue(),
                Collections.emptyList()
            )
        ).collect(Collectors.toList());
  }

  // TODO: These methods are defined to handle when the historic/projection is opened by the process path.
  public static List<BacklogMonitor> sumBacklogProjection(
      final Map<Instant, Map<ProcessName, Map<Instant, Map<ProcessPathName, Integer>>>> backlogProjection,
      final Double unitsPerOrderRatio
  ) {
    return backlogProjection.entrySet().stream().map(
            backlogHistoricalEntry ->
                new BacklogMonitor(
                    backlogHistoricalEntry.getKey(),
                    mapToProcessesMonitorList(backlogHistoricalEntry.getValue(), unitsPerOrderRatio)
                )
        ).sorted(Comparator.comparing(BacklogMonitor::date))
        .collect(Collectors.toList());
  }

  private static List<ProcessesMonitor> mapToProcessesMonitorList(
      final Map<ProcessName, Map<Instant, Map<ProcessPathName, Integer>>> backlogByProcess,
      final Double unitsPerOrderRatio
  ) {
    return backlogByProcess.entrySet().stream()
        .map(backlogByProcessEntry -> resultProcessesMonitor(backlogByProcessEntry, unitsPerOrderRatio))
        .collect(Collectors.toList());
  }

  private static ProcessesMonitor resultProcessesMonitor(
      final Map.Entry<ProcessName, Map<Instant, Map<ProcessPathName, Integer>>> backlogByProcess,
      final Double unitsPerOrderRatio
  ) {
    final var quantities = sumProcessMonitorQuantity(backlogByProcess.getValue());

    return new ProcessesMonitor(
        backlogByProcess.getKey(),
        getShippingProcess().contains(backlogByProcess.getKey())
            ? (int) (quantities / unitsPerOrderRatio)
            : quantities,
        mapToSlasMonitorList(backlogByProcess.getValue())
    );
  }

  private static Integer sumProcessMonitorQuantity(final Map<Instant, Map<ProcessPathName, Integer>> backlogBySla) {
    return backlogBySla.values().stream()
        .map(Map::values)
        .flatMap(Collection::stream)
        .mapToInt(Integer::intValue)
        .sum();
  }

  private static List<SlasMonitor> mapToSlasMonitorList(final Map<Instant, Map<ProcessPathName, Integer>> backlogBySla) {
    return backlogBySla.entrySet().stream()
        .map(backlogBySlaEntry -> new SlasMonitor(
                backlogBySlaEntry.getKey(),
                sumProcessPathQuantity(backlogBySlaEntry.getValue()),
                mapToProcessPathMonitorList(backlogBySlaEntry.getValue())
            )
        ).collect(Collectors.toList());
  }

  private static Integer sumProcessPathQuantity(final Map<ProcessPathName, Integer> backlogByProcessPath) {
    return backlogByProcessPath.values().stream()
        .mapToInt(Integer::intValue)
        .sum();
  }

  private static List<ProcessPathMonitor> mapToProcessPathMonitorList(final Map<ProcessPathName, Integer> backlogByProcessPath) {
    return backlogByProcessPath.entrySet().stream()
        .map(backlogByProcessPathEntry -> new ProcessPathMonitor(
            backlogByProcessPathEntry.getKey(),
            backlogByProcessPathEntry.getValue())
        ).collect(Collectors.toList());
  }
}
