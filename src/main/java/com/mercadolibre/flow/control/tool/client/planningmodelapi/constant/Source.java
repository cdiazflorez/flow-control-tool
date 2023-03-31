package com.mercadolibre.flow.control.tool.client.planningmodelapi.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Locale;

public enum Source {
  FORECAST,
  SIMULATION;

  @JsonValue
  public String getName() {
    return name().toLowerCase(Locale.getDefault());
  }
}
