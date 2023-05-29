package com.mercadolibre.flow.control.tool.client.planningmodelapi.dto;

import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.OutboundProcessName;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.PlanningWorkflow;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName;
import java.time.Instant;
import java.util.Set;

public record BacklogProjectionRequest(
    Backlog backlog,
    PlannedUnit plannedUnit,
    Set<Throughput> throughput,
    Instant dateFrom,
    Instant dateTo,
    PlanningWorkflow workflow
) {
  public record Backlog(
      Set<Process> process
  ) {
  }

  public record PlannedUnit(
      Set<ProcessPath> processPath
  ) {
  }

  public record Throughput(
      Instant operationHour,
      Set<Process> quantityByProcessName
  ) {
  }

  public record Process(
      OutboundProcessName name,
      Set<ProcessPath> processPath,
      Integer total
  ) {
  }

  public record ProcessPath(
      ProcessPathName name,
      Set<Quantity> quantity
  ) {
  }

  public record Quantity(
      Instant dateIn,
      Instant dateOut,
      Integer total
  ) {
  }
}
