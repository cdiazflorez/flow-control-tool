package com.mercadolibre.flow.control.tool.client.planningmodelapi;

import static com.mercadolibre.flow.control.tool.client.planningmodelapi.PlanningModelApiUtils.UNIT_PER_ORDER_RATIO;

import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.Metadata;
import com.mercadolibre.flow.control.tool.feature.monitor.usecase.ForecastMetadata;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlanningModelApiAdapter implements ForecastMetadata {

  private final PlanningModelApiClient planningModelApiClient;

  @Override
  public Optional<Double> getUnitsPerOrderRatio(final Workflow workflow,
                                                final String warehouseId,
                                                final Instant viewDate) {
    final ZonedDateTime dateFrom = viewDate.atZone(ZoneOffset.UTC).withFixedOffsetZone();
    final ZonedDateTime dateTo = dateFrom.plusDays(1);

    final List<Metadata> forecastMetadata =
        planningModelApiClient.getForecastMetadata(workflow, warehouseId, dateFrom, dateTo);

    return forecastMetadata.stream()
        .filter(ratio -> UNIT_PER_ORDER_RATIO.equalsIgnoreCase(ratio.key()))
        .findAny()
        .map(Metadata::value)
        .map(Double::parseDouble);
  }
}
