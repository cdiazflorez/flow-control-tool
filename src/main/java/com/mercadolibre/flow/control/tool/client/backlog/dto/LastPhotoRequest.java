package com.mercadolibre.flow.control.tool.client.backlog.dto;

import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoQueryParam.GROUP_BY;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoQueryParam.LOGISTIC_CENTER_ID;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoQueryParam.PHOTO_DATE_TO;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoQueryParam.STEPS;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoQueryParam.WORKFLOWS;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;

import com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoGrouper;
import com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep;
import com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoWorkflow;
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
    Set<PhotoWorkflow> workflows,
    Set<PhotoGrouper> groupBy,
    Set<PhotoStep> steps,
    Instant photoDateTo
) {

  /**
   * Get needed query params for photos/last GET request.
   */
  public Map<String, String> getQueryParams() {
    final Map<String, String> queryParams = new ConcurrentHashMap<>();
    queryParams.put(LOGISTIC_CENTER_ID.getName(), logisticCenterId);
    addAsQueryParam(queryParams, WORKFLOWS.getName(), workflows.stream().map(PhotoWorkflow::getAlias).toList());
    addAsQueryParam(queryParams, GROUP_BY.getName(), groupBy.stream().map(PhotoGrouper::getName).toList());
    addAsQueryParam(queryParams, STEPS.getName(), steps.stream().map(PhotoStep::getName).toList());
    addAsQueryParam(queryParams, PHOTO_DATE_TO.getName(), photoDateTo);
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
