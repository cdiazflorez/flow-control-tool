package com.mercadolibre.flow.control.tool.client.planningmodelapi.adapter;

import static com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.OutboundProcessName.PACKING;
import static com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.OutboundProcessName.PICKING;
import static com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.PlanningWorkflow.FBM_WMS_OUTBOUND;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.BATCH_SORTER;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.HU_ASSEMBLY;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.PACKING_WALL;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.SHIPPING;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.WALL_IN;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.WAVING;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName.TOT_MONO;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName.TOT_MULTI_BATCH;
import static com.mercadolibre.flow.control.tool.util.TestUtils.DATE_FROM;
import static com.mercadolibre.flow.control.tool.util.TestUtils.DATE_TO;
import static com.mercadolibre.flow.control.tool.util.TestUtils.objectMapper;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mercadolibre.fbm.wms.outbound.commons.rest.HttpRequest;
import com.mercadolibre.fbm.wms.outbound.commons.rest.exception.ClientException;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.PlanningModelApiClient;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.BacklogProjectionRequest;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.BacklogProjectionResponse;
import com.mercadolibre.flow.control.tool.exception.ProjectionInputsNotFoundException;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName;
import com.mercadolibre.restclient.Response;
import com.mercadolibre.restclient.http.Headers;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BacklogProjectionAdapterTest {

  private static final String LOGISTIC_CENTER_ID = "ARTW01";

  private static final Instant DATE_OUT = Instant.parse("2023-04-22T10:00:00Z");

  private static final Instant DATE_OUT2 = Instant.parse("2023-04-22T16:00:00Z");

  private static final Instant DATE_IN = Instant.parse("2023-04-20T10:00:00Z");

  private static final Instant DATE_IN2 = Instant.parse("2023-04-20T16:00:00Z");

  private static final Integer PICKING_UNITS = 50;

  private static final Integer PACKING_UNITS = 100;

  private static final Set<ProcessName> ALL_PROCESSES_SET = Set.of(
      WAVING,
      ProcessName.PICKING,
      BATCH_SORTER,
      WALL_IN,
      ProcessName.PACKING,
      PACKING_WALL,
      HU_ASSEMBLY,
      SHIPPING
  );

  private static final Map<ProcessName, Map<ProcessPathName, Map<Instant, Integer>>> CURRENT_BACKLOG = Map.of(
      ProcessName.PICKING,
      Map.of(
          TOT_MONO,
          Map.of(
              DATE_OUT, PICKING_UNITS,
              DATE_OUT2, PACKING_UNITS
          ),
          TOT_MULTI_BATCH,
          Map.of(
              DATE_OUT, PICKING_UNITS,
              DATE_OUT2, PACKING_UNITS
          )
      )
  );

  private static final Map<ProcessPathName, Map<Instant, Map<Instant, Integer>>> PLANNED_BACKLOGS = Map.of(
      TOT_MONO,
      Map.of(
          DATE_IN,
          Map.of(DATE_OUT, PICKING_UNITS),
          DATE_IN2,
          Map.of(DATE_OUT2, PACKING_UNITS)
      ),
      TOT_MULTI_BATCH,
      Map.of(
          DATE_IN,
          Map.of(DATE_OUT, PICKING_UNITS),
          DATE_IN2,
          Map.of(DATE_OUT2, PACKING_UNITS)
      )
  );

  private static final Map<Instant, Map<ProcessName, Integer>> THROUGHPUT = Map.of(
      DATE_OUT, Map.of(
          ProcessName.PICKING, PICKING_UNITS
      ),
      DATE_OUT2, Map.of(
          ProcessName.PACKING, PACKING_UNITS
      )
  );

  @InjectMocks
  private BacklogProjectionAdapter backlogProjectionAdapter;

  @Mock
  private PlanningModelApiClient planningModelApiClient;

  @Test
  void testExecuteBacklogProjectionError() throws JsonProcessingException {
    // GIVEN
    final ClientException ce = new ClientException(
        "PLANNING_MODEL_API",
        HttpRequest.builder()
            .url("URL")
            .build(),
        new Response(404, new Headers(Map.of()), objectMapper().writeValueAsBytes("projection_inputs_exception"))
    );

    when(planningModelApiClient.getBacklogProjection(
        eq(LOGISTIC_CENTER_ID),
        any(BacklogProjectionRequest.class)
    ))
        .thenThrow(ce);

    assertThrows(
        ProjectionInputsNotFoundException.class,
        () -> backlogProjectionAdapter.executeBacklogProjection(
            LOGISTIC_CENTER_ID,
            DATE_FROM,
            DATE_TO,
            ALL_PROCESSES_SET,
            CURRENT_BACKLOG,
            PLANNED_BACKLOGS,
            THROUGHPUT
        )
    );
  }

  @Test
  void testBacklogProjectionAdapter() {
    // GIVEN
    final BacklogProjectionRequest backlogProjectionRequest = mockBacklogProjectionRequest();
    final List<BacklogProjectionResponse> backlogProjectionResponse = mockBacklogProjectionResponse();

    when(planningModelApiClient
        .getBacklogProjection(LOGISTIC_CENTER_ID, backlogProjectionRequest))
        .thenReturn(backlogProjectionResponse);

    // WHEN
    final var backlogProjection = backlogProjectionAdapter.executeBacklogProjection(
        LOGISTIC_CENTER_ID,
        DATE_FROM,
        DATE_TO,
        ALL_PROCESSES_SET,
        CURRENT_BACKLOG,
        PLANNED_BACKLOGS,
        THROUGHPUT
    );

    //THEN
    assertEquals(expectedAdaptedResponse(), backlogProjection);
  }

  @ParameterizedTest
  @MethodSource("argumentProviderBacklogProjection")
  void testBacklogProjectionAdapterEmptyNullResponse(final List<BacklogProjectionResponse> backlogProjectionResponses) {
    // GIVEN
    final BacklogProjectionRequest backlogProjectionRequest = mockBacklogProjectionRequest();

    when(planningModelApiClient
        .getBacklogProjection(LOGISTIC_CENTER_ID, backlogProjectionRequest))
        .thenReturn(backlogProjectionResponses);

    // WHEN
    final var backlogProjection = backlogProjectionAdapter.executeBacklogProjection(
        LOGISTIC_CENTER_ID,
        DATE_FROM,
        DATE_TO,
        ALL_PROCESSES_SET,
        CURRENT_BACKLOG,
        PLANNED_BACKLOGS,
        THROUGHPUT
    );

    //THEN
    assertEquals(emptyMap(), backlogProjection);
  }

  private static Stream<Arguments> argumentProviderBacklogProjection() {

    return Stream.of(
        Arguments.of((Object) null),
        Arguments.of(emptyList())
    );
  }

  private static Map<Instant, Map<ProcessName, Map<Instant, Integer>>> expectedAdaptedResponse() {
    Map<Instant, Map<ProcessName, Map<Instant, Integer>>> projectionAdapted = new ConcurrentHashMap<>();
    projectionAdapted.computeIfAbsent(DATE_FROM, k -> new ConcurrentHashMap<>())
        .computeIfAbsent(ProcessName.PICKING, k -> new ConcurrentHashMap<>())
        .put(DATE_FROM, PICKING_UNITS);
    return projectionAdapted;
  }

  private BacklogProjectionRequest mockBacklogProjectionRequest() {

    final var requestBacklog = mockBacklogForProjectionRequest();

    final var requestPlannedUnit = mockPlannedUnitForProjectionRequest();

    final var throughput = mockThroughputForProjectionRequest();

    return new BacklogProjectionRequest(
        requestBacklog,
        requestPlannedUnit,
        throughput,
        DATE_FROM,
        DATE_TO,
        FBM_WMS_OUTBOUND
    );
  }

  private static BacklogProjectionRequest.Backlog mockBacklogForProjectionRequest() {
    return new BacklogProjectionRequest.Backlog(Set.of(
        new BacklogProjectionRequest.Process(
            PICKING,
            Set.of(
                new BacklogProjectionRequest.ProcessPath(
                    TOT_MONO,
                    Set.of(
                        new BacklogProjectionRequest.Quantity(
                            null,
                            DATE_OUT,
                            PICKING_UNITS
                        ),
                        new BacklogProjectionRequest.Quantity(
                            null,
                            DATE_OUT2,
                            PACKING_UNITS
                        )
                    )
                ),
                new BacklogProjectionRequest.ProcessPath(
                    TOT_MULTI_BATCH,
                    Set.of(
                        new BacklogProjectionRequest.Quantity(
                            null,
                            DATE_OUT,
                            PICKING_UNITS
                        ),
                        new BacklogProjectionRequest.Quantity(
                            null,
                            DATE_OUT2,
                            PACKING_UNITS
                        )
                    )

                )
            ),
            null
        )
    ));
  }

  private static BacklogProjectionRequest.PlannedUnit mockPlannedUnitForProjectionRequest() {
    return new BacklogProjectionRequest.PlannedUnit(
        Set.of(
            new BacklogProjectionRequest.ProcessPath(
                TOT_MONO,
                Set.of(
                    new BacklogProjectionRequest.Quantity(
                        DATE_IN,
                        DATE_OUT,
                        PICKING_UNITS
                    ),
                    new BacklogProjectionRequest.Quantity(
                        DATE_IN2,
                        DATE_OUT2,
                        PACKING_UNITS
                    )
                )
            ),
            new BacklogProjectionRequest.ProcessPath(
                TOT_MULTI_BATCH,
                Set.of(
                    new BacklogProjectionRequest.Quantity(
                        DATE_IN,
                        DATE_OUT,
                        PICKING_UNITS
                    ),
                    new BacklogProjectionRequest.Quantity(
                        DATE_IN2,
                        DATE_OUT2,
                        PACKING_UNITS
                    )
                )
            )
        )
    );
  }

  private static Set<BacklogProjectionRequest.Throughput> mockThroughputForProjectionRequest() {
    return Set.of(
        new BacklogProjectionRequest.Throughput(
            DATE_OUT,
            Set.of(
                new BacklogProjectionRequest.Process(
                    PICKING,
                    emptySet(),
                    PICKING_UNITS
                )
            )
        ),
        new BacklogProjectionRequest.Throughput(
            DATE_OUT2,
            Set.of(
                new BacklogProjectionRequest.Process(
                    PACKING,
                    emptySet(),
                    PACKING_UNITS
                )
            )
        )
    );
  }

  private List<BacklogProjectionResponse> mockBacklogProjectionResponse() {
    final BacklogProjectionResponse.ProcessPath processPath = new BacklogProjectionResponse.ProcessPath(TOT_MONO, PICKING_UNITS);
    final BacklogProjectionResponse.Sla sla =
        new BacklogProjectionResponse.Sla(DATE_FROM, 50, List.of(processPath));
    final BacklogProjectionResponse.Process process = new BacklogProjectionResponse.Process(PICKING, List.of(sla));

    final BacklogProjectionResponse.Backlog backlog = new BacklogProjectionResponse.Backlog(List.of(process));
    return List.of(new BacklogProjectionResponse(DATE_FROM, List.of(backlog)));
  }
}
