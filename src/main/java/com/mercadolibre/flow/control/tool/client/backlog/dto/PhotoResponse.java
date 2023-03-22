package com.mercadolibre.flow.control.tool.client.backlog.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * DTO class for BacklogApi photos/last response.
 *
 * @param takenOn it corresponds to the Instant when the photo was taken.
 * @param groups  list [key, values] per photo and BacklogPhoto Grouper
 */
public record PhotoResponse(
    @JsonProperty("taken_on")
    Instant takenOn,
    List<Group> groups
) {

  /**
   * Group with key values per photo.
   *
   * @param key   maps [key, value] where key is a BacklogPhoto Grouper.
   *              If grouper(key) is STEP, the value will correspond to one of BacklogPhotoSteps.
   *              else if AREA, the value will be the name of the AREA, and it varies 4 each Warehouse.
   * @param total units for the given group.
   */
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Group(
      Map<String, String> key,
      int total
  ) {
  }
}
