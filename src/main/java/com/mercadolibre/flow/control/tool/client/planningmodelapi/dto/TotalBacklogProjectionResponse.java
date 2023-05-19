package com.mercadolibre.flow.control.tool.client.planningmodelapi.dto;

import com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName;
import java.time.Instant;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TotalBacklogProjectionResponse {

  Instant date;
  List<Sla> sla;

  @Data
  @NoArgsConstructor
  public static class Sla {
    Instant dateOut;
    Integer quantity;
    List<ProcessPath> processPath;
  }

  @Data
  @NoArgsConstructor
  public static class ProcessPath {
    ProcessPathName name;
    Integer quantity;
  }

}
