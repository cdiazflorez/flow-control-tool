package com.mercadolibre.flow.control.tool.feature.entity;

import java.util.Locale;

public enum ProcessName {

  WAVING,
  PICKING,
  BATCH_SORTER,
  WALL_IN,
  PACKING,
  PACKING_WALL,
  HU_ASSEMBLY,
  SHIPPED;

  public static ProcessName from(final String value) {
    return valueOf(value.toUpperCase(Locale.getDefault()));
  }

  public String getName() {
    return name().toLowerCase(Locale.getDefault());
  }
}
