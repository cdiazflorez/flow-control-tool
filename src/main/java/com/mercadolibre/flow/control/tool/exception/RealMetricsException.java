package com.mercadolibre.flow.control.tool.exception;

public class RealMetricsException extends RuntimeException {

  static final long serialVersionUID = 2L;

  private final String logisticCenterId;

  private final String workflow;

  private final Integer status;

  private final String message;

  public RealMetricsException(final String logisticCenterId,
                              final String workflow,
                              final Throwable cause,
                              final Integer status) {
    super(cause);

    this.logisticCenterId = logisticCenterId;
    this.workflow = workflow;
    this.status = status;
    this.message = cause.getMessage();
  }

  @Override
  public String getMessage() {
    return String.format(
        "[%s] - [%s]: [%s]",
        workflow,
        logisticCenterId,
        message
    );
  }

  public Integer getStatus() {
    return status;
  }
}
