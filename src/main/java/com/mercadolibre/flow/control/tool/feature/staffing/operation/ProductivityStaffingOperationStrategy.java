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

public class ProductivityStaffingOperationStrategy implements StaffingOperationStrategy {

  @Override
  public StaffingOperationValues getStaffingOperation(final List<StaffingPlannedData> staffingPlannedData,
                                                      final List<MetricData> metricsData) {

    final Map<Instant, Long> realByDate = metricsData.stream()
        .collect(
            Collectors.toMap(
                MetricData::date,
                MetricData::productivity
            )
        );

    final Optional<Instant> maxDateOptional = staffingPlannedData.stream().max(Comparator.comparing(StaffingPlannedData::date))
        .map(StaffingPlannedData::date);

    Optional<Double> realTotalOptional = Optional.empty();

    if (maxDateOptional.isPresent()) {
      realTotalOptional = realByDate.containsKey(maxDateOptional.get())
          ? Optional.of(realByDate.values().stream().mapToLong(Long::longValue).average().orElse(0D))
          : Optional.empty();
    }

    final long plannedTotal = Math.round(staffingPlannedData.stream()
                                             .mapToLong(StaffingPlannedData::planned)
                                             .average()
                                             .orElse(0D));

    return new StaffingOperationValues(
        StaffingOperationData.builder()
            .planned(plannedTotal)
            .real(realTotalOptional.map(Math::round).orElse(null))
            .build(),
        staffingPlannedData.stream()
            .map(staffingPlanned -> {
              final long planned = staffingPlanned.planned();
              final Optional<Long> realOptional = Optional.ofNullable(realByDate.get(staffingPlanned.date()));
              return StaffingOperationData.builder()
                  .date(staffingPlanned.date())
                  .planned(planned)
                  .plannedEdited(staffingPlanned.plannedEdited())
                  .real(realOptional.orElse(null))
                  .deviation(realOptional.map(real -> real - planned).orElse(null))
                  .build();
            })
            .toList()
    );
  }
}
