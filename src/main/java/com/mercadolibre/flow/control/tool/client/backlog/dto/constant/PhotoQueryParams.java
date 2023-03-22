package com.mercadolibre.flow.control.tool.client.backlog.dto.constant;

import java.util.Locale;

/**
 * Needed query params used into GET request for BacklogApi photos/.
 */
public enum PhotoQueryParams {

  LOGISTIC_CENTER_ID,
  WORKFLOWS,
  GROUP_BY,
  PHOTO_DATE_TO,
  STEPS;

  public static PhotoQueryParams from(final String value) {
    return valueOf(value.toUpperCase(Locale.getDefault()));
  }

  public String getName() {
    return name().toLowerCase(Locale.getDefault());
  }
}
