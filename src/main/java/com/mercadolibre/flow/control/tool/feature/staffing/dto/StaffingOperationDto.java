package com.mercadolibre.flow.control.tool.feature.staffing.dto;

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
      Boolean plannedNonSystemicEdited
  ) {}
}
