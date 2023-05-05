package com.mercadolibre.flow.control.tool.exception;

import java.io.Serial;
import java.time.Instant;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class InvalidDateRangeException extends RuntimeException {
  @Serial
  private static final long serialVersionUID = 2L;

  private final Instant dateFrom;

  private final Instant dateTo;

  @Override
  public String getMessage() {
    return String.format(
        "DateFrom [%s] is after dateTo [%s]",
        dateFrom,
        dateTo
    );
  }
}
