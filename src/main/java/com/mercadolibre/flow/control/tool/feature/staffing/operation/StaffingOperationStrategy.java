package com.mercadolibre.flow.control.tool.feature.staffing.operation;

import com.mercadolibre.flow.control.tool.feature.staffing.domain.StaffingOperationValues;
import com.mercadolibre.flow.control.tool.feature.staffing.domain.StaffingPlannedData;
import java.util.List;

/**
 * Interface used to obtain the total and the values of the staffing operation.
 */
public interface StaffingOperationStrategy {

  StaffingOperationValues getStaffingOperation(List<StaffingPlannedData> staffingPlannedData);

}
