package com.mercadolibre.flow.control.tool.feature.entity;

import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public enum ProcessName {

  WAVING,
  PICKING,
  BATCH_SORTER,
  WALL_IN,
  PACKING,
  PACKING_WALL,
  HU_ASSEMBLY,
  SHIPPED;

  private static final Map<String, ProcessName> LOOKUP = Arrays.stream(values()).collect(
      toMap(ProcessName::toString, Function.identity())
  );

  public static Optional<ProcessName> from(final String value) {
    return Optional.ofNullable(LOOKUP.get(value.toUpperCase(Locale.US)));
  }
  public String getName() {
    return name().toLowerCase(Locale.getDefault());
  }
}
