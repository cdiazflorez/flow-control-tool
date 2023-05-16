package com.mercadolibre.flow.control.tool.feature.backlog.monitor;

import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.OutboundProcessName;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.PlanningWorkflow;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.BacklogLimit;
import java.time.Instant;
import java.util.List;

/**
 * This interface defines a gateway for retrieving backlog limits for a logistic center, workflow, and list of processes
 * between the specified base dates.
 * The implementation of this interface should return a list of {@link BacklogLimit} objects representing the limits for each process
 * based on the provided parameters.
 */
public interface GetBacklogLimitGatewayAux {

  /**
   * The implementation should return the backlog limits given by upper and lower.
   *
   * @param logisticCenterId logistic center id.
   * @param workflow         outbound
   * @param processes        list of processes to be requested.
   * @param dateFrom         base date to backlog.
   * @param dateTo           base date to backlog.
   * @return List with Limits by each process.
   */
  List<BacklogLimit> getBacklogLimits(
      String logisticCenterId,
      PlanningWorkflow workflow,
      List<OutboundProcessName> processes,
      Instant dateFrom,
      Instant dateTo
  );
}
