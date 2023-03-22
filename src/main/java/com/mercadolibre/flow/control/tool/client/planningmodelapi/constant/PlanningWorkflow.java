package com.mercadolibre.flow.control.tool.client.planningmodelapi.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Locale;

public enum PlanningWorkflow {
  FBM_WMS_OUTBOUND;

  public static PlanningWorkflow from(final String value) {
    return valueOf(value.toUpperCase(Locale.getDefault()).replace('-', '_'));
  }

  @JsonValue
  public String getName() {
    return this.toString().toLowerCase(Locale.getDefault()).replace('_', '-');
  }
}
