package com.mercadolibre.flow.control.tool.feature.entity;

import java.util.Locale;

public enum Workflow {

  FBM_WMS_OUTBOUND;

  public static Workflow from(final String value) {
    return valueOf(value.toUpperCase(Locale.getDefault()));
  }

  public String getName() {
    return name().toLowerCase(Locale.getDefault());
  }
}
