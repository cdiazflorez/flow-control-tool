package com.mercadolibre.flow.control.tool.feature.backlog.monitor.domain;

import com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName;
import java.time.Instant;
import java.util.List;

public record ProjectionTotal(
    Instant dateOperation,
    List<SlaProjected> slas

) {

  public record SlaProjected(
      Instant date,
      int quantity,
      List<Path> processPaths
  ) {
  }

  public record Path(
      ProcessPathName name,
      int quantity
  ) {

  }
}
