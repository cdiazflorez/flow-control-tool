package com.mercadolibre.flow.control.tool.feature.forecastdeviation.constant;

import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public enum Filter {
  DATE_IN,
  DATE_OUT;


  private static final Map<String, Filter> LOOKUP = Arrays.stream(values()).collect(
      toMap(Filter::toString, Function.identity())
  );

  public static Optional<Filter> from(final String value) {
    return Optional.ofNullable(LOOKUP.get(value.toUpperCase(Locale.US)));
  }

  public String getName() {
    return name().toLowerCase(Locale.getDefault());
  }
}
