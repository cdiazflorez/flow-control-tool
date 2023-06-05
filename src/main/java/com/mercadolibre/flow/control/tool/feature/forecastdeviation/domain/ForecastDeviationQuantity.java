package com.mercadolibre.flow.control.tool.feature.forecastdeviation.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ForecastDeviationQuantity {
    private final Integer planned;
    private final Integer real;
    private final Integer deviation;
    private final Double deviationPercentage;
}
