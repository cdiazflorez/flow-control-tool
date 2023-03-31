package com.mercadolibre.flow.control.tool.feature.staffing.constant;

import java.util.Locale;

public enum StaffingType {
  HEADCOUNT,
  PRODUCTIVITY,
  THROUGHPUT;

  public static StaffingType from(final String value) {
    return valueOf(value.toUpperCase(Locale.getDefault()));
  }

  public String getName() {
    return name().toLowerCase(Locale.getDefault());
  }
}
