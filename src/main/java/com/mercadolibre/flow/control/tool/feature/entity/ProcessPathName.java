package com.mercadolibre.flow.control.tool.feature.entity;

import static java.util.stream.Collectors.toMap;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public enum ProcessPathName {
  TOT_MONO,
  NON_TOT_MONO,
  TOT_MULTI_BATCH,
  NON_TOT_MULTI_BATCH,
  TOT_MULTI_ORDER,
  NON_TOT_MULTI_ORDER,
  TOT_SINGLE_SKU,
  GLOBAL;

  private static final Map<String, ProcessPathName> LOOKUP = Arrays.stream(values()).collect(
      toMap(ProcessPathName::toString, Function.identity())
  );

  public static ProcessPathName from(final String value) {
    return valueOf(value.toUpperCase(Locale.getDefault()));
  }

  public static Optional<ProcessPathName> of(final String value) {
    return Optional.ofNullable(LOOKUP.get(value.toUpperCase(Locale.US).replace('-', '_')));
  }

  public static List<ProcessPathName> multiBatchPaths() {
    return List.of(TOT_MULTI_BATCH, NON_TOT_MULTI_BATCH);
  }

  public static List<ProcessPathName> pathsMinusMultiBatch() {
    return List.of(TOT_MONO, NON_TOT_MONO, TOT_MULTI_ORDER, NON_TOT_MULTI_ORDER, TOT_SINGLE_SKU, GLOBAL);
  }

  public static List<ProcessPathName> allPaths() {
    return Stream.concat(multiBatchPaths().stream(), pathsMinusMultiBatch().stream()).toList();
  }

  @JsonValue
  public String getName() {
    return name().toLowerCase(Locale.getDefault());
  }

}
