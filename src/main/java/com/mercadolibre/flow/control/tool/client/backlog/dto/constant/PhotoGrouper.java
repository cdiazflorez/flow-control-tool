package com.mercadolibre.flow.control.tool.client.backlog.dto.constant;

import java.util.Locale;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Possible values for "group_by" param for BacklogApi photos/.
 */
@Getter
@RequiredArgsConstructor
public enum PhotoGrouper {

  AREA,
  STEP,
  PATH,
  DATE_OUT;

  public static PhotoGrouper from(final String value) {
    return valueOf(value.toUpperCase(Locale.getDefault()));
  }

  public String getName() {
    return name().toLowerCase(Locale.getDefault());
  }
}
