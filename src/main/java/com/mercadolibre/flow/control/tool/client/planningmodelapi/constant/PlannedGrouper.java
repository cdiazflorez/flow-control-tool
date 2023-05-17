package com.mercadolibre.flow.control.tool.client.planningmodelapi.constant;

import java.util.Locale;

public enum PlannedGrouper {

  DATE_IN,
  DATE_OUT,
  PROCESS_PATH;

  public String getName() {
    return name().toLowerCase(Locale.getDefault());
  }
}
