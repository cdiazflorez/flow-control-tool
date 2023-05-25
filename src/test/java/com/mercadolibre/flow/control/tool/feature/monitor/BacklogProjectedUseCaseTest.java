package com.mercadolibre.flow.control.tool.feature.monitor;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.mercadolibre.flow.control.tool.exception.NoUnitsPerOrderRatioFound;
import com.mercadolibre.flow.control.tool.feature.backlog.genericgateway.UnitsPerOrderRatioGateway;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.BacklogProjectedUseCase;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.BacklogProjectedUseCase.PlannedEntitiesGateway;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.BacklogMonitor;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.ProcessPathMonitor;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.ProcessesMonitor;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.SlasMonitor;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName;
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

  private static final Instant DATE_OUT2 = Instant.parse("2023-04-22T16:00:00Z");

  private static final Instant VIEW_DATE = Instant.parse("2023-05-03T08:00:00Z");

  private static final int PICKING_TOTAL = 50;

  private static final Map<ProcessName, Map<ProcessPathName, Map<Instant, Integer>>> CURRENT_BACKLOG = Map.of(
      ProcessName.PICKING,
      Map.of(
          ProcessPathName.TOT_MONO,
          Map.of(
              DATE_OUT, 100,
              DATE_OUT2, 100
          ),
          ProcessPathName.TOT_MULTI_BATCH,
          Map.of(
              DATE_OUT, 100,
              DATE_OUT2, 100
          )
      )
  );

  private static final Map<ProcessPathName, Map<Instant, Map<Instant, Integer>>> PLANNED_BACKLOGS = Map.of(
      ProcessPathName.TOT_MONO,
      Map.of(
          OP_DATE1,
          Map.of(DATE_OUT, 25)
      ),
      ProcessPathName.TOT_MULTI_BATCH,
      Map.of(
          OP_DATE2,
          Map.of(DATE_OUT, 22)
      )
  );

  private static final Map<Instant, Map<ProcessName, Integer>> THROUGHPUT = Map.of(
      OP_DATE1, Map.of(
          ProcessName.PICKING, 100,
          ProcessName.PACKING, 100
      ),
      OP_DATE2, Map.of(
          ProcessName.PICKING, 75,
          ProcessName.PACKING, 75
      ),
      OP_DATE3, Map.of(
          ProcessName.PICKING, 50,
          ProcessName.PACKING, 50
      )
  );

  private static final Map<ProcessPathName, Integer> BACKLOG_BY_PROCESS1 = Map.of(
      ProcessPathName.TOT_MONO, 25, ProcessPathName.TOT_MULTI_BATCH, 175);

  private static final Map<ProcessPathName, Integer> BACKLOG_BY_PROCESS2 = Map.of(
      ProcessPathName.TOT_MONO, 0, ProcessPathName.TOT_MULTI_BATCH, 0);

  private static final Map<ProcessPathName, Integer> BACKLOG_BY_PROCESS3 = Map.of(
      ProcessPathName.TOT_MONO, 22, ProcessPathName.TOT_MULTI_BATCH, 0);

  private static final Map<Instant, Map<ProcessName, Map<Instant, Map<ProcessPathName, Integer>>>> BACKLOG_PP = Map.of(
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

  private static final Map<Instant, Map<ProcessName, Map<Instant, Map<ProcessPathName, Integer>>>> BACKLOG_DISORDER1_PP = Map.of(
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

  private static final Map<Instant, Map<ProcessName, Map<Instant, Map<ProcessPathName, Integer>>>> BACKLOG_DISORDER2_PP = Map.of(
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

  private static final Map<Instant, Map<ProcessName, Map<Instant, Integer>>> BACKLOG = Map.of(
      OP_DATE1,
      Map.of(
          ProcessName.PICKING,
          Map.of(DATE_OUT, PICKING_TOTAL)
      ),
      OP_DATE2,
      Map.of(
          ProcessName.PICKING,
          Map.of(DATE_OUT, 100)
      ),
      OP_DATE3,
      Map.of(
          ProcessName.PICKING,
          Map.of(DATE_OUT, 150)
      )
  );

  private static final Map<Instant, Map<ProcessName, Map<Instant, Integer>>> BACKLOG_DISORDER1 = Map.of(
      OP_DATE2,
      Map.of(
          ProcessName.PICKING,
          Map.of(DATE_OUT, 100)
      ),
      OP_DATE3,
      Map.of(
          ProcessName.PICKING,
          Map.of(DATE_OUT, 150)
      ),
      OP_DATE1,
      Map.of(
          ProcessName.PICKING,
          Map.of(DATE_OUT, PICKING_TOTAL)
      )
  );

  private static final Map<Instant, Map<ProcessName, Map<Instant, Integer>>> BACKLOG_DISORDER2 = Map.of(
      OP_DATE3,
      Map.of(
          ProcessName.PICKING,
          Map.of(DATE_OUT, 150)
      ),
      OP_DATE2,
      Map.of(
          ProcessName.PICKING,
          Map.of(DATE_OUT, 100)
      ),
      OP_DATE1,
      Map.of(
          ProcessName.PICKING,
          Map.of(DATE_OUT, PICKING_TOTAL)
      )
  );

  @Mock
  private BacklogProjectedUseCase.BacklogGateway backlogApiGateway;

  @Mock
  private PlannedEntitiesGateway plannedEntitiesGateway;

  @Mock
  private BacklogProjectedUseCase.BacklogProjectionGateway backlogProjectionGateway;

  @Mock
  private UnitsPerOrderRatioGateway unitsPerOrderRatioGateway;

  @InjectMocks
  private BacklogProjectedUseCase backlogProjectedUseCase;

  private static List<BacklogMonitor> expected() {
    return List.of(
        backlogMonitorMock(OP_DATE1, 25, 50),
        backlogMonitorMock(OP_DATE2, 0, 100),
        backlogMonitorMock(OP_DATE3, 22, 150)
    );
  }

  private static List<BacklogMonitor> expectedNullPP() {
    return List.of(
        backlogMonitorMockNullPP(OP_DATE1, PICKING_TOTAL),
        backlogMonitorMockNullPP(OP_DATE2, 100),
        backlogMonitorMockNullPP(OP_DATE3, 150)
    );
  }

  private static Stream<ParametersTest> parameterBacklog() {
    return Stream.of(
        new ParametersTest(CURRENT_BACKLOG, PLANNED_BACKLOGS, THROUGHPUT, BACKLOG_PP, BACKLOG, expectedNullPP()),
        new ParametersTest(emptyMap(), PLANNED_BACKLOGS, THROUGHPUT, emptyMap(), emptyMap(), emptyList()),
        new ParametersTest(emptyMap(), emptyMap(), THROUGHPUT, emptyMap(), emptyMap(), emptyList()),
        new ParametersTest(emptyMap(), emptyMap(), emptyMap(), emptyMap(), emptyMap(), emptyList()),
        new ParametersTest(CURRENT_BACKLOG, emptyMap(), THROUGHPUT, emptyMap(), emptyMap(), emptyList()),
        new ParametersTest(emptyMap(), PLANNED_BACKLOGS, emptyMap(), emptyMap(), emptyMap(), emptyList())
    );
  }

  private static Stream<ParametersTest> parameterBacklogOrderDate() {
    return Stream.of(
        new ParametersTest(CURRENT_BACKLOG, PLANNED_BACKLOGS, THROUGHPUT, BACKLOG_DISORDER1_PP, BACKLOG_DISORDER1, expectedNullPP()),
        new ParametersTest(CURRENT_BACKLOG, PLANNED_BACKLOGS, THROUGHPUT, BACKLOG_DISORDER2_PP, BACKLOG_DISORDER2, expectedNullPP())
    );
  }

  private static BacklogMonitor backlogMonitorMock(
      final Instant date,
      final Integer quantityTotMono,
      final Integer quantityTotMultiBatch
  ) {

    final int totalQuantity = quantityTotMono + quantityTotMultiBatch;

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
                                ProcessPathName.TOT_MONO,
                                quantityTotMono
                            ),
                            new ProcessPathMonitor(
                                ProcessPathName.TOT_MULTI_BATCH,
                                quantityTotMultiBatch
                            )
                        )
                    )
                )

            )
        )
    );

  }

  private static BacklogMonitor backlogMonitorMockNullPP(
      final Instant date,
      final Integer totalQuantity
  ) {

    return new BacklogMonitor(date,
        List.of(new ProcessesMonitor(
                ProcessName.PICKING,
                totalQuantity,
                List.of(
                    new SlasMonitor(
                        DATE_OUT,
                        totalQuantity,
                        emptyList()
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

    final var response = backlogProjectedUseCase.getBacklogProjected(
        OP_DATE1,
        OP_DATE3,
        LOGISTIC_CENTER,
        WORKFLOW,
        Set.of(ProcessName.PICKING),
        VIEW_DATE
    );

    assertEquals(parameters.expectedBacklogMonitors, response);
  }

  @ParameterizedTest
  @MethodSource("parameterBacklogOrderDate")
  void testOrderOperationDate(final ParametersTest parameters) {
    whenGateways(parameters);

    final var response = backlogProjectedUseCase.getBacklogProjected(
        OP_DATE1,
        OP_DATE3,
        LOGISTIC_CENTER,
        WORKFLOW,
        Set.of(ProcessName.PICKING),
        VIEW_DATE
    );

    assertEquals(parameters.expectedBacklogMonitors, response);

  }

  @ParameterizedTest
  @MethodSource("parameterBacklog")
  void testGetBacklogProjectedUseCaseException(final ParametersTest parameters) {

    final Set<ProcessName> processNames = Set.of(ProcessName.PICKING);

    when(backlogApiGateway.getBacklogTotalsByProcessAndPPandSla(LOGISTIC_CENTER, WORKFLOW, processNames, OP_DATE1))
        .thenReturn(parameters.currentBacklogs);

    when(plannedEntitiesGateway.getPlannedUnitByPPDateInAndDateOut(WORKFLOW, LOGISTIC_CENTER, OP_DATE1, OP_DATE3))
        .thenReturn(parameters.plannedUnit);

    when(plannedEntitiesGateway.getThroughputByDateAndProcess(
        WORKFLOW,
        LOGISTIC_CENTER,
        OP_DATE1,
        OP_DATE3,
        processNames
    ))
        .thenReturn(parameters.throughput);

    when(backlogProjectionGateway.executeBacklogProjection(
        LOGISTIC_CENTER,
        OP_DATE1,
        OP_DATE3,
        processNames,
        parameters.currentBacklogs,
        parameters.plannedUnit,
        parameters.throughput)
    )
        .thenReturn(parameters.backlogProjectionByDateOut);

    when(unitsPerOrderRatioGateway.getUnitsPerOrderRatio(WORKFLOW, LOGISTIC_CENTER, VIEW_DATE))
        .thenReturn(Optional.of(0.0));

    assertThrows(
        NoUnitsPerOrderRatioFound.class,
        () -> backlogProjectedUseCase
            .getBacklogProjected(OP_DATE1, OP_DATE3, LOGISTIC_CENTER, WORKFLOW, processNames, VIEW_DATE)
    );
  }

  private void whenGateways(final ParametersTest parameters) {
    when(backlogApiGateway.getBacklogTotalsByProcessAndPPandSla(LOGISTIC_CENTER, WORKFLOW, Set.of(ProcessName.PICKING), OP_DATE1))
        .thenReturn(parameters.currentBacklogs);

    when(plannedEntitiesGateway.getPlannedUnitByPPDateInAndDateOut(WORKFLOW, LOGISTIC_CENTER, OP_DATE1, OP_DATE3))
        .thenReturn(parameters.plannedUnit);

    when(plannedEntitiesGateway.getThroughputByDateAndProcess(
        WORKFLOW,
        LOGISTIC_CENTER,
        OP_DATE1,
        OP_DATE3,
        Set.of(ProcessName.PICKING)
    ))
        .thenReturn(parameters.throughput);

    when(backlogProjectionGateway.executeBacklogProjection(
        LOGISTIC_CENTER,
        OP_DATE1,
        OP_DATE3,
        Set.of(ProcessName.PICKING),
        parameters.currentBacklogs,
        parameters.plannedUnit,
        parameters.throughput
    ))
        .thenReturn(parameters.backlogProjectionByDateOut);

    when(unitsPerOrderRatioGateway.getUnitsPerOrderRatio(WORKFLOW, LOGISTIC_CENTER, VIEW_DATE))
        .thenReturn(Optional.of(3.96));
  }

  private record ParametersTest(
      Map<ProcessName, Map<ProcessPathName, Map<Instant, Integer>>> currentBacklogs,
      Map<ProcessPathName, Map<Instant, Map<Instant, Integer>>> plannedUnit,
      Map<Instant, Map<ProcessName, Integer>> throughput,
      Map<Instant, Map<ProcessName, Map<Instant, Map<ProcessPathName, Integer>>>> backlogProjectionByPP,
      Map<Instant, Map<ProcessName, Map<Instant, Integer>>> backlogProjectionByDateOut,
      List<BacklogMonitor> expectedBacklogMonitors
  ) {
  }
}
