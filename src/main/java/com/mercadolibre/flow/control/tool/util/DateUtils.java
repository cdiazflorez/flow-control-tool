package com.mercadolibre.flow.control.tool.util;

import java.time.Duration;
import java.time.Instant;

public final class DateUtils {

  private DateUtils() {
  }

  public static boolean validDates(final Instant dateFrom, final Instant dateTo) {
    return dateFrom.isAfter(dateTo);
  }

  public static boolean isDifferenceBetweenDateBiggestThan(
      final Instant dateFrom,
      final Instant dateTo,
      final int hours) {

    final long differenceBetweenDates = Duration.between(dateFrom, dateTo).toHours();

    return differenceBetweenDates > hours;
  }
}
