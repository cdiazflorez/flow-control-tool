package com.mercadolibre.flow.control.tool.exception;

public class NoUnitsPerOrderRatioFound extends RuntimeException {
  static final long serialVersionUID = 1L;

  private final String logisticCenterId;

  public NoUnitsPerOrderRatioFound(String logisticCenterId) {
    this.logisticCenterId = logisticCenterId;
  }

  @Override
  public String getMessage() {
    return String.format(
        "The %s input obtained from the UnitsPerOrderRatio is invalid: %s",
        logisticCenterId,
        super.getMessage()
    );
  }
}
