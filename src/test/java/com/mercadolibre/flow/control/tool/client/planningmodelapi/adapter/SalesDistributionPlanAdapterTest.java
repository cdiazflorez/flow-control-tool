package com.mercadolibre.flow.control.tool.client.planningmodelapi.adapter;

import static com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.PlannedGrouper.DATE_IN;
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
import com.mercadolibre.flow.control.tool.feature.forecastdeviation.constant.Filter;
import com.mercadolibre.restclient.Response;
import com.mercadolibre.restclient.http.Headers;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
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
        Arguments.of(Filter.DATE_IN, DATE_FROM, 3),
        Arguments.of(Filter.DATE_OUT, DATE_TO, 2)
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
  void salesDistributionPlannedTest(final Filter filter, final Instant expectedDate, final int expectedSize) {
    //GIVE
    final PlannedGrouper groupBy = PlannedGrouper.from(filter.getName());
    final List<BacklogPlannedResponse> response = mockPlannedSales(groupBy);

    //WHEN
    when(planningModelApiClient.getBacklogPlanned(any(BacklogPlannedRequest.class))).thenReturn(response);

    //THEN
    final Map<Instant, Double> result = salesDistributionPlanAdapter.getSalesDistributionPlanned(
        LOGISTIC_CENTER_ID, Workflow.FBM_WMS_OUTBOUND, filter, DATE_FROM, DATE_TO, DATE_FROM, DATE_TO
    );

    verify(planningModelApiClient).getBacklogPlanned(backlogPlannedRequestArgumentCaptor.capture());
    final BacklogPlannedRequest inputs = backlogPlannedRequestArgumentCaptor.getValue();


    Assertions.assertTrue(inputs.getGroupBy().stream().anyMatch(grouper -> groupBy == grouper));
    Assertions.assertEquals(expectedSize, result.size());
    Assertions.assertTrue(result.containsKey(expectedDate));


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
            .getSalesDistributionPlanned(LOGISTIC_CENTER_ID, Workflow.FBM_WMS_OUTBOUND, Filter.DATE_IN, DATE_FROM, DATE_TO,
                DATE_FROM, DATE_TO)
    );


  }

  private List<BacklogPlannedResponse> mockPlannedSales(final PlannedGrouper groupBy) {

    return DATE_IN.equals(groupBy)
        ? List.of(
        new BacklogPlannedResponse(new BacklogPlannedResponse.GroupKey(null, DATE_FROM, null), 10),
        new BacklogPlannedResponse(new BacklogPlannedResponse.GroupKey(null, DATE_FROM.plus(1, ChronoUnit.HOURS), null), 10),
        new BacklogPlannedResponse(new BacklogPlannedResponse.GroupKey(null, DATE_FROM.plus(2, ChronoUnit.HOURS), null), 10)
    )
        : List.of(
        new BacklogPlannedResponse(new BacklogPlannedResponse.GroupKey(null, null, DATE_TO), 12),
        new BacklogPlannedResponse(new BacklogPlannedResponse.GroupKey(null, null, DATE_TO.plus(1, ChronoUnit.HOURS)), 12)
    );
  }

}
