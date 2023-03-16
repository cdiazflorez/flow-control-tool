package com.mercadolibre.flow.control.tool.client.backlog;

import com.mercadolibre.flow.control.tool.feature.status.usecase.BacklogStatusUseCase.BacklogGateway;
import com.mercadolibre.flow.control.tool.feature.status.usecase.constant.Processes;
import com.mercadolibre.flow.control.tool.feature.status.usecase.constant.Workflow;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// TODO: This will be implemented in another task
@Component
@RequiredArgsConstructor
public class BacklogAdapter implements BacklogGateway {

  @Override
  public Map<Processes, Integer> getBacklogTotalsByProcess(
      final String logisticCenterId,
      final Workflow workflow,
      final Set<Processes> processes,
      final Instant viewDate
  ) {
    return Map.of();
  }
}
