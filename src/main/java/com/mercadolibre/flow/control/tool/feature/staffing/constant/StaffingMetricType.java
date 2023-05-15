package com.mercadolibre.flow.control.tool.feature.staffing.constant;

import com.mercadolibre.flow.control.tool.feature.staffing.operation.HeadcountStaffingOperationStrategy;
import com.mercadolibre.flow.control.tool.feature.staffing.operation.ProductivityStaffingOperationStrategy;
import com.mercadolibre.flow.control.tool.feature.staffing.operation.StaffingOperationStrategy;
import com.mercadolibre.flow.control.tool.feature.staffing.operation.ThrougputStaffingOperationStrategy;
import java.util.Locale;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public enum StaffingMetricType {
  HEADCOUNT(new HeadcountStaffingOperationStrategy()),
  PRODUCTIVITY(new ProductivityStaffingOperationStrategy()),
  THROUGHPUT(new ThrougputStaffingOperationStrategy());

  private final StaffingOperationStrategy staffingOperationStrategy;

  public StaffingOperationStrategy getStaffingOperationStrategy() {
    return staffingOperationStrategy;
  }

  public static StaffingMetricType from(final String value) {
    return valueOf(value.toUpperCase(Locale.getDefault()));
  }

  public String getName() {
    return name().toLowerCase(Locale.getDefault());
  }
}
