package com.mercadolibre.flow.control.tool.feature.status.usecase.constant;

import java.util.Locale;

public enum ValueType {
  UNITS,
  ORDERS;

  public static ValueType from(final String value) {
    return valueOf(value.toUpperCase(Locale.getDefault()));
  }

  public String getName() {
    return name().toLowerCase(Locale.getDefault());
  }
}
