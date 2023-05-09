package com.mercadolibre.flow.control.tool.util;

import com.mercadolibre.flow.control.tool.exception.InvalidDateRangeException;
import java.time.Duration;
import java.time.Instant;

public final class DateUtils {

  private DateUtils() {
  }

  public static boolean isDifferenceBetweenDateBiggestThan(
      final Instant dateFrom,
      final Instant dateTo,
      final int hours) {

    final long differenceBetweenDates = Duration.between(dateFrom, dateTo).toHours();

    return differenceBetweenDates > hours;
  }

  public static void validateDateRange(final Instant dateFrom, final Instant dateTo) {
    if (dateFrom.isAfter(dateTo)) {
      throw new InvalidDateRangeException(dateFrom, dateTo);
    }
  }
}
