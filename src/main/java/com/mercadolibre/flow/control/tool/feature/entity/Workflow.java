package com.mercadolibre.flow.control.tool.feature.entity;

import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public enum Workflow {

  FBM_WMS_OUTBOUND;

  private static final Map<String, Workflow> LOOKUP = Arrays.stream(values()).collect(
      toMap(Workflow::toString, Function.identity())
  );

  public static Optional<Workflow> from(final String value) {
    return Optional.ofNullable(LOOKUP.get(value.toUpperCase(Locale.US)));
  }

  public String getName() {
    return name().toLowerCase(Locale.getDefault());
  }
}
