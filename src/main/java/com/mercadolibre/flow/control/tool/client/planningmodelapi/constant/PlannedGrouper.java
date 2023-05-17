package com.mercadolibre.flow.control.tool.client.planningmodelapi.constant;

public enum PlannedGrouper {

  DATE_IN,
  DATE_OUT,
  PROCESS_PATH;

  public String getName() {
    return name().toLowerCase();
  }
}
