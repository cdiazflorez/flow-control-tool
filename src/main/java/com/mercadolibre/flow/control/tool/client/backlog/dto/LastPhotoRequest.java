package com.mercadolibre.flow.control.tool.client.backlog.dto;

import static com.mercadolibre.flow.control.tool.client.backlog.dto.BacklogPhotoConstants.BacklogPhotoQueryParams.GROUP_BY;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.BacklogPhotoConstants.BacklogPhotoQueryParams.LOGISTIC_CENTER_ID;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.BacklogPhotoConstants.BacklogPhotoQueryParams.PHOTO_DATE_TO;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.BacklogPhotoConstants.BacklogPhotoQueryParams.STEPS;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.BacklogPhotoConstants.BacklogPhotoQueryParams.WORKFLOWS;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;

import com.mercadolibre.flow.control.tool.client.backlog.dto.BacklogPhotoConstants.BacklogPhotoGrouper;
import com.mercadolibre.flow.control.tool.client.backlog.dto.BacklogPhotoConstants.BacklogPhotoSteps;
import com.mercadolibre.flow.control.tool.client.backlog.dto.BacklogPhotoConstants.BacklogPhotoWorkflows;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DTO Class for Backlog photos/last request. Based on given filters/params.
 *
 * @param logisticCenterId warehouse
 * @param workflows        list of BacklogPhotoWorkflows
 * @param groupBy          list of BacklogPhotoGrouper
 * @param steps            list of BacklogPhotoSteps
 * @param photoDateTo      Instant date
 */
public record LastPhotoRequest(
    String logisticCenterId,
    Set<BacklogPhotoWorkflows> workflows,
    Set<BacklogPhotoGrouper> groupBy,
    Set<BacklogPhotoSteps> steps,
    Instant photoDateTo
) {

  /**
   * Get needed query params for photos/last GET request.
   */
  public Map<String, String> getQueryParams() {
    final Map<String, String> queryParams = new ConcurrentHashMap<>();
    queryParams.put(LOGISTIC_CENTER_ID.name(), logisticCenterId);
    addAsQueryParam(queryParams, WORKFLOWS.name(), workflows.stream().map(BacklogPhotoWorkflows::getBacklogPhotoWorkflow).toList());
    addAsQueryParam(queryParams, GROUP_BY.name(), groupBy.stream().map(BacklogPhotoGrouper::name).toList());
    addAsQueryParam(queryParams, STEPS.name(), steps.stream().map(BacklogPhotoSteps::name).toList());
    addAsQueryParam(queryParams, PHOTO_DATE_TO.name(), photoDateTo);
    return queryParams;
  }

  /**
   * Add to the given map the given key-value where value is an Instant.
   */
  private void addAsQueryParam(final Map<String, String> map, final String key, final Instant value) {
    if (value != null) {
      map.put(key, ISO_INSTANT.format(value));
    }
  }

  /**
   * Add to the given map the given key-value.
   */
  private void addAsQueryParam(final Map<String, String> map, final String key, final List<String> value) {
    if (value != null) {
      map.put(key, String.join(",", value));
    }
  }

}
