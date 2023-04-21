package com.mercadolibre.flow.control.tool.feature.entity;

import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public enum ProcessPath {
  TOT_MULTI_BATCH,
  NON_TOT_MULTI_BATCH,
  TOT_MONO,
  NON_TOT_MONO,
  TOT_MULTI_ORDER,
  NON_TOT_MULTI_ORDER,
  TOT_SINGLE_SKU,
  GLOBAL;

  private static final Map<String, ProcessPath> LOOKUP = Arrays.stream(values()).collect(toMap(ProcessPath::toString, Function.identity()));

  public static ProcessPath from(final String value) {
    return valueOf(value.toUpperCase(Locale.getDefault()));
  }

  public static Optional<ProcessPath> of(final String value) {
    return Optional.ofNullable(LOOKUP.get(value.toUpperCase(Locale.US).replace('-', '_')));
  }

  public static List<ProcessPath> multiBatchPaths() {
    return List.of(TOT_MULTI_BATCH, NON_TOT_MULTI_BATCH);
  }

  public static List<ProcessPath> pathsMinusMultiBatch() {
    return List.of(TOT_MONO, NON_TOT_MONO, TOT_MULTI_ORDER, NON_TOT_MULTI_ORDER, TOT_SINGLE_SKU, GLOBAL);
  }

  public static List<ProcessPath> allPaths() {
    return Stream.concat(multiBatchPaths().stream(), pathsMinusMultiBatch().stream()).toList();
  }

  public String getName() {
    return name().toLowerCase(Locale.getDefault());
  }
}
