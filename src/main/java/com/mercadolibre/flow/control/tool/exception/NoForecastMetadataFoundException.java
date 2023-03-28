package com.mercadolibre.flow.control.tool.exception;

public class NoForecastMetadataFoundException extends RuntimeException {
  static final long serialVersionUID = 1L;

  private final String logisticCenterId;

  public NoForecastMetadataFoundException(String logisticCenterId) {
    this.logisticCenterId = logisticCenterId;
  }

  public NoForecastMetadataFoundException(String logisticCenterId, Throwable cause) {
    super(cause);
    this.logisticCenterId = logisticCenterId;
  }

  @Override
  public String getMessage() {
    return String.format(
        "The %s input obtained from the ForecastMetadata is invalid: %s",
        logisticCenterId,
        super.getMessage()
    );
  }
}
