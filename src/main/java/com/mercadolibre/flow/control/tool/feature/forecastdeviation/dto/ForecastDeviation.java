package com.mercadolibre.flow.control.tool.feature.forecastdeviation.dto;

import java.time.Instant;
import java.util.List;

public record ForecastDeviation(
    Integer totalPlanned,
    Integer totalReal,
    Integer totalDeviation,
    Double totalPercentageDeviation,
    List<DeviationDetail> details
) {

  public record DeviationDetail(
      Instant date,
      Integer real,
      Integer planned,
      Integer totalDeviation,
      Double percentageDeviation
  ) {
  }

}
