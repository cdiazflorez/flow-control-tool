package com.mercadolibre.flow.control.tool.feature.staffing.operation;

import static java.util.stream.Collectors.toMap;

import com.mercadolibre.flow.control.tool.feature.staffing.domain.MetricData;
import com.mercadolibre.flow.control.tool.feature.staffing.domain.StaffingOperationData;
import com.mercadolibre.flow.control.tool.feature.staffing.domain.StaffingOperationValues;
import com.mercadolibre.flow.control.tool.feature.staffing.domain.StaffingPlannedData;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class HeadcountStaffingOperationStrategy implements StaffingOperationStrategy {

  @Override
  public StaffingOperationValues getStaffingOperation(final List<StaffingPlannedData> staffingPlannedData,
                                                      final List<MetricData> metricsData) {

    final Map<Instant, Long> presentSystemicByDate = metricsData.stream()
        .collect(
            toMap(
                MetricData::date,
                v -> v.productivity() == 0 ? 0 : v.throughput() / v.productivity()
            )
        );

    final Map<Instant, Double> ratioPresentNonSystemicByDate = staffingPlannedData.stream()
        .collect(
            toMap(
                StaffingPlannedData::date,
                v -> v.planned() == 0 ? 0 : (double) v.plannedNonSystemic() / (double) v.planned()
            )
        );

    final Map<Instant, Long> presentNonSystemicByDate = presentSystemicByDate.entrySet().stream()
        .collect(
            toMap(
                Map.Entry::getKey,
                v -> Math.round(v.getValue() * ratioPresentNonSystemicByDate.get(v.getKey()))
            )
        );

    final Optional<Instant> maxDateOptional = staffingPlannedData.stream()
        .map(StaffingPlannedData::date)
        .max(Comparator.naturalOrder());

    final long plannedSystemicTotal = staffingPlannedData.stream().mapToLong(StaffingPlannedData::planned).sum();
    final long plannedNonSystemicTotal = staffingPlannedData.stream().mapToLong(StaffingPlannedData::plannedNonSystemic).sum();

    Optional<Long> presentSystemicTotal = Optional.empty();
    Optional<Long> presentNonSystemicTotal = Optional.empty();

    if (maxDateOptional.isPresent()) {
      presentSystemicTotal = presentSystemicByDate.containsKey(maxDateOptional.get())
          ? Optional.of(presentSystemicByDate.values().stream().mapToLong(Long::longValue).sum())
          : Optional.empty();
      presentNonSystemicTotal = presentNonSystemicByDate.containsKey(maxDateOptional.get())
          ? Optional.of((presentNonSystemicByDate.values().stream().mapToLong(Long::longValue).sum()))
          : Optional.empty();
    }

    return new StaffingOperationValues(
        StaffingOperationData.builder()
            .plannedSystemic(plannedSystemicTotal)
            .plannedNonSystemic(plannedNonSystemicTotal)
            .presentSystemic(presentSystemicTotal.orElse(null))
            .presentNonSystemic(presentNonSystemicTotal.orElse(null))
            .build(),
        staffingPlannedData.stream()
            .map(staffingPlanned -> buildStaffingOperationData(staffingPlanned, presentSystemicByDate, presentNonSystemicByDate))
            .toList()
    );
  }

  private StaffingOperationData buildStaffingOperationData(final StaffingPlannedData staffingPlanned,
                                                           final Map<Instant, Long> presentSystemicByDate,
                                                           final Map<Instant, Long> presentNonSystemicByDate) {
    final Instant date = staffingPlanned.date();
    final long plannedSystemic = staffingPlanned.planned();
    final long plannedNonSystemic = staffingPlanned.plannedNonSystemic();
    final Optional<Long> presentSystemicOptional = Optional.ofNullable(presentSystemicByDate.get(date));
    final Optional<Long> presentNonSystemicOptional = Optional.ofNullable(presentNonSystemicByDate.get(date));
    return StaffingOperationData.builder()
        .date(date)
        .plannedSystemic(plannedSystemic)
        .plannedSystemicEdited(staffingPlanned.plannedEdited())
        .plannedNonSystemic(plannedNonSystemic)
        .plannedNonSystemicEdited(staffingPlanned.plannedNonSystemicEdited())
        .presentSystemic(presentSystemicOptional.orElse(null))
        .presentNonSystemic(presentNonSystemicOptional.orElse(null))
        .deviationSystemic(presentSystemicOptional.map(presentSystemic -> plannedSystemic - presentSystemic).orElse(null))
        .deviationNonSystemic(presentNonSystemicOptional.map(presentNonSystemic -> plannedNonSystemic - presentNonSystemic).orElse(null))
        .build();
  }
}
