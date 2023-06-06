package com.mercadolibre.flow.control.tool.client.planningmodelapi.dto;

import com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName;
import java.time.Instant;
import java.util.Set;

public record TotalBacklogProjectionRequest(
    Instant dateFrom,
    Instant dateTo,
    Backlog backlog,
    PlannedUnit plannedUnit,
    Set<Throughput> throughput
) {
  public record Backlog(
      Set<ProcessPathByDateOut> processPath
  ) {
    public record ProcessPathByDateOut(
        ProcessPathName name,
        Set<QuantityByDateOut> quantity
    ) {
      public record QuantityByDateOut(
          Instant dateOut,
          int quantity
      ) {
      }
    }
  }

  public record PlannedUnit(
      Set<ProcessPathByDateInOut> processPath
  ) {
    public record ProcessPathByDateInOut(
        ProcessPathName name,
        Set<QuantityByDateInOut> quantity
    ) {
      public record QuantityByDateInOut(
          Instant dateIn,
          Instant dateOut,
          int quantity
      ) {
      }
    }
  }

  public record Throughput(
      Instant date,
      int quantity
  ) {
  }
}
