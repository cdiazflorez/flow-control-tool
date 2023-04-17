package com.mercadolibre.flow.control.tool.exception;

import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import java.util.Arrays;

public class ProcessNotSupportedException extends RuntimeException {
  static final long serialVersionUID = 1L;

  private static final String MESSAGE_PATTERN = "Value %s is invalid, instead it should be one of %s";

  private final String process;

  public ProcessNotSupportedException(String process) {
    this.process = process;
  }

  @Override
  public String getMessage() {
    return String.format(MESSAGE_PATTERN, process, Arrays.toString(ProcessName.values()));
  }
}
