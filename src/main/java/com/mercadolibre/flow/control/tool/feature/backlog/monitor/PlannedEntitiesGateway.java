package com.mercadolibre.flow.control.tool.feature.backlog.monitor;

import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

/**
 * Gateway planning api to obtain tph, plannedBacklog.
 */
public interface PlannedEntitiesGateway {

  Map<Instant, Map<ProcessName, Integer>> getThroughputByDateAndProcess(Workflow workflow,
                                                                        String logisticCenterId,
                                                                        Instant dateFrom,
                                                                        Instant dateTo,
                                                                        Set<ProcessName> process);

  Map<ProcessPathName, Map<Instant, Map<Instant, Integer>>> getPlannedUnitByPPDateInAndDateOut(Workflow workflow,
                                                                                               String logisticCenterId,
                                                                                               Instant dateFrom,
                                                                                               Instant dateTo);

}
