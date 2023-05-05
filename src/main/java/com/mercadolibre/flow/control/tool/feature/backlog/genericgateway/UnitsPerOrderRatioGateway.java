package com.mercadolibre.flow.control.tool.feature.backlog.genericgateway;

import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import java.time.Instant;
import java.util.Optional;

/**
 * Interface for methods used across the Forecast Metadata.
 */
public interface UnitsPerOrderRatioGateway {
  /**
   * The implementation should return the ratio.
   *
   * @param logisticCenterId outbound
   * @param warehouseId      logistic center id
   * @param viewDate         base date to backlog.
   * @return optional of double.
   */
  Optional<Double> getUnitsPerOrderRatio(
      Workflow logisticCenterId,
      String warehouseId,
      Instant viewDate
  );
}
