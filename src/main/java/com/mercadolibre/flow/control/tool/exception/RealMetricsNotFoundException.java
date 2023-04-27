package com.mercadolibre.flow.control.tool.exception;

public class RealMetricsNotFoundException extends RuntimeException {

  static final long serialVersionUID = 2L;

  private final String logisticCenterId;

  private final String workflow;

  public RealMetricsNotFoundException(final String logisticCenterId,
                                      final String workflow,
                                      final Throwable cause) {
    super(cause);
    this.logisticCenterId = logisticCenterId;
    this.workflow = workflow;
  }

  @Override
  public String getMessage() {
    return String.format(
        "Real metrics not found for workflow [%s] and logistic center [%s]",
        workflow,
        logisticCenterId
    );
  }
}
