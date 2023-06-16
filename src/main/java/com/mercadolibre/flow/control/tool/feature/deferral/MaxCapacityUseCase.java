package com.mercadolibre.flow.control.tool.feature.deferral;

import com.mercadolibre.flow.control.tool.feature.deferral.dto.MaximumCapacityDataDto;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MaxCapacityUseCase {

  private final GetMaxCapacityGateway getMaxCapacityGateway;

  public List<MaximumCapacityDataDto> getMaxCapacity(final String logisticCenterId,
                                                     final Instant dateFrom,
                                                     final Instant dateTo) {

    final Map<Instant, Long> maxCapacityForHour =
        getMaxCapacityGateway.getMaxCapacityForHour(logisticCenterId, dateFrom, dateTo);

    return maxCapacityForHour.entrySet().stream()
        .map(
            capacity ->
                new MaximumCapacityDataDto(
                    capacity.getKey(),
                    capacity.getValue()
                )
        ).toList();
  }
}
