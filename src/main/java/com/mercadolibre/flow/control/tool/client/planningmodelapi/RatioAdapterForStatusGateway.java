package com.mercadolibre.flow.control.tool.client.planningmodelapi;

import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.Metadata;
import com.mercadolibre.flow.control.tool.feature.status.usecase.BacklogStatusUseCase.UnitsPerOrderRatioGateway;
import com.mercadolibre.flow.control.tool.feature.status.usecase.constant.Workflow;
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
public class RatioAdapterForStatusGateway implements UnitsPerOrderRatioGateway {

  private static final String UNIT_PER_ORDER_RATIO = "units_per_order_ratio";

  private final PlanningModelApiClient planningModelApiClient;

  @Override
  public Optional<Double> getUnitsPerOrderRatio(final Workflow workflow,
                                                final String logisticCenterId,
                                                final Instant viewDate) {
    final ZonedDateTime dateTime = ZonedDateTime.ofInstant(viewDate, ZoneOffset.UTC);

    final List<Metadata> forecastMetadata =
        planningModelApiClient.getForecastMetadata(workflow, logisticCenterId, dateTime);

    return forecastMetadata.stream()
        .filter(ratio -> UNIT_PER_ORDER_RATIO.equalsIgnoreCase(ratio.key()))
        .findAny()
        .map(Metadata::value)
        .map(Double::parseDouble);
  }
}