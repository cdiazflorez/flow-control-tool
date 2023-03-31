package com.mercadolibre.flow.control.tool.feature.staffing.operation;

import com.mercadolibre.flow.control.tool.feature.staffing.domain.StaffingOperationData;
import com.mercadolibre.flow.control.tool.feature.staffing.domain.StaffingOperationValues;
import com.mercadolibre.flow.control.tool.feature.staffing.domain.StaffingPlannedData;
import java.util.List;

public class ProductivityStaffingOperationStrategy implements StaffingOperationStrategy {

  @Override
  public StaffingOperationValues getStaffingOperation(List<StaffingPlannedData> staffingPlannedData) {
    return new StaffingOperationValues(
        StaffingOperationData.builder()
            .planned(
                (long) staffingPlannedData.stream()
                    .mapToLong(StaffingPlannedData::planned)
                    .average()
                    .orElse(0D)
            )
            .build(),
        staffingPlannedData.stream()
            .map(staffingPlanned -> StaffingOperationData.builder()
                .date(staffingPlanned.date())
                .planned(staffingPlanned.planned())
                .plannedEdited(staffingPlanned.plannedEdited())
                .build())
            .toList()
    );
  }
}
