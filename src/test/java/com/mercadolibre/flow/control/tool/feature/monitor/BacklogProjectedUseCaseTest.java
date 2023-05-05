package com.mercadolibre.flow.control.tool.feature.monitor;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.mercadolibre.flow.control.tool.exception.NoUnitsPerOrderRatioFound;
import com.mercadolibre.flow.control.tool.feature.backlog.genericgateway.BacklogGateway;
import com.mercadolibre.flow.control.tool.feature.backlog.genericgateway.UnitsPerOrderRatioGateway;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.BacklogProjectedUseCase;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.BacklogMonitor;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.ProcessPathMonitor;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.ProcessesMonitor;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.SlasMonitor;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessPath;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BacklogProjectedUseCaseTest {
  private static final Workflow WORKFLOW = Workflow.FBM_WMS_OUTBOUND;
  private static final String LOGISTIC_CENTER = "ARTW01";
  private static final Instant OP_DATE1 = Instant.parse("2023-04-21T10:00:00Z");
  private static final Instant OP_DATE2 = Instant.parse("2023-04-21T11:00:00Z");
  private static final Instant OP_DATE3 = Instant.parse("2023-04-21T12:00:00Z");
  private static final Instant DATE_OUT = Instant.parse("2023-04-22T10:00:00Z");
  private static final Instant VIEW_DATE = Instant.parse("2023-05-03T08:00:00Z");
  private static final Map<ProcessName, Integer> CURRENT_BACKLOG = Map.of(ProcessName.PICKING, 100);
  private static final List<BacklogProjectedUseCase.PlannedBacklog> PLANNED_BACKLOGS = List.of(
      new BacklogProjectedUseCase.PlannedBacklog(OP_DATE1, DATE_OUT, 25),
      new BacklogProjectedUseCase.PlannedBacklog(OP_DATE2, DATE_OUT, 22),
      new BacklogProjectedUseCase.PlannedBacklog(OP_DATE3, DATE_OUT, 100)
  );
  private static final List<BacklogProjectedUseCase.Throughput> THROUGHPUT = List.of(
      new BacklogProjectedUseCase.Throughput(OP_DATE1, ProcessPath.TOT_MONO, ProcessName.PICKING, 100),
      new BacklogProjectedUseCase.Throughput(OP_DATE2, ProcessPath.TOT_MONO, ProcessName.PICKING, 75),
      new BacklogProjectedUseCase.Throughput(OP_DATE3, ProcessPath.TOT_MONO, ProcessName.PICKING, 50),
      new BacklogProjectedUseCase.Throughput(OP_DATE1, ProcessPath.TOT_MULTI_BATCH, ProcessName.PICKING, 50),
      new BacklogProjectedUseCase.Throughput(OP_DATE2, ProcessPath.TOT_MULTI_BATCH, ProcessName.PICKING, 75),
      new BacklogProjectedUseCase.Throughput(OP_DATE3, ProcessPath.TOT_MULTI_BATCH, ProcessName.PICKING, 100)
  );
  private static final Map<ProcessPath, Integer> BACKLOG_BY_PROCESS1 = Map.of(ProcessPath.TOT_MONO, 25, ProcessPath.TOT_MULTI_BATCH, 175);
  private static final Map<ProcessPath, Integer> BACKLOG_BY_PROCESS2 = Map.of(ProcessPath.TOT_MONO, 0, ProcessPath.TOT_MULTI_BATCH, 0);
  private static final Map<ProcessPath, Integer> BACKLOG_BY_PROCESS3 = Map.of(ProcessPath.TOT_MONO, 22, ProcessPath.TOT_MULTI_BATCH, 0);
  private static final Map<Instant, Map<ProcessName, Map<Instant, Map<ProcessPath, Integer>>>> BACKLOG = Map.of(
      OP_DATE1,
      Map.of(
          ProcessName.PICKING,
          Map.of(DATE_OUT, BACKLOG_BY_PROCESS1)
      ),
      OP_DATE2,
      Map.of(
          ProcessName.PICKING,
          Map.of(DATE_OUT, BACKLOG_BY_PROCESS2)
      ),
      OP_DATE3,
      Map.of(
          ProcessName.PICKING,
          Map.of(DATE_OUT, BACKLOG_BY_PROCESS3)
      )
  );

  private static final Map<Instant, Map<ProcessName, Map<Instant, Map<ProcessPath, Integer>>>> BACKLOG_DISORDER1 = Map.of(
      OP_DATE2,
      Map.of(
          ProcessName.PICKING,
          Map.of(DATE_OUT, BACKLOG_BY_PROCESS2)
      ),
      OP_DATE3,
      Map.of(
          ProcessName.PICKING,
          Map.of(DATE_OUT, BACKLOG_BY_PROCESS3)
      ),
      OP_DATE1,
      Map.of(
          ProcessName.PICKING,
          Map.of(DATE_OUT, BACKLOG_BY_PROCESS1)
      )
  );

  private static final Map<Instant, Map<ProcessName, Map<Instant, Map<ProcessPath, Integer>>>> BACKLOG_DISORDER2 = Map.of(
      OP_DATE3,
      Map.of(
          ProcessName.PICKING,
          Map.of(DATE_OUT, BACKLOG_BY_PROCESS3)
      ),
      OP_DATE2,
      Map.of(
          ProcessName.PICKING,
          Map.of(DATE_OUT, BACKLOG_BY_PROCESS2)
      ),
      OP_DATE1,
      Map.of(
          ProcessName.PICKING,
          Map.of(DATE_OUT, BACKLOG_BY_PROCESS1)
      )
  );

  @Mock
  private BacklogGateway backlogApiGateway;

  @Mock
  private BacklogProjectedUseCase.PlanningEntitiesGateway planningEntitiesGateway;

  @Mock
  private BacklogProjectedUseCase.BacklogProjectionGateway backlogProjectionGateway;

  @Mock
  private UnitsPerOrderRatioGateway unitsPerOrderRatioGateway;

  @InjectMocks
  private BacklogProjectedUseCase backlogProjectedUseCase;

  private static List<BacklogMonitor> expected() {
    return List.of(
        backlogMonitorMock(OP_DATE1, 25, 175),
        backlogMonitorMock(OP_DATE2, 0, 0),
        backlogMonitorMock(OP_DATE3, 22, 0)
    );
  }

  private static Stream<ParametersTest> parameterBacklog() {
    return Stream.of(
        new ParametersTest(CURRENT_BACKLOG, PLANNED_BACKLOGS, THROUGHPUT, BACKLOG, expected()),
        new ParametersTest(emptyMap(), PLANNED_BACKLOGS, THROUGHPUT, emptyMap(), emptyList()),
        new ParametersTest(emptyMap(), emptyList(), THROUGHPUT, emptyMap(), emptyList()),
        new ParametersTest(emptyMap(), emptyList(), emptyList(), emptyMap(), emptyList()),
        new ParametersTest(CURRENT_BACKLOG, emptyList(), THROUGHPUT, emptyMap(), emptyList()),
        new ParametersTest(emptyMap(), PLANNED_BACKLOGS, emptyList(), emptyMap(), emptyList())
    );
  }

  private static Stream<ParametersTest> parameterBacklogOrderDate() {
    return Stream.of(
        new ParametersTest(CURRENT_BACKLOG, PLANNED_BACKLOGS, THROUGHPUT, BACKLOG_DISORDER1, expected()),
        new ParametersTest(CURRENT_BACKLOG, PLANNED_BACKLOGS, THROUGHPUT, BACKLOG_DISORDER2, expected())
    );
  }

  private static BacklogMonitor backlogMonitorMock(final Instant date, final Integer quantityTotMono, final Integer quantityTotMultiBatch) {

    final Integer totalQuantity = quantityTotMono + quantityTotMultiBatch;

    return new BacklogMonitor(date,
        List.of(new ProcessesMonitor(
                ProcessName.PICKING,
                totalQuantity,
                List.of(
                    new SlasMonitor(
                        DATE_OUT,
                        totalQuantity,
                        List.of(
                            new ProcessPathMonitor(
                                ProcessPath.TOT_MONO,
                                quantityTotMono
                            ),
                            new ProcessPathMonitor(
                                ProcessPath.TOT_MULTI_BATCH,
                                quantityTotMultiBatch
                            )
                        )
                    )
                )

            )
        )
    );

  }

  @ParameterizedTest
  @MethodSource("parameterBacklog")
  void testGetBacklogProjectedUseCase(final ParametersTest parameters) {
    whenGateways(parameters);

    final var response =
        backlogProjectedUseCase.getBacklogProjected(OP_DATE1, OP_DATE3, LOGISTIC_CENTER, WORKFLOW, Set.of(ProcessName.PICKING), VIEW_DATE);

    assertEquals(parameters.expected, response);
  }

  @ParameterizedTest
  @MethodSource("parameterBacklogOrderDate")
  void testOrderOperationDate(final ParametersTest parameters) {
    whenGateways(parameters);

    final var response =
        backlogProjectedUseCase.getBacklogProjected(OP_DATE1, OP_DATE3, LOGISTIC_CENTER, WORKFLOW, Set.of(ProcessName.PICKING), VIEW_DATE);

    assertEquals(parameters.expected, response);

  }

  @ParameterizedTest
  @MethodSource("parameterBacklog")
  void testGetBacklogProjectedUseCaseException(final ParametersTest parameters) {
    when(backlogApiGateway.getBacklogTotalsByProcess(LOGISTIC_CENTER, WORKFLOW, Set.of(ProcessName.PICKING), OP_DATE1))
        .thenReturn(parameters.currentBacklogs);

    when(planningEntitiesGateway.getPlannedBacklog(WORKFLOW, LOGISTIC_CENTER, OP_DATE1, OP_DATE3))
        .thenReturn(parameters.plannedBacklogs);

    when(planningEntitiesGateway.getThroughput(WORKFLOW, LOGISTIC_CENTER, OP_DATE1, OP_DATE3, Set.of(ProcessName.PICKING)))
        .thenReturn(parameters.throughput);

    when(backlogProjectionGateway.executeBacklogProjection(
        OP_DATE1, OP_DATE3, Set.of(ProcessName.PICKING), parameters.currentBacklogs, parameters.throughput, parameters.plannedBacklogs))
        .thenReturn(parameters.backlog);

    when(unitsPerOrderRatioGateway.getUnitsPerOrderRatio(WORKFLOW, LOGISTIC_CENTER, VIEW_DATE))
        .thenReturn(Optional.of(0.0));

    assertThrows(
        NoUnitsPerOrderRatioFound.class,
        () -> backlogProjectedUseCase
            .getBacklogProjected(OP_DATE1, OP_DATE3, LOGISTIC_CENTER, WORKFLOW, Set.of(ProcessName.PICKING), VIEW_DATE)
    );
  }

  private void whenGateways(final ParametersTest parameters) {
    when(backlogApiGateway.getBacklogTotalsByProcess(LOGISTIC_CENTER, WORKFLOW, Set.of(ProcessName.PICKING), OP_DATE1))
        .thenReturn(parameters.currentBacklogs);

    when(planningEntitiesGateway.getPlannedBacklog(WORKFLOW, LOGISTIC_CENTER, OP_DATE1, OP_DATE3))
        .thenReturn(parameters.plannedBacklogs);

    when(planningEntitiesGateway.getThroughput(WORKFLOW, LOGISTIC_CENTER, OP_DATE1, OP_DATE3, Set.of(ProcessName.PICKING)))
        .thenReturn(parameters.throughput);

    when(backlogProjectionGateway.executeBacklogProjection(
        OP_DATE1, OP_DATE3, Set.of(ProcessName.PICKING), parameters.currentBacklogs, parameters.throughput, parameters.plannedBacklogs))
        .thenReturn(parameters.backlog);

    when(unitsPerOrderRatioGateway.getUnitsPerOrderRatio(WORKFLOW, LOGISTIC_CENTER, VIEW_DATE))
        .thenReturn(Optional.of(3.96));
  }

  private record ParametersTest(
      Map<ProcessName, Integer> currentBacklogs,
      List<BacklogProjectedUseCase.PlannedBacklog> plannedBacklogs,
      List<BacklogProjectedUseCase.Throughput> throughput,
      Map<Instant, Map<ProcessName, Map<Instant, Map<ProcessPath, Integer>>>> backlog,
      List<BacklogMonitor> expected
  ) {
  }
}
