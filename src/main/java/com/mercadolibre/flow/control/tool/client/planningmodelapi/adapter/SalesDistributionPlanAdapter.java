package com.mercadolibre.flow.control.tool.client.planningmodelapi.adapter;

import static com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.PlannedGrouper.DATE_IN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.mercadolibre.fbm.wms.outbound.commons.rest.exception.ClientException;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.PlanningModelApiClient;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.PlannedGrouper;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.PlanningWorkflow;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.BacklogPlannedRequest;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.BacklogPlannedResponse;
import com.mercadolibre.flow.control.tool.exception.ForecastNotFoundException;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import com.mercadolibre.flow.control.tool.feature.forecastdeviation.constant.Filter;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;


/**
 * Adapter for Planning Model API Client.  This class is used to get the sales distribution plan.
 */
@AllArgsConstructor
@Component
public class SalesDistributionPlanAdapter {

  private final PlanningModelApiClient planningModelApiClient;

  /**
   * Retrieve the sales distribution plan grouped by date_in or date_out.
   *
   * @param logisticCenterId The ID of the logistic center.
   * @param workflow         The workflow for which to retrieve the sales distribution.
   * @param filter          The grouping value date_in/date_out
   * @param dateInFrom       The start date of the time period to get sales.
   * @param dateInTo         The end date of the time period to get sales.
   * @param dateOutFrom      The start date of the time period to get CPTs
   * @param dateOutTo        The end date of the time period to get CPTs
   * @return A map of instant and total value.
   */
  public Map<Instant, Double> getSalesDistributionPlanned(
      final String logisticCenterId,
      final Workflow workflow,
      final Filter filter,
      final Instant dateInFrom,
      final Instant dateInTo,
      final Instant dateOutFrom,
      final Instant dateOutTo
  ) {

    final PlannedGrouper groupBy = PlannedGrouper.from(filter.getName());

    final BacklogPlannedRequest request = BacklogPlannedRequest.builder()
        .logisticCenter(logisticCenterId)
        .planningWorkflow(PlanningWorkflow.from(workflow.getName()))
        .groupBy(Set.of(groupBy))
        .dateInFrom(dateInFrom)
        .dateInTo(dateInTo)
        .dateOutFrom(dateOutFrom)
        .dateOutTo(dateOutTo)
        .build();

    try {

      final List<BacklogPlannedResponse> plannedSales = planningModelApiClient.getBacklogPlanned(request);

      return plannedSales.stream()
          .collect(
              Collectors.groupingBy(
                  grouper -> DATE_IN == groupBy ? grouper.group().dateIn() : grouper.group().dateOut(),
                  Collectors.summingDouble(BacklogPlannedResponse::total)
              )
          );

    } catch (ClientException ce) {
      throw forecastNotFound(ce)
          ? new ForecastNotFoundException(logisticCenterId, workflow.getName(), ce)
          : ce;
    }
  }

  private boolean forecastNotFound(ClientException ex) {
    return ex.getResponseStatus() == NOT_FOUND.value() && ex.getResponseBody().contains("forecast_not_found");
  }
}
