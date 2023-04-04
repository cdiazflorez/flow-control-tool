package com.mercadolibre.flow.control.tool.client.staffingapi.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetricDto {

  private Instant date;

  private long effProductivity;

  private long throughput;

}
