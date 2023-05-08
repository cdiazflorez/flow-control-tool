package com.mercadolibre.flow.control.tool.client.planningmodelapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.OutboundProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName;
import java.time.Instant;
import java.util.List;

public record BacklogProjectionResponse(
    @JsonProperty("operation_hour")
    Instant operationHour,
    List<Backlog> backlog
) {

  public record Backlog(
      List<Process> process
  ) {
  }

  public record Process(
      OutboundProcessName name,
      List<Sla> sla
  ) {
  }

  public record ProcessPath(
      ProcessPathName name,
      Integer quantity
  ) {
  }

  public record Sla(
      @JsonProperty("date_out")
      Instant dateOut,
      @JsonProperty("process_path")
      List<ProcessPath> processPath
  ) {
  }

}
