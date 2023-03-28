package com.mercadolibre.flow.control.tool.feature.backlog.status;

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

  final BacklogGateway backlogGateway;
  final UnitsPerOrderRatioGateway unitsPerOrderRatioGateway;

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

      final Optional<Double> ratio =
          unitsPerOrderRatioGateway.getUnitsPerOrderRatio(workflow, logisticCenterId, viewDate);

      final Map<String, Integer> ordersByProcess = ratio.map(r -> processes.stream()
              .collect(
                  Collectors.toMap(
                      ProcessName::getName,
                      value ->
                          (int) (backlogTotalsByProcess.getOrDefault(value, DEFAULT_PROCESS_TOTAL) / r)
                  )
              ))
          .orElse(
              processes.stream()
                  .collect(
                      Collectors
                          .toMap(
                              ProcessName::getName,
                              value -> 0
                          )
                  )
          );

      return new BacklogStatus(
          ordersByProcess,
          ratio.orElse(0.0)
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

  /**
   * Interface for methods used across the Forecast Metadata.
   */
  public interface UnitsPerOrderRatioGateway {
    /**
     * The implementation should return the ratio.
     *
     * @param logisticCenterId outbound
     * @param warehouseId      logistic center id
     * @param viewDate         base date to backlog.
     * @return optional of double.
     */
    Optional<Double> getUnitsPerOrderRatio(
        Workflow logisticCenterId,
        String warehouseId,
        Instant viewDate
    );
  }

}
