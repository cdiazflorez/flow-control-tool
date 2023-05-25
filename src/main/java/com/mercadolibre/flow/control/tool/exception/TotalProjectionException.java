package com.mercadolibre.flow.control.tool.exception;

public class TotalProjectionException extends RuntimeException {
  static final long serialVersionUID = 1L;

  private final String logisticCenterId;

  private final Integer status;

  private final String message;


  public TotalProjectionException(final String logisticCenterId,
                                  final Throwable cause,
                                  final Integer status) {
    super(cause);

    this.logisticCenterId = logisticCenterId;
    this.status = status;
    this.message = cause.getMessage();
  }

  @Override
  public String getMessage() {
    return String.format(
        "[%s]: [%s]",
        logisticCenterId,
        message
    );
  }

  public Integer getStatus() {
    return status;
  }
}
