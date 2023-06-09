package com.mercadolibre.flow.control.tool.feature.forecastdeviation.domain;

import java.time.Instant;
import java.util.Map;

public record ForecastDeviationData(ForecastDeviationQuantity totalForecastDeviationQuantity,
                                    Map<Instant, ForecastDeviationQuantity> forecastDeviationQuantityByDate) {
}
