package com.mercadolibre.flow.control.tool.client.staffingapi.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Locale;

public enum StaffingWorkflow {
  FBM_WMS_OUTBOUND;

  public static StaffingWorkflow from(final String value) {
    return valueOf(value.toUpperCase(Locale.getDefault()).replace('-', '_'));
  }

  @JsonValue
  public String getName() {
    return this.toString().toLowerCase(Locale.getDefault()).replace('_', '-');
  }
}
