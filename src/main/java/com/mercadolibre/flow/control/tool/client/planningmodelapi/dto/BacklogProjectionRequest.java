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
    public record Process(
        OutboundProcessName name,
        Set<ProcessPathByDateOut> processPath
    ) {
      public record ProcessPathByDateOut(
          ProcessPathName name,
          Set<QuantityByDateOut> quantity
      ) {
        public record QuantityByDateOut(
            Instant dateOut,
            int total
        ) {
        }
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
          Integer total
      ) {
      }
    }
  }

  public record Throughput(
      Instant operationHour,
      Set<QuantityByProcessName> quantityByProcessName
  ) {
    public record QuantityByProcessName(
        OutboundProcessName name,
        Integer total
    ) {
    }
  }
}


