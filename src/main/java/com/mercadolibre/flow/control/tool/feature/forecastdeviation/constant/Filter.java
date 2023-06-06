package com.mercadolibre.flow.control.tool.feature.forecastdeviation.constant;

import static java.time.temporal.ChronoUnit.HOURS;
import static java.util.stream.Collectors.toMap;

import java.time.Instant;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Filter {
  DATE_IN(Filter::buildDateInFilter),
  DATE_OUT(Filter::buildDateOutFilter);

  private static final int DAYS_TO_SEARCH = 7;

  public final BiFunction<Instant, Instant, DateFilter> dateFilterFunction;

  private static final Map<String, Filter> LOOKUP = Arrays.stream(values()).collect(
      toMap(Filter::toString, Function.identity())
  );

  public static Optional<Filter> from(final String value) {
    return Optional.ofNullable(LOOKUP.get(value.toUpperCase(Locale.US)));
  }

  public String getName() {
    return name().toLowerCase(Locale.getDefault());
  }

  private static DateFilter buildDateInFilter(final Instant dateFrom, final Instant dateTo) {
    return new DateFilter(dateFrom, dateTo, dateFrom, dateTo.plus(DAYS_TO_SEARCH, HOURS));
  }

  private static DateFilter buildDateOutFilter(final Instant dateFrom, final Instant dateTo) {
    return new DateFilter(dateFrom.minus(DAYS_TO_SEARCH, HOURS), dateTo, dateFrom, dateTo);
  }

  public record DateFilter(Instant dateInFrom, Instant dateInTo, Instant dateOutFrom, Instant dateOutTo) {
  }
}
