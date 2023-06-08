package com.mercadolibre.flow.control.tool.client.backlog.dto.constant;

import java.util.Locale;

/**
 * Needed query params used into GET request for BacklogApi photos/.
 */
public enum PhotoQueryParam {

  LOGISTIC_CENTER_ID,
  WORKFLOWS,
  GROUP_BY,
  PHOTO_DATE_TO,
  STEPS,
  DATE_FROM,
  DATE_TO,
  DATE_IN_FROM,
  DATE_IN_TO,
  DATE_OUT_FROM,
  DATE_OUT_TO;

  public static PhotoQueryParam from(final String value) {
    return valueOf(value.toUpperCase(Locale.getDefault()));
  }

  public String getName() {
    return name().toLowerCase(Locale.getDefault());
  }
}
