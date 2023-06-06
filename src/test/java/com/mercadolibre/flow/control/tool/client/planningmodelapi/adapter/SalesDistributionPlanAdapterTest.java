package com.mercadolibre.flow.control.tool.client.planningmodelapi.adapter;

import static com.mercadolibre.flow.control.tool.util.TestUtils.objectMapper;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mercadolibre.fbm.wms.outbound.commons.rest.HttpRequest;
import com.mercadolibre.fbm.wms.outbound.commons.rest.exception.ClientException;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.PlanningModelApiClient;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.PlannedGrouper;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.BacklogPlannedRequest;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.BacklogPlannedResponse;
import com.mercadolibre.flow.control.tool.exception.ForecastNotFoundException;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import com.mercadolibre.restclient.Response;
import com.mercadolibre.restclient.http.Headers;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SalesDistributionPlanAdapterTest {

  private static final String LOGISTIC_CENTER_ID = "ARBA01";

  private static final Instant DATE_FROM = Instant.parse("2023-05-18T08:00:00Z");

  private static final Instant DATE_TO = Instant.parse("2023-05-18T10:00:00Z");

  @InjectMocks
  private SalesDistributionPlanAdapter salesDistributionPlanAdapter;

  @Mock
  private PlanningModelApiClient planningModelApiClient;

  @Captor
  private ArgumentCaptor<BacklogPlannedRequest> backlogPlannedRequestArgumentCaptor;

  private static Stream<Arguments> grouperArgumentsTest() {
    return Stream.of(
        Arguments.of(PlannedGrouper.DATE_IN),
        Arguments.of(PlannedGrouper.DATE_OUT)
    );
  }

  private static Stream<Arguments> exceptionsArgumentsTest() throws JsonProcessingException {
    return Stream.of(
        Arguments.of(
            ForecastNotFoundException.class,
            new ClientException(
                "PLANNING_MODEL_API",
                HttpRequest.builder()
                    .url("URL")
                    .build(),
                new Response(404, new Headers(Map.of()), objectMapper().writeValueAsBytes("forecast_not_found"))
            )
        ),
        Arguments.of(
            ClientException.class,
            new ClientException(
                "PLANNING_MODEL_API",
                HttpRequest.builder()
                    .url("URL")
                    .build(),
                new Throwable("Error")
            )
        )
    );
  }

  @ParameterizedTest
  @MethodSource("grouperArgumentsTest")
  @DisplayName("Test get sales distribution.")
  void salesDistributionPlannedTest(final PlannedGrouper groupBy) {
    //GIVE
    final Instant expectedDateInFrom = PlannedGrouper.DATE_IN.equals(groupBy)
        ? DATE_FROM
        : DATE_FROM.minus(7, ChronoUnit.DAYS);

    final Optional<Instant> expectedDateOutTo = PlannedGrouper.DATE_IN.equals(groupBy)
        ? Optional.of(DATE_TO.plus(7, ChronoUnit.DAYS))
        : Optional.of(DATE_TO);

    final List<BacklogPlannedResponse> response = mockPlannedSales(groupBy, expectedDateInFrom);


    //WHEN
    when(planningModelApiClient.getBacklogPlanned(any(BacklogPlannedRequest.class))).thenReturn(response);

    //THEN
    final Map<Instant, Double> result = salesDistributionPlanAdapter.getSalesDistributionPlanned(
        Workflow.FBM_WMS_OUTBOUND, LOGISTIC_CENTER_ID, DATE_FROM, DATE_TO, groupBy
    );

    verify(planningModelApiClient).getBacklogPlanned(backlogPlannedRequestArgumentCaptor.capture());
    final BacklogPlannedRequest inputs = backlogPlannedRequestArgumentCaptor.getValue();

    Assertions.assertEquals(3, result.size());
    Assertions.assertTrue(result.containsKey(expectedDateInFrom));
    Assertions.assertTrue(result.containsKey(expectedDateInFrom.plus(1, ChronoUnit.HOURS)));
    Assertions.assertTrue(result.containsKey(expectedDateInFrom.plus(2, ChronoUnit.HOURS)));
    Assertions.assertEquals(PlannedGrouper.DATE_IN.equals(groupBy) ? 10 : 12, result.get(expectedDateInFrom));

    Assertions.assertEquals(expectedDateInFrom, inputs.dateInFrom());
    Assertions.assertEquals(expectedDateOutTo, inputs.dateOutTo());

  }

  @ParameterizedTest
  @MethodSource("exceptionsArgumentsTest")
  @DisplayName("Test sales distribution with exceptions.")
  void salesDistributionPlanErrorTest(final Class<? extends Exception> exceptionClass, final ClientException exception) {

    //WHEN
    when(planningModelApiClient.getBacklogPlanned(any(BacklogPlannedRequest.class))).thenThrow(exception);


    Assertions.assertThrows(
        exceptionClass,
        () -> salesDistributionPlanAdapter
            .getSalesDistributionPlanned(Workflow.FBM_WMS_OUTBOUND, LOGISTIC_CENTER_ID, DATE_FROM, DATE_TO, PlannedGrouper.DATE_IN)
    );


  }

  private List<BacklogPlannedResponse> mockPlannedSales(final PlannedGrouper groupBy, final Instant date) {

    return PlannedGrouper.DATE_IN.equals(groupBy)
        ? List.of(
        new BacklogPlannedResponse(new BacklogPlannedResponse.GroupKey(null, date, null), 10),
        new BacklogPlannedResponse(new BacklogPlannedResponse.GroupKey(null, date.plus(1, ChronoUnit.HOURS), null), 10),
        new BacklogPlannedResponse(new BacklogPlannedResponse.GroupKey(null, date.plus(2, ChronoUnit.HOURS), null), 10)
    )
        : List.of(
        new BacklogPlannedResponse(new BacklogPlannedResponse.GroupKey(null, null, date), 12),
        new BacklogPlannedResponse(new BacklogPlannedResponse.GroupKey(null, null, date.plus(1, ChronoUnit.HOURS)), 12),
        new BacklogPlannedResponse(new BacklogPlannedResponse.GroupKey(null, null, date.plus(2, ChronoUnit.HOURS)), 12)
    );
  }

}
