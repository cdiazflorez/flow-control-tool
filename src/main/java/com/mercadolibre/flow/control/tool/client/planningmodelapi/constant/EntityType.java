package com.mercadolibre.flow.control.tool.client.planningmodelapi.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Locale;

public enum EntityType {
  HEADCOUNT,
  PRODUCTIVITY,
  THROUGHPUT,
  BACKLOG_LOWER_LIMIT,
  BACKLOG_UPPER_LIMIT,
  BACKLOG_LOWER_LIMIT_SHIPPING,
  BACKLOG_UPPER_LIMIT_SHIPPING;

  @JsonValue
  public String getName() {
    return name().toLowerCase(Locale.getDefault());
  }
}
