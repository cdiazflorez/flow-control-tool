package com.mercadolibre.flow.control.tool.client.planningmodelapi.dto;

import com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TotalBacklogProjectionResponse {

  private Instant date;
  private List<Sla> sla;

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Sla {
    private Instant dateOut;
    private Integer quantity;
    private List<ProcessPath> processPath;
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class ProcessPath {
    private ProcessPathName name;
    private Integer quantity;
  }

}
