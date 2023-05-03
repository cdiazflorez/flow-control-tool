package com.mercadolibre.flow.control.tool.client;

import com.mercadolibre.flow.control.tool.feature.backlog.monitor.BacklogProjectedUseCase;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessPath;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * TODO: Temporary class. Should be removed once the real adapter has been implemented.
 */
@Component
public class AdapterAux implements BacklogProjectedUseCase.PlanningEntitiesGateway, BacklogProjectedUseCase.BacklogProjectionGateway {

  @Override
  public List<BacklogProjectedUseCase.Throughput> getThroughput(Workflow workflow, String logisticCenterId, Instant dateFrom,
                                                                Instant dateTo,
                                                                Set<ProcessName> process) {
    return Collections.emptyList();
  }

  @Override
  public List<BacklogProjectedUseCase.PlannedBacklog> getPlannedBacklog(Workflow workflow, String logisticCenterId, Instant dateFrom,
                                                                        Instant dateTo) {
    return Collections.emptyList();
  }

  @Override
  public Map<Instant, Map<ProcessName, Map<Instant, Map<ProcessPath, Integer>>>> executeBacklogProjection(
      Instant dateFrom,
      Instant dateTo,
      Set<ProcessName> process,
      Map<ProcessName, Integer> currentBacklogs,
      List<BacklogProjectedUseCase.Throughput> throughput,
      List<BacklogProjectedUseCase.PlannedBacklog> plannedBacklogs) {
    return Collections.emptyMap();
  }
}
