package com.mercadolibre.flow.control.tool.client.planningmodelapi.adapter;

import static java.util.Collections.emptyMap;

import com.mercadolibre.flow.control.tool.feature.deferral.GetMaxCapacityGateway;
import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MaxCapacityAdapterAux implements GetMaxCapacityGateway {
  @Override
  public Map<Instant, Long> getMaxCapacityForHour(final String logisticCenterId,
                                                     final Instant dateFrom,
                                                     final Instant dateTo) {
    return emptyMap();
  }
}
