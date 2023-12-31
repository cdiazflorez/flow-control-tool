package com.mercadolibre.flow.control.tool.feature.backlog.status;

import com.mercadolibre.flow.control.tool.exception.NoForecastMetadataFoundException;
import com.mercadolibre.flow.control.tool.feature.backlog.genericgateway.UnitsPerOrderRatioGateway;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.ValueType;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class BacklogStatusUseCase {

  private static final Integer DEFAULT_PROCESS_TOTAL = 0;

  private final BacklogGateway backlogGateway;

  private final UnitsPerOrderRatioGateway unitsPerOrderRatioGateway;

  public BacklogStatus getBacklogStatus(
      final String logisticCenterId,
      final Workflow workflow,
      final ValueType valueType,
      final Set<ProcessName> processes,
      final Instant viewDate
  ) {

    final Map<ProcessName, Integer> backlogTotalsByProcess = backlogGateway.getBacklogTotalsByProcess(
        logisticCenterId,
        workflow,
        processes,
        viewDate);

    if (valueType.equals(ValueType.ORDERS)) {
      final double minValue = 1;
      final Optional<Double> unitsPerOrderRatio =
          unitsPerOrderRatioGateway.getUnitsPerOrderRatio(workflow, logisticCenterId, viewDate);

      final Map<String, Integer> ordersByProcess =
          unitsPerOrderRatio
              .filter(ratio -> ratio >= minValue)
              .map(ratio -> processes.stream()
                  .collect(
                      Collectors.toMap(
                          ProcessName::getName,
                          value ->
                              (int) (backlogTotalsByProcess.getOrDefault(value, DEFAULT_PROCESS_TOTAL) / ratio)
                      )
                  ))
              .orElseThrow(
                  () -> new NoForecastMetadataFoundException(logisticCenterId)
              );

      return new BacklogStatus(
          ordersByProcess,
          unitsPerOrderRatio.orElse(0.0)
      );
    }

    final Map<String, Integer> unitsByProcess = processes.stream()
        .collect(Collectors.toMap(ProcessName::getName, value -> backlogTotalsByProcess.getOrDefault(value, DEFAULT_PROCESS_TOTAL)));

    return new BacklogStatus(
        unitsByProcess,
        null
    );
  }


  /**
   * Interface for methods used across the Backlog Status.
   */
  public interface BacklogGateway {

    /**
     * The implementation should return the backlog given by process and its total units|orders.
     *
     * @param logisticCenterId logistic center id.
     * @param workflow         outbound
     * @param processes        list of processes to be requested.
     * @param viewDate         base date to backlog.
     * @return map with amount of units by each process.
     */
    Map<ProcessName, Integer> getBacklogTotalsByProcess(
        String logisticCenterId,
        Workflow workflow,
        Set<ProcessName> processes,
        Instant viewDate
    );
  }
}
