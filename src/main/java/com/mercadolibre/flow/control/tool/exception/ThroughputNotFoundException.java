package com.mercadolibre.flow.control.tool.exception;

public class ThroughputNotFoundException extends RuntimeException {

  static final long serialVersionUID = 141L;

  public ThroughputNotFoundException(final String message) {
    super(message);
  }
}
