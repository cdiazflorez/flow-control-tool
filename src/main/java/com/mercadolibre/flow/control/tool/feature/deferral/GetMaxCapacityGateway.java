package com.mercadolibre.flow.control.tool.feature.deferral;

import java.time.Instant;
import java.util.Map;

/**
 * Gateway planning api to obtain max_capacity.
 */
public interface GetMaxCapacityGateway {
  Map<Instant, Long> getMaxCapacityForHour(String logisticCenterId,
                                              Instant dateFrom,
                                              Instant dateTo);
}
