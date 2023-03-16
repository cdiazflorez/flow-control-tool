package com.mercadolibre.flow.control.tool.client.backlog.dto.constant;

import java.util.Locale;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Possible values for steps param in BacklogApi photos/.
 */
@Getter
@RequiredArgsConstructor
public enum PhotoSteps {

  PENDING,
  TO_PICK,
  PICKING,
  PICKED,
  TO_GROUP,
  GROUPING,
  GROUPED,
  TO_SORT,
  SORTED,
  TO_PACK,
  PACKING,
  PACKING_WALL,
  PACKED,
  TO_DISPATCH,
  TO_OUT,
  TO_ROUTE;

  public static PhotoSteps from(final String value) {
    return valueOf(value.toUpperCase(Locale.getDefault()));
  }

  public String getName() {
    return name().toLowerCase(Locale.getDefault());
  }
}
