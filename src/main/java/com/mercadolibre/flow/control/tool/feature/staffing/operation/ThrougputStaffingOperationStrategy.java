package com.mercadolibre.flow.control.tool.feature.staffing.operation;

import com.mercadolibre.flow.control.tool.feature.staffing.domain.MetricData;
import com.mercadolibre.flow.control.tool.feature.staffing.domain.StaffingOperationData;
import com.mercadolibre.flow.control.tool.feature.staffing.domain.StaffingOperationValues;
import com.mercadolibre.flow.control.tool.feature.staffing.domain.StaffingPlannedData;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ThrougputStaffingOperationStrategy implements StaffingOperationStrategy {

  @Override
  public StaffingOperationValues getStaffingOperation(final List<StaffingPlannedData> staffingPlannedData,
                                                      final List<MetricData> metricsData) {

    final Optional<Instant> maxDateOptional = staffingPlannedData.stream().max(Comparator.comparing(StaffingPlannedData::date))
        .map(StaffingPlannedData::date);

    final Map<Instant, Long> realByDate = metricsData.stream()
        .collect(
            Collectors.toMap(
                MetricData::date,
                MetricData::throughput
            )
        );

    final var shouldCalculateRealTotal = maxDateOptional.isPresent() && realByDate.containsKey(maxDateOptional.get());
    final Optional<Long> realTotalOptional = shouldCalculateRealTotal
        ? Optional.of(realByDate.values().stream().mapToLong(Long::longValue).sum())
        : Optional.empty();

    return new StaffingOperationValues(
        StaffingOperationData.builder()
            .planned(staffingPlannedData.stream().mapToLong(StaffingPlannedData::planned).sum())
            .real(realTotalOptional.orElse(null))
            .build(),
        staffingPlannedData.stream()
            .map(staffingPlanned -> {
              final long planned = staffingPlanned.planned();
              final Optional<Long> realOptional = Optional.ofNullable(realByDate.get(staffingPlanned.date()));
              return StaffingOperationData.builder()
                  .date(staffingPlanned.date())
                  .planned(planned)
                  .real(realOptional.orElse(null))
                  .deviation(realOptional.map(real -> real - planned).orElse(null))
                  .build();
            })
            .toList()
    );
  }
}
