package com.mercadolibre.flow.control.tool.feature.backlog.monitor;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.flatMapping;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingInt;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import com.mercadolibre.flow.control.tool.feature.backlog.monitor.domain.ProjectionTotal;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.domain.SlaQuantity;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.ProcessPathMonitor;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.SlasMonitor;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.TotalBacklogMonitor;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName;
import com.mercadolibre.flow.control.tool.feature.entity.ValueType;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BacklogProjectedTotalUseCase {

  private final BacklogProjectedGateway backlogProjectedGateway;

  private final PlannedEntitiesGateway plannedEntitiesGateway;

  private final TotalBacklogProjectionGateway totalBacklogProjectionGateway;

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

    final Map<Instant, Integer> throughputByDate = plannedEntitiesGateway.getThroughputByDateAndProcess(workflow,
                                                                                                        logisticCenterId,
                                                                                                        dateFrom,
                                                                                                        dateTo,
                                                                                                        throughputProcesses).entrySet()
        .stream()
        .collect(
            toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().values().stream().mapToInt(Integer::intValue).sum()
            )
        );

    final List<ProjectionTotal> projection = totalBacklogProjectionGateway.getTotalProjection(logisticCenterId,
                                                                                              dateFrom,
                                                                                              dateTo,
                                                                                              backlogSlaQuantityByProcessPath,
                                                                                              plannedSlaQuantityByProcessPath,
                                                                                              throughputByDate);

    return projection.stream()
        .collect(
            collectingAndThen(
                groupingBy(
                    ProjectionTotal::dateOperation,
                    toList()
                ),
                projectionTotalByDateOperation -> projectionTotalByDateOperation.entrySet().stream()
                    .flatMap(
                        projectionTotalByDateOperationEntry -> projectionTotalByDateOperationEntry.getValue()
                            .stream()
                            .map(
                                projectionTotal -> new TotalBacklogMonitor(
                                    projectionTotalByDateOperationEntry.getKey(),
                                    getProjectionTotalQuantity(projectionTotalByDateOperationEntry.getValue()),
                                    getSlaMonitor(projectionTotal.slas())
                                )))
                    .toList()
            )
        );

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

  private Integer getProjectionTotalQuantity(final List<ProjectionTotal> projectionTotals) {
    return projectionTotals.stream()
        .mapToInt(projectionTotal -> projectionTotal.slas().stream()
            .mapToInt(ProjectionTotal.SlaProjected::quantity)
            .sum())
        .sum();
  }

  private List<SlasMonitor> getSlaMonitor(final List<ProjectionTotal.SlaProjected> slaProjected) {
    return slaProjected.stream()
        .map(sla -> new SlasMonitor(sla.date(),
                                    sla.quantity(),
                                    sla.processPaths().stream()
                                        .map(path -> new ProcessPathMonitor(path.name(),
                                                                            path.quantity()))
                                        .toList()))
        .toList();
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
