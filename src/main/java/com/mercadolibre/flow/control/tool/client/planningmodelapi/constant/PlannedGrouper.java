package com.mercadolibre.flow.control.tool.client.planningmodelapi.constant;

import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public enum PlannedGrouper {

  DATE_IN,
  DATE_OUT,
  PROCESS_PATH;

  private static final Map<String, PlannedGrouper> LOOKUP = Arrays.stream(values()).collect(
      toMap(PlannedGrouper::toString, Function.identity())
  );

  public static PlannedGrouper from(final String value) {
    return LOOKUP.get(value.toUpperCase(Locale.US));
  }

  public String getName() {
    return name().toLowerCase(Locale.getDefault());
  }
}
