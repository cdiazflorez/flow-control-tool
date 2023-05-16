package com.mercadolibre.flow.control.tool.exception;

public class ForecastNotFoundException extends RuntimeException {

  static final long serialVersionUID = 2L;

  private final String logisticCenterId;

  private final String workflow;

  public ForecastNotFoundException(final String logisticCenterId,
                                   final String workflow) {
    this.logisticCenterId = logisticCenterId;
    this.workflow = workflow;
  }

  public ForecastNotFoundException(final String logisticCenterId,
                                   final String workflow,
                                   final Throwable cause) {
    super(cause);
    this.logisticCenterId = logisticCenterId;
    this.workflow = workflow;
  }

  @Override
  public String getMessage() {
    return String.format(
        "Forecast not found for workflow [%s] and logistic center [%s]",
        workflow,
        logisticCenterId
    );
  }
}
