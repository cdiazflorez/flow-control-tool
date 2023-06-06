package com.mercadolibre.flow.control.tool.feature.forecastdeviation.domain;

import lombok.Getter;

@Getter
public class ForecastDeviationQuantity {
    private final Integer planned;
    private final Integer real;
    private final Integer deviation;
    private final Double deviationPercentage;

    public ForecastDeviationQuantity(final Integer planned, final Integer real, final Integer deviation, final Double deviationPercentage) {
        this.planned = planned;
        this.real = real;
        this.deviation = deviation;
        this.deviationPercentage = deviationPercentage;
    }

    public ForecastDeviationQuantity(Integer planned) {
        this.planned = planned;
        this.real = null;
        this.deviation = null;
        this.deviationPercentage = null;
    }
}
