package com.mercadolibre.flow.control.tool.feature.forecastdeviation.domain;

import lombok.Getter;

@Getter
public class ForecastDeviationQuantity {
    private Integer planned;
    private Integer real;
    private Integer deviation;
    private Double deviationPercentage;

    public ForecastDeviationQuantity(final Integer planned, final Integer real, final Integer deviation, final Double deviationPercentage) {
        this.planned = planned;
        this.real = real;
        this.deviation = deviation;
        this.deviationPercentage = deviationPercentage;
    }

    public ForecastDeviationQuantity(final Integer planned) {
        this.planned = planned;
    }
}
