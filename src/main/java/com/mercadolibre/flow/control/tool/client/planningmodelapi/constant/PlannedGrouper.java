package com.mercadolibre.flow.control.tool.client.planningmodelapi.constant;

import java.util.Locale;

public enum PlannedGrouper {

  DATE_IN,
  DATE_OUT,
  PROCESS_PATH;

  public static PlannedGrouper from(final String value) {
    return valueOf(value.toUpperCase(Locale.getDefault()));
  }

  public String getName() {
    return name().toLowerCase(Locale.getDefault());
  }
}
