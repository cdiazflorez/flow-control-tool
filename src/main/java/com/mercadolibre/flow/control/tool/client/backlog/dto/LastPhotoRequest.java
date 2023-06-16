package com.mercadolibre.flow.control.tool.client.backlog.dto;

import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoQueryParam.DATE_IN_FROM;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoQueryParam.DATE_IN_TO;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoQueryParam.DATE_OUT_FROM;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoQueryParam.DATE_OUT_TO;
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
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LastPhotoRequest {
  String logisticCenterId;
  Set<PhotoWorkflow> workflows;
  Set<PhotoGrouper> groupBy;
  Set<PhotoStep> steps;
  Instant photoDateTo;
  Instant dateInFrom;
  Instant dateInTo;
  Instant dateOutFrom;
  Instant dateOutTo;

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

    if (dateInFrom != null && dateInTo != null) {
      queryParams.put(DATE_IN_FROM.getName(), ISO_INSTANT.format(dateInFrom));
      queryParams.put(DATE_IN_TO.getName(), ISO_INSTANT.format(dateInTo));
    }

    if (dateOutFrom != null && dateOutTo != null) {
      queryParams.put(DATE_OUT_FROM.getName(), ISO_INSTANT.format(dateOutFrom));
      queryParams.put(DATE_OUT_TO.getName(), ISO_INSTANT.format(dateOutTo));
    }

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
    if (value != null && !value.isEmpty()) {
      map.put(key, String.join(",", value));
    }
  }
}
