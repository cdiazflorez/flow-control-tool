package com.mercadolibre.flow.control.tool.feature.status.usecase;

import com.mercadolibre.flow.control.tool.feature.status.usecase.constant.Processes;
import com.mercadolibre.flow.control.tool.feature.status.usecase.constant.ValueType;
import com.mercadolibre.flow.control.tool.feature.status.usecase.constant.Workflow;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class BacklogStatusUseCase {

  private static final Integer DEFAULT_PROCESS_TOTAL = 0;

  final BacklogGateway backlogGateway;

  public Map<Processes, Integer> getBacklogTotalsByProcess(
      final String logisticCenterId,
      final Workflow workflow,
      final ValueType valueType,
      final Set<Processes> processes,
      final Instant viewDate
  ) {

    final Map<Processes, Integer> backlogTotalsByProcess = backlogGateway.getBacklogTotalsByProcess(
        logisticCenterId,
        workflow,
        processes,
        viewDate);

    return processes.stream()
        .collect(Collectors.toMap(Function.identity(), value -> backlogTotalsByProcess.getOrDefault(value, DEFAULT_PROCESS_TOTAL)));

  }

  /**
   * Interface for methods used across the Backlog Status.
   */
  public interface BacklogGateway {

    /**
     * The implementation should return the backlog given by process and its total units|orders.
     * @param logisticCenterId logistic center id.
     * @param workflow outbound
     * @param processes list of processes to be requested.
     * @param viewDate base date to backlog.
     * @return map with amount of units by each process.
     */
    Map<Processes, Integer> getBacklogTotalsByProcess(
        String logisticCenterId,
        Workflow workflow,
        Set<Processes> processes,
        Instant viewDate
    );
  }

}
