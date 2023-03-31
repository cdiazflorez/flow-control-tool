package com.mercadolibre.flow.control.tool.feature.staffing.operation;

import com.mercadolibre.flow.control.tool.feature.staffing.domain.StaffingOperationData;
import com.mercadolibre.flow.control.tool.feature.staffing.domain.StaffingOperationValues;
import com.mercadolibre.flow.control.tool.feature.staffing.domain.StaffingPlannedData;
import java.util.List;

public class HeadcountStaffingOperationStrategy implements StaffingOperationStrategy {

  @Override
  public StaffingOperationValues getStaffingOperation(List<StaffingPlannedData> staffingPlannedData) {
    return new StaffingOperationValues(
        StaffingOperationData.builder()
            .plannedSystemic(staffingPlannedData.stream().mapToLong(StaffingPlannedData::planned).sum())
            .plannedNonSystemic(staffingPlannedData.stream().mapToLong(StaffingPlannedData::plannedNonSystemic).sum())
            .build(),
        staffingPlannedData.stream()
            .map(staffingPlanned -> StaffingOperationData.builder()
                .date(staffingPlanned.date())
                .plannedSystemic(staffingPlanned.planned())
                .plannedSystemicEdited(staffingPlanned.plannedEdited())
                .plannedNonSystemic(staffingPlanned.plannedNonSystemic())
                .plannedNonSystemicEdited(staffingPlanned.plannedNonSystemicEdited())
                .build())
            .toList()
    );
  }
}
