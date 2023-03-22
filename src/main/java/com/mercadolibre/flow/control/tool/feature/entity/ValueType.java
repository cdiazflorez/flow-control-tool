package com.mercadolibre.flow.control.tool.feature.entity;

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
