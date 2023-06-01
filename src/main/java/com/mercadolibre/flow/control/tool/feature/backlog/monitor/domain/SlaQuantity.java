package com.mercadolibre.flow.control.tool.feature.backlog.monitor.domain;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SlaQuantity {
  private Instant dateIn;
  private Instant dateOut;
  private Integer quantity;
}
