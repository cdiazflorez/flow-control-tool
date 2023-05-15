package com.mercadolibre.flow.control.tool.feature.staffing.dto;

import com.mercadolibre.flow.control.tool.feature.staffing.domain.StaffingOperationData;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public record StaffingOperationDto(
    Instant lastModifiedDate,
    Map<String, Map<String, StaffingOperationValuesDto>> values
) {

  public record StaffingOperationValuesDto(StaffingOperationDataDto total, List<StaffingOperationDataDto> values) {
  }

  public record StaffingOperationDataDto(
      Instant date,
      Long planned,
      Long plannedSystemic,
      Boolean plannedEdited,
      Boolean plannedSystemicEdited,
      Long plannedNonSystemic,
      Boolean plannedNonSystemicEdited,
      Long presentSystemic,
      Long presentNonSystemic,
      Long deviationSystemic,
      Long deviationNonSystemic,
      Long real,
      Long deviation
  ) {

    public static StaffingOperationDataDto from(final StaffingOperationData staffingOperationData) {
      return new StaffingOperationDataDto(
          staffingOperationData.getDate(),
          staffingOperationData.getPlanned(),
          staffingOperationData.getPlannedSystemic(),
          staffingOperationData.getPlannedEdited(),
          staffingOperationData.getPlannedSystemicEdited(),
          staffingOperationData.getPlannedNonSystemic(),
          staffingOperationData.getPlannedNonSystemicEdited(),
          staffingOperationData.getPresentSystemic(),
          staffingOperationData.getPresentNonSystemic(),
          staffingOperationData.getDeviationSystemic(),
          staffingOperationData.getDeviationNonSystemic(),
          staffingOperationData.getReal(),
          staffingOperationData.getDeviation()
      );
    }

  }
}
