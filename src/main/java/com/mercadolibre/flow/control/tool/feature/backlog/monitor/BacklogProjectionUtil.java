package com.mercadolibre.flow.control.tool.feature.backlog.monitor;

import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.getShippingProcess;
import static java.util.Collections.emptyList;

import com.mercadolibre.flow.control.tool.exception.NoUnitsPerOrderRatioFound;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.BacklogMonitor;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.ProcessPathMonitor;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.ProcessesMonitor;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.SlasMonitor;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class BacklogProjectionUtil {

  private static final double MIN_VALUE_FOR_UNIT_PER_ORDER_RATIO = 1;

  private BacklogProjectionUtil() {
  }

  /**
   * Completes the list of BacklogMonitors for each hour within the specified date range.
   * Filling missing BacklogMonitor for a specific hour and also missing process with quantity of 0
   *
   * @param backlogMonitors The list of BacklogMonitors.
   * @param dateFrom        The starting date.
   * @param dateTo          The ending date.
   * @return The list of completed BacklogMonitors for each hour within the date range.
   */
  public static List<BacklogMonitor> fillBacklogMonitorsMissingDatesAndProcesses(
      final List<BacklogMonitor> backlogMonitors,
      final Instant dateFrom,
      final Instant dateTo
  ) {

    return Stream.iterate(dateFrom, instant -> instant.plus(Duration.ofHours(1)))
        .limit(Duration.between(dateFrom, dateTo).toHours() + 1)
        .map(hour -> fillMissingDateMonitors(backlogMonitors, hour)
        ).collect(Collectors.toList());

  }

  /**
   * Fills in the missing BacklogMonitor with empty ProcessesMonitor list for a specific hour.
   * if it exists, by completing the list of ProcessesMonitors with missing processes.
   *
   * @param backlogMonitors The list of BacklogMonitors.
   * @param hour            The hour for which to fill the missing BacklogMonitor.
   * @return The filled BacklogMonitor for the specified hour.
   */
  private static BacklogMonitor fillMissingDateMonitors(final List<BacklogMonitor> backlogMonitors, final Instant hour) {
    return backlogMonitors.stream()
        .filter(monitor -> monitor.date().equals(hour))
        .findFirst()
        .map(backlogMonitor -> new BacklogMonitor(
            backlogMonitor.date(),
            completeMissingProcesses(backlogMonitor.processes())
        )).orElseGet(() -> new BacklogMonitor(hour, createEmptyProcessesList()));
  }

  /**
   * Completes the list of ProcessesMonitors by adding missing processes with a quantity of 0.
   *
   * @param processes The list of ProcessesMonitors.
   * @return The completed list of ProcessesMonitors.
   */
  private static List<ProcessesMonitor> completeMissingProcesses(final List<ProcessesMonitor> processes) {

    return Stream.concat(
        processes.stream(),
        Arrays.stream(ProcessName.values())
            .filter(processName -> processes.stream().noneMatch(processesMonitor -> processesMonitor.name().equals(processName)))
            .map(processName -> new ProcessesMonitor(processName, 0, emptyList()))
    ).collect(Collectors.toList());
  }

  /**
   * Creates an empty list of ProcessesMonitors with a quantity of 0 for each process name.
   *
   * @return The empty list of ProcessesMonitors.
   */
  private static List<ProcessesMonitor> createEmptyProcessesList() {
    return Arrays.stream(ProcessName.values())
        .map(processName -> new ProcessesMonitor(processName, 0, emptyList()))
        .collect(Collectors.toList());
  }

  /**
   * Orders the list of BacklogMonitors by date and returns a new sorted list.
   *
   * @param backlogMonitors The list of BacklogMonitors to be ordered.
   * @return The sorted list of BacklogMonitors.
   */
  public static List<BacklogMonitor> orderMonitorsByDate(List<BacklogMonitor> backlogMonitors) {
    return backlogMonitors.stream()
        .sorted(Comparator.comparing(BacklogMonitor::date))
        .map(BacklogProjectionUtil::orderProcesses)
        .collect(Collectors.toList());
  }

  /**
   * Orders the list of ProcessesMonitors within a BacklogMonitor by name and returns a new BacklogMonitor with the ordered processes.
   *
   * @param backlogMonitor The BacklogMonitor containing the processes to be ordered.
   * @return The new BacklogMonitor with ordered processes.
   */
  private static BacklogMonitor orderProcesses(BacklogMonitor backlogMonitor) {
    List<ProcessesMonitor> orderedProcesses = backlogMonitor.processes().stream()
        .map(BacklogProjectionUtil::orderSlas)
        .sorted(Comparator.comparing(ProcessesMonitor::name))
        .collect(Collectors.toList());
    return new BacklogMonitor(backlogMonitor.date(), orderedProcesses);
  }

  /**
   * Orders the list of SlasMonitors within a ProcessesMonitor by date and returns a new ProcessesMonitor with the ordered slas.
   *
   * @param processesMonitor The ProcessesMonitor containing the slas to be ordered.
   * @return The new ProcessesMonitor with ordered slas.
   */
  private static ProcessesMonitor orderSlas(ProcessesMonitor processesMonitor) {
    List<SlasMonitor> orderedSlas = processesMonitor.slas().stream()
        .sorted(Comparator.comparing(SlasMonitor::date))
        .collect(Collectors.toList());
    return new ProcessesMonitor(processesMonitor.name(), processesMonitor.quantity(), orderedSlas);
  }

  /**
   * Computes the sum of backlog projections with empty process paths and returns a list of BacklogMonitors.
   *
   * @param backlogProjection  The backlog projection map.
   * @param unitsPerOrderRatio The units per order ratio.
   * @return The list of BacklogMonitors representing the summed backlog projection.
   */
  public static List<BacklogMonitor> sumBacklogProjectionEmptyPP(
      final Map<Instant, Map<ProcessName, Map<Instant, Integer>>> backlogProjection,
      final Double unitsPerOrderRatio
  ) {
    return backlogProjection.entrySet().stream().map(
            backlogHistoricalEntry ->
                new BacklogMonitor(
                    backlogHistoricalEntry.getKey(),
                    mapToProcessesMonitorListEmptyPP(backlogHistoricalEntry.getValue(), unitsPerOrderRatio)
                )
        ).sorted(Comparator.comparing(BacklogMonitor::date))
        .collect(Collectors.toList());
  }

  /**
   * Maps the backlog by process map to a list of ProcessesMonitors with empty process paths.
   *
   * @param backlogByProcess   The backlog by process map.
   * @param unitsPerOrderRatio The units per order ratio.
   * @return The list of ProcessesMonitors with empty process paths.
   */
  private static List<ProcessesMonitor> mapToProcessesMonitorListEmptyPP(
      final Map<ProcessName, Map<Instant, Integer>> backlogByProcess,
      final Double unitsPerOrderRatio
  ) {
    return backlogByProcess.entrySet().stream()
        .map(backlogByProcessEntry -> resultProcessesMonitorEmptyPP(backlogByProcessEntry, unitsPerOrderRatio))
        .collect(Collectors.toList());
  }

  /**
   * Computes the ProcessesMonitor with empty process paths based on the backlog by process map and units per order ratio.
   *
   * @param backlogByProcess The backlog by process map.
   * @param unitsPerOrderRatio The units per order ratio.
   * @return The ProcessesMonitor with null process paths.
   */
  private static ProcessesMonitor resultProcessesMonitorEmptyPP(
      final Map.Entry<ProcessName, Map<Instant, Integer>> backlogByProcess,
      final Double unitsPerOrderRatio
  ) {
    final var process = backlogByProcess.getKey();
    final var quantities = sumProcessMonitorQuantityEmptyPP(backlogByProcess.getValue());

    return new ProcessesMonitor(
        process,
        getShippingProcess().contains(process)
            ? (int) (quantities / unitsPerOrderRatio)
            : quantities,
        mapToSlasMonitorEmptyPPList(backlogByProcess.getValue())
    );
  }

  /**
   * Sums the quantities in the backlog by SLA map and returns the total quantity.
   *
   * @param backlogBySla The backlog by SLA map.
   * @return The sum of the quantities in the backlog by SLA.
   */
  private static Integer sumProcessMonitorQuantityEmptyPP(final Map<Instant, Integer> backlogBySla) {
    return backlogBySla.values().stream()
        .mapToInt(Integer::intValue)
        .sum();
  }

  /**
   * Maps the backlog by SLA map to a list of SlasMonitors with empty process paths.
   *
   * @param backlogBySla The backlog by SLA map.
   * @return The list of SlasMonitors with empty process paths.
   */
  private static List<SlasMonitor> mapToSlasMonitorEmptyPPList(final Map<Instant, Integer> backlogBySla) {
    return backlogBySla.entrySet().stream()
        .map(backlogBySlaEntry -> new SlasMonitor(
                backlogBySlaEntry.getKey(),
                backlogBySlaEntry.getValue(),
                emptyList()
            )
        ).collect(Collectors.toList());
  }

  public static Double validateUnitsPerOrderRatio(final Optional<Double> unitsPerOrderRatio, final String logisticCenterId) {
    return unitsPerOrderRatio.filter(ratio -> ratio >= MIN_VALUE_FOR_UNIT_PER_ORDER_RATIO)
        .orElseThrow(
            () -> new NoUnitsPerOrderRatioFound(logisticCenterId)
        );
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
