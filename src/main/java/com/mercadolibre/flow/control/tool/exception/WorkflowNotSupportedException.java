package com.mercadolibre.flow.control.tool.exception;

public class WorkflowNotSupportedException extends RuntimeException {
  static final long serialVersionUID = 1L;

  private static final String MESSAGE_PATTERN = "Workflow: %s not supported";

  private final String workflow;

  public WorkflowNotSupportedException(String workflow) {
    this.workflow = workflow;
  }

  @Override
  public String getMessage() {
    return String.format(MESSAGE_PATTERN, workflow);
  }
}
