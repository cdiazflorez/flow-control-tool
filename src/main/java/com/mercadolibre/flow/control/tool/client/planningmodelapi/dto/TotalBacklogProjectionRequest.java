package com.mercadolibre.flow.control.tool.client.planningmodelapi.dto;

import com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName;
import java.time.Instant;
import java.util.List;

public record TotalBacklogProjectionRequest(
    Instant dateFrom,
    Instant dateTo,
    Backlog backlog,
    PlannedUnit plannedUnit,
    List<Throughput> throughput
) {
  public record Backlog(
      List<ProcessPath> processPath
  ) {
  }

  public record PlannedUnit(
      List<ProcessPath> processPath
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
      Integer quantity
  ) {
  }

  public record Throughput(
      Instant date,
      Integer quantity
  ) {
  }
}

