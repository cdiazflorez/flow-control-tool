package com.mercadolibre.flow.control.tool.exception;

import java.io.Serial;
import java.time.Instant;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class InvalidateDateDurationRangeException extends RuntimeException {
  @Serial
  private static final long serialVersionUID = 2L;

  private final Instant dateFrom;

  private final Instant dateTo;

  private final Long range;

  @Override
  public String getMessage() {
    return String.format(
        "Range of DateFrom [%s] and dateTo [%s] = [%s] hs not support",
        dateFrom,
        dateTo,
        range
    );
  }
}
