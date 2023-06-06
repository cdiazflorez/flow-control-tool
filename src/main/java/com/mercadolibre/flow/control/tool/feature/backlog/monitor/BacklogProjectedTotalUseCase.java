package com.mercadolibre.flow.control.tool.feature.backlog.monitor;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.flatMapping;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingInt;
import static java.util.stream.Collectors.toMap;

import com.mercadolibre.flow.control.tool.feature.backlog.genericgateway.UnitsPerOrderRatioGateway;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.domain.ProjectionTotal;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.domain.SlaQuantity;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.ProcessPathMonitor;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.SlasMonitor;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.TotalBacklogMonitor;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName;
import com.mercadolibre.flow.control.tool.feature.entity.ValueType;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BacklogProjectedTotalUseCase {

  private final BacklogProjectedGateway backlogProjectedGateway;

  private final PlannedEntitiesGateway plannedEntitiesGateway;

  private final TotalBacklogProjectionGateway totalBacklogProjectionGateway;

  private final UnitsPerOrderRatioGateway unitsPerOrderRatioGateway;

  public List<TotalBacklogMonitor> getTotalProjection(final String logisticCenterId,
                                                      final Workflow workflow,
                                                      final Set<ProcessName> backlogProcesses,
                                                      final Set<ProcessName> throughputProcesses,
                                                      final ValueType valueType,
                                                      final Instant dateFrom,
                                                      final Instant dateTo,
                                                      final Instant viewDate) {

    final Map<ProcessPathName, List<SlaQuantity>> backlogSlaQuantityByProcessPath = getBacklogSlaQuantityByProcessPath(logisticCenterId,
        workflow,
        backlogProcesses,
        dateFrom);

    final Map<ProcessPathName, List<SlaQuantity>> plannedSlaQuantityByProcessPath =
        getBacklogPlannedSlaQuantityByProcessPath(workflow,
            logisticCenterId,
            dateFrom,
            dateTo);

    final double unitsPerOrderRatio = unitsPerOrderRatioGateway.getUnitsPerOrderRatio(workflow, logisticCenterId, viewDate).orElseThrow();

    final Map<Instant, Integer> throughputByDate = plannedEntitiesGateway.getThroughputByDateAndProcess(workflow,
            logisticCenterId,
            dateFrom,
            dateTo,
            throughputProcesses).entrySet()
        .stream()
        .collect(
            toMap(
                Map.Entry::getKey,
                entry -> {
                  final var quantity = entry.getValue().values().stream().mapToInt(Integer::intValue).sum();
                  return valueType == ValueType.UNITS
                      ? quantity : Math.toIntExact(Math.round(quantity * unitsPerOrderRatio));
                }
            )
        );

    final List<ProjectionTotal> projection = totalBacklogProjectionGateway.getTotalProjection(logisticCenterId,
        dateFrom,
        dateTo,
        backlogSlaQuantityByProcessPath,
        plannedSlaQuantityByProcessPath,
        throughputByDate);

    final List<TotalBacklogMonitor> totalBacklogMonitors = projection.stream()
        .map(
            projectionTotal -> new TotalBacklogMonitor(
                projectionTotal.dateOperation(),
                getProjectionTotalQuantity(
                    projectionTotal,
                    unitsPerOrderRatio,
                    valueType),
                getSlaMonitor(projectionTotal.slas(),
                    unitsPerOrderRatio,
                    valueType)
            ))
        .toList();

    final Stream<TotalBacklogMonitor> responseTotalBacklogMonitor = completeTotalBacklogMonitors(totalBacklogMonitors,
        dateFrom.truncatedTo(ChronoUnit.HOURS).plus(Duration.ofHours(1)),
        dateTo.truncatedTo(ChronoUnit.HOURS));

    return responseTotalBacklogMonitor
        .sorted(Comparator.comparing(TotalBacklogMonitor::date, Comparator.nullsLast(Comparator.naturalOrder())))
        .toList();
  }

  private Map<ProcessPathName, List<SlaQuantity>> getBacklogSlaQuantityByProcessPath(final String logisticCenterId,
                                                                                     final Workflow workflow,
                                                                                     final Set<ProcessName> backlogProcesses,
                                                                                     final Instant date) {

    final Map<ProcessName, Map<ProcessPathName, Map<Instant, Integer>>> backlogTotalByProcessAndProcessPathAndSla =
        backlogProjectedGateway.getBacklogTotalsByProcessAndPPandSla(logisticCenterId, workflow, backlogProcesses, date);

    return backlogTotalByProcessAndProcessPathAndSla.values()
        .stream()
        .flatMap(quantityByDateAndProcessPath -> quantityByDateAndProcessPath.entrySet().stream())
        .collect(
            groupingBy(
                Map.Entry::getKey,
                collectingAndThen(
                    flatMapping(
                        quantityByDateAndProcessPathEntry -> quantityByDateAndProcessPathEntry.getValue().entrySet().stream(),
                        groupingBy(
                            Map.Entry::getKey,
                            summingInt(Map.Entry::getValue)
                        )
                    ),
                    quantityByDate -> quantityByDate.entrySet().stream()
                        .map(quantityByDateEntry -> SlaQuantity.builder()
                            .dateOut(quantityByDateEntry.getKey())
                            .quantity(quantityByDateEntry.getValue())
                            .build())
                        .toList()
                )
            )
        );
  }

  private Map<ProcessPathName, List<SlaQuantity>> getBacklogPlannedSlaQuantityByProcessPath(final Workflow workflow,
                                                                                            final String logisticCenterId,
                                                                                            final Instant dateFrom,
                                                                                            final Instant dateTo) {

    final Map<ProcessPathName, Map<Instant, Map<Instant, Integer>>> plannedUnitByPPDateInAndDateOut = plannedEntitiesGateway
        .getPlannedUnitByPPDateInAndDateOut(workflow,
            logisticCenterId,
            dateFrom,
            dateTo);

    return plannedUnitByPPDateInAndDateOut.entrySet()
        .stream()
        .collect(
            toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().entrySet().stream()
                    .flatMap(
                        dateInAndDateOutEntry -> dateInAndDateOutEntry.getValue().entrySet().stream()
                            .map(
                                dateOutAndQuantityEntry -> SlaQuantity.builder()
                                    .dateIn(dateInAndDateOutEntry.getKey())
                                    .dateOut(dateOutAndQuantityEntry.getKey())
                                    .quantity(dateOutAndQuantityEntry.getValue())
                                    .build()
                            ))
                    .toList()
            ));

  }

  private Integer getProjectionTotalQuantity(final ProjectionTotal projectionTotals,
                                             final Double unitsPerOrderRatio,
                                             final ValueType valueType) {
    final var quantity = projectionTotals.slas().stream()
        .mapToInt(ProjectionTotal.SlaProjected::quantity)
        .sum();
    return convertUnitsToOrders(quantity, unitsPerOrderRatio, valueType);
  }

  private List<SlasMonitor> getSlaMonitor(final List<ProjectionTotal.SlaProjected> slaProjected,
                                          final Double unitsPerOrderRatio,
                                          final ValueType valueType) {
    return slaProjected.stream()
        .map(sla -> new SlasMonitor(sla.date(),
            convertUnitsToOrders(sla.quantity(), unitsPerOrderRatio, valueType),
            sla.processPaths().stream()
                .map(path -> new ProcessPathMonitor(path.name(),
                    convertUnitsToOrders(path.quantity(), unitsPerOrderRatio, valueType)))
                .toList()))
        .toList();
  }

  private int convertUnitsToOrders(final int units, final double ratio, final ValueType valueType) {
    return valueType == ValueType.UNITS ? units : Math.toIntExact(Math.round(units / ratio));
  }

  public Stream<TotalBacklogMonitor> completeTotalBacklogMonitors(
      final List<TotalBacklogMonitor> totalBacklogMonitors,
      final Instant dateFrom,
      final Instant dateTo
  ) {

    return Stream.iterate(dateFrom, instant -> instant.plus(Duration.ofHours(1)))
        .limit(Duration.between(dateFrom, dateTo).toHours() + 1)
        .map(hour -> fillMissingTotalDateMonitors(totalBacklogMonitors, hour));
  }

  private TotalBacklogMonitor fillMissingTotalDateMonitors(final List<TotalBacklogMonitor> totalBacklogMonitors,
                                                           final Instant hour) {
    return totalBacklogMonitors.stream()
        .filter(monitor -> monitor.date().equals(hour))
        .findAny()
        .orElseGet(() -> new TotalBacklogMonitor(hour, 0, emptyList()));
  }

  /**
   * Gateway to get backlog total projected.
   */
  public interface TotalBacklogProjectionGateway {

    /**
     * Get backlog total projected.
     *
     * @param logisticCenterId The logistic center id.
     * @param dateFrom         The date from.
     * @param dateTo           The date to.
     * @param backlog          The backlog.
     * @param plannedUnits     The planned units.
     * @param throughput       The throughput.
     * @return The backlog total projected.
     */
    List<ProjectionTotal> getTotalProjection(String logisticCenterId,
                                             Instant dateFrom,
                                             Instant dateTo,
                                             Map<ProcessPathName, List<SlaQuantity>> backlog,
                                             Map<ProcessPathName, List<SlaQuantity>> plannedUnits,
                                             Map<Instant, Integer> throughput);

  }

}
