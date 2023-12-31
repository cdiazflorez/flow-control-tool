package com.mercadolibre.flow.control.tool.feature.backlog.monitor;

import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

/**
 * Interface for methods used across the Backlog Status.
 */
public interface BacklogProjectedGateway {

  /**
   * The implementation should return the backlog given by process and its total units|orders.
   *
   * @param logisticCenterId logistic center id.
   * @param workflow         outbound
   * @param processes        list of processes to be requested.
   * @param viewDate         base date to backlog.
   * @return map with amount of units by each process.
   */
  Map<ProcessName, Map<ProcessPathName, Map<Instant, Integer>>> getBacklogTotalsByProcessAndPPandSla(
      String logisticCenterId,
      Workflow workflow,
      Set<ProcessName> processes,
      Instant viewDate
  );

}
