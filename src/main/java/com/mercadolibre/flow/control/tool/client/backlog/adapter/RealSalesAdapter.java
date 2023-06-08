package com.mercadolibre.flow.control.tool.client.backlog.adapter;

import com.mercadolibre.fbm.wms.outbound.commons.rest.exception.ClientException;
import com.mercadolibre.flow.control.tool.client.backlog.BacklogApiClient;
import com.mercadolibre.flow.control.tool.client.backlog.dto.LastPhotoRequest;
import com.mercadolibre.flow.control.tool.client.backlog.dto.PhotoResponse;
import com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoGrouper;
import com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoWorkflow;
import com.mercadolibre.flow.control.tool.exception.RealSalesException;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import com.mercadolibre.flow.control.tool.feature.forecastdeviation.constant.Filter;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Adapter for Backlogs API.  This class is used to get the real sales.
 */
@Component
@RequiredArgsConstructor
public class RealSalesAdapter {

  private final BacklogApiClient backlogApiClient;

  /**
   * Retrieve the real sales grouped by date_in or date_out.
   *
   * @param logisticCenterId The ID of the logistic center.
   * @param workflow         The workflow for which to retrieve the sales distribution.
   * @param filter           The grouping value date_in/date_out
   * @param dateInFrom       The start date of the time period to get sales.
   * @param dateInTo         The end date of the time period to get sales.
   * @param dateOutFrom      The start date of the time period to get CPTs
   * @param dateOutTo        The end date of the time period to get CPTs
   * @param dateTo           The date to get photo
   * @return A map of instant and total value.
   * @throws RealSalesException Catch clientExceptions
   */

  public Map<Instant, Integer> getRealSales(
      final String logisticCenterId,
      final Workflow workflow,
      final Filter filter,
      final Instant dateInFrom,
      final Instant dateInTo,
      final Instant dateOutFrom,
      final Instant dateOutTo,
      final Instant dateTo
  ) {

    final PhotoGrouper groupBy = PhotoGrouper.from(filter.getName());

    final LastPhotoRequest request = LastPhotoRequest.builder()
        .logisticCenterId(logisticCenterId)
        .workflows(Set.of(PhotoWorkflow.from(workflow)))
        .groupBy(Set.of(groupBy))
        .photoDateTo(dateTo)
        .dateInFrom(dateInFrom)
        .dateInTo(dateInTo)
        .dateOutFrom(dateOutFrom)
        .dateOutTo(dateOutTo)
        .build();

    try {

      final PhotoResponse lastPhoto = backlogApiClient.getLastPhoto(request);

      if (lastPhoto == null) {
        return Collections.emptyMap();
      }

      return lastPhoto.groups().stream()
          .collect(
              Collectors.toMap(
                  group -> Instant.parse(group.key().get(groupBy.getName())),
                  PhotoResponse.Group::total,
                  Integer::sum
              ));

    } catch (ClientException ce) {
      throw new RealSalesException(logisticCenterId, ce, ce.getResponseStatus());
    }
  }

}
