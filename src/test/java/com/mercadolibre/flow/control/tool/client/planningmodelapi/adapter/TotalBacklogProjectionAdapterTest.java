package com.mercadolibre.flow.control.tool.client.planningmodelapi.adapter;

import static com.mercadolibre.flow.control.tool.util.TestUtils.LOGISTIC_CENTER_ID;
import static com.mercadolibre.flow.control.tool.util.TestUtils.objectMapper;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mercadolibre.fbm.wms.outbound.commons.rest.HttpRequest;
import com.mercadolibre.fbm.wms.outbound.commons.rest.exception.ClientException;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.PlanningModelApiClient;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.TotalBacklogProjectionRequest;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.TotalBacklogProjectionResponse;
import com.mercadolibre.flow.control.tool.exception.TotalProjectionException;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.domain.ProjectionTotal;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.domain.SlaQuantity;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName;
import com.mercadolibre.restclient.Response;
import com.mercadolibre.restclient.http.Headers;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
class TotalBacklogProjectionAdapterTest {

  private static final Instant DATE_FROM = Instant.parse("2023-03-28T07:00:00Z");
  private static final Instant DATE_TO = Instant.parse("2023-03-28T10:00:00Z");
  private static final Instant DATE_IN = Instant.parse("2023-03-27T03:00:00Z");

  @Mock
  private PlanningModelApiClient planningModelApiClient;

  @InjectMocks
  private TotalBacklogProjectionAdapter totalBacklogProjectionAdapter;

  @Captor
  private ArgumentCaptor<TotalBacklogProjectionRequest> totalBacklogProjectionRequestCaptor;

  private static Stream<Arguments> processPathValidationArguments() {
    return Stream.of(
        Arguments.of(true),
        Arguments.of(false)
    );
  }

  @ParameterizedTest
  @MethodSource("processPathValidationArguments")
  @DisplayName("Test total backlog projection.")
  void totalProjectionTest(final boolean hasProcessPath) {
    //GIVEN
    final Map<ProcessPathName, List<SlaQuantity>> backlog = mockInputsWhitProcessPath();
    final Map<ProcessPathName, List<SlaQuantity>> plannedUnits = mockInputsWhitProcessPath();
    final Map<Instant, Integer> throughput = mockThroughput();


    when(planningModelApiClient.getTotalBacklogProjection(anyString(), any(TotalBacklogProjectionRequest.class)))
        .thenReturn(mockClientResponseWithProcessPath(hasProcessPath));

    //WHEN
    final List<ProjectionTotal> projection = totalBacklogProjectionAdapter
        .getTotalProjection(LOGISTIC_CENTER_ID, DATE_FROM, DATE_TO, backlog, plannedUnits, throughput);

    //THEN
    verify(planningModelApiClient).getTotalBacklogProjection(any(), totalBacklogProjectionRequestCaptor.capture());
    final TotalBacklogProjectionRequest inputRequestCaptured = totalBacklogProjectionRequestCaptor.getValue();


    if (hasProcessPath) {

      assertQuantityByProcessPath(inputRequestCaptured.backlog().processPath(), ProcessPathName.NON_TOT_MONO, null);
      assertQuantityByProcessPath(inputRequestCaptured.backlog().processPath(), ProcessPathName.TOT_MULTI_BATCH, null);
      assertQuantityByProcessPath(inputRequestCaptured.backlog().processPath(), ProcessPathName.NON_TOT_MULTI_BATCH, null);
      assertQuantityByProcessPath(inputRequestCaptured.plannedUnit().processPath(), ProcessPathName.NON_TOT_MONO, DATE_IN);
      assertQuantityByProcessPath(inputRequestCaptured.plannedUnit().processPath(), ProcessPathName.TOT_MULTI_BATCH, DATE_IN);
      assertQuantityByProcessPath(inputRequestCaptured.plannedUnit().processPath(), ProcessPathName.NON_TOT_MULTI_BATCH, DATE_IN);

      assertEquals(
          expectedThroughput(),
          inputRequestCaptured.throughput().stream()
              .sorted(Comparator.comparing(TotalBacklogProjectionRequest.Throughput::date))
              .toList()
      );

    }

    assertEquals(expectedProjection(hasProcessPath), projection);


  }

  @Test
  void testGetTotalProjectionError() throws JsonProcessingException {
    //GIVEN
    final Map<ProcessPathName, List<SlaQuantity>> backlog = mockInputsWhitProcessPath();
    final Map<ProcessPathName, List<SlaQuantity>> plannedUnits = mockInputsWhitProcessPath();
    final Map<Instant, Integer> throughput = mockThroughput();
    final ClientException ce = new ClientException(
        "PLANNING_MODEL_API",
        HttpRequest.builder()
            .url("URL")
            .build(),
        new Response(404, new Headers(Map.of()), objectMapper().writeValueAsBytes("total_projection_exception"))
    );
    when(planningModelApiClient.getTotalBacklogProjection(anyString(), any(TotalBacklogProjectionRequest.class)))
        .thenThrow(ce);

    assertThrows(
        TotalProjectionException.class,
        () -> totalBacklogProjectionAdapter
            .getTotalProjection(LOGISTIC_CENTER_ID, DATE_FROM, DATE_TO, backlog, plannedUnits, throughput)
    );
  }

  private void assertQuantityByProcessPath(final List<TotalBacklogProjectionRequest.ProcessPath> processPath,
                                           final ProcessPathName processPathName,
                                           final Instant dateIn) {
    assertEquals(
        buildExpectedProcessPathToRequest(processPathName, dateIn).quantity(),
        processPath.stream()
            .filter(pp -> pp.name().equals(processPathName))
            .map(TotalBacklogProjectionRequest.ProcessPath::quantity)
            .flatMap(Collection::stream)
            .sorted(Comparator.comparing(TotalBacklogProjectionRequest.Quantity::dateOut))
            .collect(Collectors.toList())
    );
  }


  private Map<ProcessPathName, List<SlaQuantity>> mockInputsWhitProcessPath() {
    return Map.of(
        ProcessPathName.NON_TOT_MONO, buildSlaQuantity(),
        ProcessPathName.TOT_MULTI_BATCH, buildSlaQuantity(),
        ProcessPathName.NON_TOT_MULTI_BATCH, buildSlaQuantity()
    );
  }

  private List<SlaQuantity> buildSlaQuantity() {
    return List.of(
        new SlaQuantity(DATE_IN, DATE_FROM.plus(1, ChronoUnit.HOURS), 10),
        new SlaQuantity(DATE_IN, DATE_FROM.plus(3, ChronoUnit.HOURS), 10)
    );
  }

  private Map<Instant, Integer> mockThroughput() {
    return Map.of(
        DATE_FROM, 10,
        DATE_FROM.plus(1, ChronoUnit.HOURS), 5,
        DATE_FROM.plus(2, ChronoUnit.HOURS), 20,
        DATE_FROM.plus(3, ChronoUnit.HOURS), 15
    );
  }

  private List<TotalBacklogProjectionResponse> mockClientResponseWithProcessPath(final boolean hasProcessPath) {
    return List.of(
        buildTotalBacklogProjectionResponse(DATE_FROM, hasProcessPath),
        buildTotalBacklogProjectionResponse(DATE_FROM.plus(1, ChronoUnit.HOURS), hasProcessPath),
        buildTotalBacklogProjectionResponse(DATE_FROM.plus(2, ChronoUnit.HOURS), hasProcessPath),
        buildTotalBacklogProjectionResponse(DATE_FROM.plus(3, ChronoUnit.HOURS), hasProcessPath)
    );
  }

  private TotalBacklogProjectionResponse buildTotalBacklogProjectionResponse(final Instant operationDate, final boolean hasProcessPath) {
    return new TotalBacklogProjectionResponse(
        operationDate,
        List.of(
            buildSlaClientResponse(DATE_FROM.plus(1, ChronoUnit.HOURS), hasProcessPath),
            buildSlaClientResponse(DATE_FROM.plus(3, ChronoUnit.HOURS), hasProcessPath)
        )
    );
  }

  private TotalBacklogProjectionResponse.Sla buildSlaClientResponse(final Instant date, final boolean hasProcessPath) {
    final List<TotalBacklogProjectionResponse.ProcessPath> processPath = hasProcessPath
        ? List.of(
        new TotalBacklogProjectionResponse.ProcessPath(ProcessPathName.NON_TOT_MONO, 10),
        new TotalBacklogProjectionResponse.ProcessPath(ProcessPathName.TOT_MULTI_BATCH, 5),
        new TotalBacklogProjectionResponse.ProcessPath(ProcessPathName.NON_TOT_MULTI_BATCH, 5))
        : null;

    return new TotalBacklogProjectionResponse.Sla(date, 20, processPath);
  }

  private List<TotalBacklogProjectionRequest.Throughput> expectedThroughput() {
    return List.of(
        new TotalBacklogProjectionRequest.Throughput(DATE_FROM, 10),
        new TotalBacklogProjectionRequest.Throughput(DATE_FROM.plus(1, ChronoUnit.HOURS), 5),
        new TotalBacklogProjectionRequest.Throughput(DATE_FROM.plus(2, ChronoUnit.HOURS), 20),
        new TotalBacklogProjectionRequest.Throughput(DATE_FROM.plus(3, ChronoUnit.HOURS), 15)
    );
  }

  private TotalBacklogProjectionRequest.ProcessPath buildExpectedProcessPathToRequest(
      final ProcessPathName processPath,
      final Instant dateIn
  ) {
    return new TotalBacklogProjectionRequest.ProcessPath(
        processPath,
        List.of(
            new TotalBacklogProjectionRequest.Quantity(dateIn, DATE_FROM.plus(1, ChronoUnit.HOURS), 10),
            new TotalBacklogProjectionRequest.Quantity(dateIn, DATE_FROM.plus(3, ChronoUnit.HOURS), 10)
        )
    );
  }

  private List<ProjectionTotal> expectedProjection(final boolean hasProcessPath) {
    return List.of(
        buildProjectionTotal(DATE_FROM, hasProcessPath),
        buildProjectionTotal(DATE_FROM.plus(1, ChronoUnit.HOURS), hasProcessPath),
        buildProjectionTotal(DATE_FROM.plus(2, ChronoUnit.HOURS), hasProcessPath),
        buildProjectionTotal(DATE_FROM.plus(3, ChronoUnit.HOURS), hasProcessPath)
    );
  }

  private ProjectionTotal buildProjectionTotal(final Instant operationDate, final boolean hasProcessPath) {
    return new ProjectionTotal(
        operationDate,
        List.of(
            buildSlaProjected(DATE_FROM.plus(1, ChronoUnit.HOURS), hasProcessPath),
            buildSlaProjected(DATE_FROM.plus(3, ChronoUnit.HOURS), hasProcessPath)
        )
    );
  }

  private ProjectionTotal.SlaProjected buildSlaProjected(final Instant slaDate, final boolean hasProcessPath) {
    return new ProjectionTotal.SlaProjected(
        slaDate,
        20,
        hasProcessPath
            ? List.of(
            new ProjectionTotal.Path(ProcessPathName.NON_TOT_MONO, 10),
            new ProjectionTotal.Path(ProcessPathName.TOT_MULTI_BATCH, 5),
            new ProjectionTotal.Path(ProcessPathName.NON_TOT_MULTI_BATCH, 5))
            : List.of()
    );
  }
}
