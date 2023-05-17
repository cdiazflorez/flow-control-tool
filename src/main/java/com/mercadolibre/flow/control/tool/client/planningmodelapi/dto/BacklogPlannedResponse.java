package com.mercadolibre.flow.control.tool.client.planningmodelapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName;
import java.time.Instant;

public record BacklogPlannedResponse(

    GroupKey group,

    double total

) {
  public record GroupKey(

      @JsonProperty("process_path")
      ProcessPathName processPath,

      @JsonProperty("date_in")
      Instant dateIn,

      @JsonProperty("date_out")
      Instant dateOut

  ) {
  }
}
