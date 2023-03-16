package com.mercadolibre.flow.control.tool.feature.monitor.usecase;

import com.mercadolibre.flow.control.tool.client.planningmodelapi.Workflow;
import java.time.Instant;
import java.util.Optional;

/**
 * Interface of PlanningModelApiAdapter.
 * signature required to obtain units for order ratios.
 */
public interface ForecastMetadata {
  Optional<Double> getUnitsPerOrderRatio(Workflow workflow,
                                         String warehouseId,
                                         Instant viewDate);
}
