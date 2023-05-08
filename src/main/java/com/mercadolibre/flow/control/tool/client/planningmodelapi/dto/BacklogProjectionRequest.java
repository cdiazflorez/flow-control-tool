package com.mercadolibre.flow.control.tool.client.planningmodelapi.dto;

import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.OutboundProcessName;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.PlanningWorkflow;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName;
import java.time.Instant;
import java.util.List;

public record BacklogProjectionRequest(
    Backlog backlog,
    PlannedUnit plannedUnit,
    List<Throughput> throughput,
    Instant dateFrom,
    Instant dateTo,
    PlanningWorkflow workflow
) {
  public record Backlog(
      List<Process> process
  ) {
  }

  public record PlannedUnit(
      List<ProcessPath> processPath
  ) {
  }

  public record Process(
      OutboundProcessName name,
      List<ProcessPath> processPath,
      Integer total
  ) {
  }

  public record ProcessPath(
      ProcessPathName name,
      List<Quantity> quantity
  ) {
  }

  public record Quantity(
      Instant dateIn,
      Instant dateOut,
      Integer total
  ) {
  }

  public record Throughput(
      Instant operationHour,
      List<Process> process
  ) {
  }
}

