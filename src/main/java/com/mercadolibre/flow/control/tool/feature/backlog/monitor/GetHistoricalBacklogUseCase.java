package com.mercadolibre.flow.control.tool.feature.backlog.monitor;

import com.mercadolibre.flow.control.tool.feature.backlog.genericgateway.UnitsPerOrderRatioGateway;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.BacklogMonitor;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class GetHistoricalBacklogUseCase {

  private static final double MIN_VALUE_FOR_UNIT_PER_ORDER_RATIO = 1;

  private final BacklogGateway backlogGateway;

  private final UnitsPerOrderRatioGateway unitsPerOrderRatioGateway;

  public List<BacklogMonitor> backlogHistoricalMonitor(
      final Workflow workflow,
      final String logisticCenterId,
      final Set<ProcessName> processes,
      final Instant dateFrom,
      final Instant dateTo,
      final Instant viewDate) {

    final Optional<Double> getUnitsPerOrderRatio =
        unitsPerOrderRatioGateway.getUnitsPerOrderRatio(workflow, logisticCenterId, viewDate)
            .filter(ratio -> ratio >= MIN_VALUE_FOR_UNIT_PER_ORDER_RATIO);

    final Double unitsPerOrderRatio = getUnitsPerOrderRatio.orElse(0D);

    final Set<ProcessName> historicalProcesses =
        getCalculableProcesses(processes, getUnitsPerOrderRatio);

    Map<Instant, Map<ProcessName, Map<Instant, Map<ProcessPathName, Integer>>>> backlogHistorical =
        backlogGateway.getBacklogByDateProcessAndPP(workflow, logisticCenterId, historicalProcesses, dateFrom, dateTo);

    if (viewDate.truncatedTo(ChronoUnit.MINUTES).equals(dateTo.truncatedTo(ChronoUnit.MINUTES))) {
      final Map<Instant, Map<ProcessName, Map<Instant, Map<ProcessPathName, Integer>>>> lastBacklogHistorical =
          backlogGateway.getLastBacklogByDateProcessAndPP(workflow, logisticCenterId, historicalProcesses, viewDate);

      backlogHistorical = mergeMaps(backlogHistorical, lastBacklogHistorical);
    }

    List<BacklogMonitor> response = BacklogProjectionUtil.sumBacklogProjection(backlogHistorical, unitsPerOrderRatio);

    BacklogProjectionUtil.order(response);
    return response;
  }

  private Set<ProcessName> getCalculableProcesses(final Set<ProcessName> processes, final Optional<Double> getUnitsPerOrderRatio) {
    return getUnitsPerOrderRatio.map(r -> processes)
        .orElse(
            processes.stream()
                .filter(processName -> !ProcessName.getShippingProcess().contains(processName))
                .collect(Collectors.toSet())
        );
  }

  private Map<Instant, Map<ProcessName, Map<Instant, Map<ProcessPathName, Integer>>>> mergeMaps(
      final Map<Instant, Map<ProcessName, Map<Instant, Map<ProcessPathName, Integer>>>> photos,
      final Map<Instant, Map<ProcessName, Map<Instant, Map<ProcessPathName, Integer>>>> lastPhoto
  ) {
    return Stream.concat(photos.entrySet().stream(), lastPhoto.entrySet().stream())
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  /**
   * Get map backlog by date, process, sla and process path.
   */
  public interface BacklogGateway {
    Map<Instant, Map<ProcessName, Map<Instant, Map<ProcessPathName, Integer>>>> getBacklogByDateProcessAndPP(
        Workflow workflow, String logisticCenter, Set<ProcessName> processes, Instant dateFrom, Instant dateTo);

    Map<Instant, Map<ProcessName, Map<Instant, Map<ProcessPathName, Integer>>>> getLastBacklogByDateProcessAndPP(
        Workflow workflow, String logisticCenter, Set<ProcessName> processes, Instant viewDate);
  }
}
