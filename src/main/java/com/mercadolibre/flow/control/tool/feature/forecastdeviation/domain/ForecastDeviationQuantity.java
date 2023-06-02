package com.mercadolibre.flow.control.tool.feature.forecastdeviation.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ForecastDeviationQuantity {
    private final Long planned;
    private final Long real;
    private final Long deviation;
    private final Double deviationPercentage;
}
