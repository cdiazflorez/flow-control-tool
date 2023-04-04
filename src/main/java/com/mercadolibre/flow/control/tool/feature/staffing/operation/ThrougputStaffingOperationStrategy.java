package com.mercadolibre.flow.control.tool.feature.staffing.operation;

import com.mercadolibre.flow.control.tool.feature.staffing.domain.StaffingOperationData;
import com.mercadolibre.flow.control.tool.feature.staffing.domain.StaffingOperationValues;
import com.mercadolibre.flow.control.tool.feature.staffing.domain.StaffingPlannedData;
import java.util.List;

public class ThrougputStaffingOperationStrategy implements StaffingOperationStrategy {

  @Override
  public StaffingOperationValues getStaffingOperation(List<StaffingPlannedData> staffingPlannedData) {
    return new StaffingOperationValues(
        StaffingOperationData.builder()
            .planned(staffingPlannedData.stream().mapToLong(StaffingPlannedData::planned).sum())
            .build(),
        staffingPlannedData.stream()
            .map(staffingPlanned -> StaffingOperationData.builder()
                .date(staffingPlanned.date())
                .planned(staffingPlanned.planned())
                .build())
            .toList()
    );
  }
}
