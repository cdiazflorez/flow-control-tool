package com.mercadolibre.flow.control.tool.client.planningmodelapi.adapter;

import static com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.PlannedGrouper.DATE_IN;
import static com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.PlannedGrouper.DATE_OUT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.mercadolibre.fbm.wms.outbound.commons.rest.exception.ClientException;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.PlanningModelApiClient;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.PlannedGrouper;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.PlanningWorkflow;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.BacklogPlannedRequest;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.BacklogPlannedResponse;
import com.mercadolibre.flow.control.tool.exception.ForecastNotFoundException;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

  private static final int DAYS = 7;

  private final PlanningModelApiClient planningModelApiClient;

  /**
   * Retrieve the sales distribution plan grouped by date_in or date_out.
   *
   * @param workflow         The workflow for which to retrieve the sales distribution.
   * @param logisticCenterId The ID of the logistic center.
   * @param dateFrom         The start date of the time period.
   * @param dateTo           The end date of the time period.
   * @param groupBy          The grouping value date_in/date_out
   * @return A map of instant and total value.
   */
  public Map<Instant, Double> getSalesDistributionPlanned(
      final Workflow workflow,
      final String logisticCenterId,
      final Instant dateFrom,
      final Instant dateTo,
      final PlannedGrouper groupBy
  ) {

    final Instant dateInFrom = DATE_OUT == groupBy ? dateFrom.minus(DAYS, ChronoUnit.DAYS) : dateFrom;
    final Instant dateOutTo = DATE_IN == groupBy ? dateTo.plus(DAYS, ChronoUnit.DAYS) : dateTo;

    final BacklogPlannedRequest request = new BacklogPlannedRequest(
        logisticCenterId,
        PlanningWorkflow.from(workflow.getName()),
        Set.of(),
        dateInFrom,
        dateTo,
        Optional.of(dateFrom),
        Optional.of(dateOutTo),
        Set.of(groupBy)
    );

    try {

      final List<BacklogPlannedResponse> plannedSales = planningModelApiClient.getBacklogPlanned(request);

      return plannedSales.stream()
          .collect(
              Collectors.groupingBy(
                  grouper -> DATE_IN.equals(groupBy) ? grouper.group().dateIn() : grouper.group().dateOut(),
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
