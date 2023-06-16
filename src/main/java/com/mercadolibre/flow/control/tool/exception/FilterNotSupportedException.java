package com.mercadolibre.flow.control.tool.exception;

public class FilterNotSupportedException extends RuntimeException {

  static final long serialVersionUID = 1L;

  private static final String MESSAGE_PATTERN = "Filter: %s not supported";

  private final String filter;

  public FilterNotSupportedException(String filter) {
    this.filter = filter;
  }

  @Override
  public String getMessage() {
    return String.format(MESSAGE_PATTERN, filter);
  }

}
