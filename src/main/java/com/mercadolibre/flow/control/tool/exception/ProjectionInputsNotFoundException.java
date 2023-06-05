package com.mercadolibre.flow.control.tool.exception;

public class ProjectionInputsNotFoundException extends RuntimeException {

  static final long serialVersionUID = 2L;

  private final String logisticCenterId;

  private final String workflow;

  private final String inputType;

  public ProjectionInputsNotFoundException(
      final String inputType,
      final String logisticCenterId,
      final String workflow,
      final Throwable cause
  ) {
    super(cause);
    this.inputType = inputType;
    this.logisticCenterId = logisticCenterId;
    this.workflow = workflow;

  }

  @Override
  public String getMessage() {
    return String.format(
        "Input type: [%s] cannot be calculated for logistic center [%s] and workflow [%s]",
        inputType,
        logisticCenterId,
        workflow
    );
  }
}
