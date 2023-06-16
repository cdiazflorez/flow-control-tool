package com.mercadolibre.flow.control.tool.feature.monitor;

import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.BATCH_SORTER;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.PACKING;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.PACKING_WALL;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.PICKING;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.SHIPPING;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.WALL_IN;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.WAVING;
import static com.mercadolibre.flow.control.tool.feature.entity.ValueType.ORDERS;
import static com.mercadolibre.flow.control.tool.feature.entity.ValueType.UNITS;
import static com.mercadolibre.flow.control.tool.feature.entity.Workflow.FBM_WMS_OUTBOUND;
import static com.mercadolibre.flow.control.tool.util.TestUtils.DATE_FROM;
import static com.mercadolibre.flow.control.tool.util.TestUtils.DATE_FROM_RESPONSE_PROJECTION;
import static com.mercadolibre.flow.control.tool.util.TestUtils.DATE_TO;
import static com.mercadolibre.flow.control.tool.util.TestUtils.LOGISTIC_CENTER_ID;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.mercadolibre.flow.control.tool.feature.backlog.BacklogProjectedTotalUseCase;
import com.mercadolibre.flow.control.tool.feature.backlog.genericgateway.UnitsPerOrderRatioGateway;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.BacklogProjectedGateway;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.PlannedEntitiesGateway;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.domain.ProjectionTotal;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.ProcessPathMonitor;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.SlasMonitor;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.TotalBacklogMonitor;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName;
import com.mercadolibre.flow.control.tool.feature.entity.ValueType;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BacklogProjectedTotalUseCaseTest {

  private static final int QUANTITY = 10;

  private static final int DISPATCH_QUANTITY = 20;

  private static final int NUMBER_OF_HOURS_RESPONSE = (int) HOURS.between(DATE_FROM, DATE_TO) - 1;

  private static final int NUMBER_OF_PATHS = ProcessPathName.allPaths().size();

  private static final List<Integer> QUANTITY_VALUES = List.of(10, 20, 30, 40, 50);

  private static final List<ProcessPathMonitor> PROCESS_PATH_MONITORS = IntStream.range(0, NUMBER_OF_PATHS)
      .mapToObj(i -> new ProcessPathMonitor(ProcessPathName.allPaths().get(i), QUANTITY_VALUES.get(i % QUANTITY_VALUES.size())))
      .toList();

  private static final Integer PROCESS_PATH_QUANTITY = PROCESS_PATH_MONITORS.stream().mapToInt(ProcessPathMonitor::quantity).sum();

  private static final List<SlasMonitor> SLAS_MONITORS = IntStream.rangeClosed(0, NUMBER_OF_HOURS_RESPONSE - 1)
      .mapToObj(hour -> new SlasMonitor(DATE_FROM_RESPONSE_PROJECTION.plus(hour, HOURS),
          PROCESS_PATH_QUANTITY,
          PROCESS_PATH_MONITORS))
      .toList();

  private static final Integer SLAS_QUANTITY = SLAS_MONITORS.stream().mapToInt(SlasMonitor::quantity).sum();

  private static final List<TotalBacklogMonitor> EXPECTED_TOTAL_MONITOR = IntStream.rangeClosed(0, NUMBER_OF_HOURS_RESPONSE)
      .mapToObj(hour -> NUMBER_OF_HOURS_RESPONSE == hour
          ? new TotalBacklogMonitor(DATE_FROM_RESPONSE_PROJECTION.plus(hour, HOURS), 0, emptyList())
          : new TotalBacklogMonitor(DATE_FROM_RESPONSE_PROJECTION.plus(hour, HOURS), SLAS_QUANTITY, SLAS_MONITORS))
      .toList();

  private static final List<ProjectionTotal> EXPECTED_DISPATCH_TOTAL = IntStream.rangeClosed(0, NUMBER_OF_HOURS_RESPONSE)
      .mapToObj(hour -> hour != 0
          ? new ProjectionTotal(DATE_FROM_RESPONSE_PROJECTION.plus(hour, HOURS), emptyList())
          : new ProjectionTotal(
          DATE_FROM_RESPONSE_PROJECTION,
          List.of(
              new ProjectionTotal.SlaProjected(
                  DATE_FROM_RESPONSE_PROJECTION,
                  DISPATCH_QUANTITY,
                  List.of(
                      new ProjectionTotal.Path(
                          ProcessPathName.TOT_MONO,
                          DISPATCH_QUANTITY
                      )
                  )
              )
          )
      )).collect(Collectors.toList());

  private static final Set<ProcessName> PROCESSES = Set.of(
      WAVING,
      PICKING,
      BATCH_SORTER,
      WALL_IN,
      PACKING,
      PACKING_WALL
  );

  private static final List<SlasMonitor> SLAS_DISPATCH_MONITOR = List.of(new SlasMonitor(
      DATE_FROM_RESPONSE_PROJECTION,
      QUANTITY,
      List.of(new ProcessPathMonitor(ProcessPathName.TOT_MONO, QUANTITY))));

  private static final List<TotalBacklogMonitor> EXPECTED_DISPATCH_TOTAL_MONITOR = IntStream.rangeClosed(0, NUMBER_OF_HOURS_RESPONSE)
      .mapToObj(hour -> hour != 0
          ? new TotalBacklogMonitor(DATE_FROM_RESPONSE_PROJECTION.plus(hour, HOURS), 0, emptyList())
          : new TotalBacklogMonitor(DATE_FROM_RESPONSE_PROJECTION, QUANTITY, SLAS_DISPATCH_MONITOR))
      .collect(Collectors.toList());


  @InjectMocks
  private BacklogProjectedTotalUseCase backlogProjectedTotalUseCase;

  @Mock
  private BacklogProjectedGateway backlogProjectedGateway;

  @Mock
  private PlannedEntitiesGateway plannedEntitiesGateway;

  @Mock
  private BacklogProjectedTotalUseCase.TotalBacklogProjectionGateway totalBacklogProjectionGateway;

  @Mock
  private UnitsPerOrderRatioGateway unitsPerOrderRatioGateway;

  private static Stream<Arguments> provideThroughputData() {
    return Stream.of(
        Arguments.of(
            Set.of(
                PACKING,
                PACKING_WALL
            ),
            UNITS,
            EXPECTED_TOTAL_MONITOR,
            mockProjectionTotal()
        ),
        Arguments.of(
            Set.of(
                SHIPPING
            ),
            ORDERS,
            EXPECTED_DISPATCH_TOTAL_MONITOR,
            EXPECTED_DISPATCH_TOTAL
        )
    );
  }

  private static List<ProjectionTotal> mockProjectionTotal() {

    final List<ProjectionTotal.Path> paths = ProcessPathName.allPaths().stream()
        .map(path -> new ProjectionTotal.Path(path, QUANTITY_VALUES.get(ProcessPathName.allPaths().indexOf(path) % QUANTITY_VALUES.size())))
        .toList();

    final List<ProjectionTotal.SlaProjected> slasProjected = IntStream.rangeClosed(0, NUMBER_OF_HOURS_RESPONSE - 1)
        .mapToObj(index -> new ProjectionTotal.SlaProjected(
            DATE_FROM_RESPONSE_PROJECTION.plus(index, HOURS),
            paths.stream().mapToInt(ProjectionTotal.Path::quantity).sum(),
            paths))
        .toList();

    return IntStream.rangeClosed(0, NUMBER_OF_HOURS_RESPONSE)
        .mapToObj(index -> NUMBER_OF_HOURS_RESPONSE == index
            ? new ProjectionTotal(DATE_FROM_RESPONSE_PROJECTION.plus(index, HOURS), emptyList())
            : new ProjectionTotal(DATE_FROM_RESPONSE_PROJECTION.plus(index, HOURS), slasProjected))
        .toList();
  }

  @ParameterizedTest
  @MethodSource("provideThroughputData")
  @DisplayName("Should get total backlog projection")
  void testGetTotalProjectionWhenIsOrderValueOk(
      final Set<ProcessName> throughputProcess,
      final ValueType valueType,
      final List<TotalBacklogMonitor> expectedTotalMonitor,
      final List<ProjectionTotal> mockProjectionTotal
  ) {
    //GIVEN

    when(unitsPerOrderRatioGateway.getUnitsPerOrderRatio(FBM_WMS_OUTBOUND, LOGISTIC_CENTER_ID, DATE_FROM))
        .thenReturn(mockUnitsPerOrderRatio());

    when(backlogProjectedGateway.getBacklogTotalsByProcessAndPPandSla(LOGISTIC_CENTER_ID, FBM_WMS_OUTBOUND, PROCESSES, DATE_FROM))
        .thenReturn(mockBacklogTotalsByProcessAndPPandSla());

    when(plannedEntitiesGateway.getPlannedUnitByPPDateInAndDateOut(FBM_WMS_OUTBOUND, LOGISTIC_CENTER_ID, DATE_FROM, DATE_TO))
        .thenReturn(mockPlannedBacklog());

    when(plannedEntitiesGateway.getThroughputByDateAndProcess(FBM_WMS_OUTBOUND, LOGISTIC_CENTER_ID, DATE_FROM, DATE_TO,
        throughputProcess))
        .thenReturn(mockThroughput());

    when(totalBacklogProjectionGateway.getTotalProjection(eq(LOGISTIC_CENTER_ID),
        eq(DATE_FROM),
        eq(DATE_TO),
        anyMap(),
        anyMap(),
        anyMap())).thenReturn(mockProjectionTotal);

    //WHEN
    final List<TotalBacklogMonitor> totalBacklogMonitors = backlogProjectedTotalUseCase.getTotalProjection(LOGISTIC_CENTER_ID,
        FBM_WMS_OUTBOUND,
        PROCESSES,
        throughputProcess,
        valueType,
        DATE_FROM,
        DATE_TO,
        DATE_FROM);
    //THEN
    assertFalse(totalBacklogMonitors.isEmpty());
    assertAll(
        "Should get total backlog projection",
        () -> assertEquals(expectedTotalMonitor.size(), totalBacklogMonitors.size()),
        () -> assertTrue(expectedTotalMonitor.containsAll(totalBacklogMonitors)),
        () -> assertTrue(totalBacklogMonitors.containsAll(expectedTotalMonitor))
    );

  }

  private Optional<Double> mockUnitsPerOrderRatio() {
    return Optional.of(2.0);
  }

  private Map<ProcessName, Map<ProcessPathName, Map<Instant, Integer>>> mockBacklogTotalsByProcessAndPPandSla() {

    final Map<Instant, Integer> quantityByDate = new ConcurrentHashMap<>(NUMBER_OF_HOURS_RESPONSE);
    for (int i = 0; i < NUMBER_OF_HOURS_RESPONSE; i++) {
      quantityByDate.put(DATE_FROM.plus(i, HOURS), QUANTITY);
    }

    final Map<ProcessPathName, Map<Instant, Integer>> quantityByDateAndProcessPath = new ConcurrentHashMap<>(NUMBER_OF_PATHS);

    for (ProcessPathName processPathName : ProcessPathName.allPaths()) {
      quantityByDateAndProcessPath.put(processPathName, quantityByDate);
    }

    final Map<ProcessName, Map<ProcessPathName, Map<Instant, Integer>>> quantityByDateAndProcess =
        new ConcurrentHashMap<>(PROCESSES.size());

    for (ProcessName processName : PROCESSES) {
      quantityByDateAndProcess.put(processName, quantityByDateAndProcessPath);
    }

    return quantityByDateAndProcess;
  }

  private Map<ProcessPathName, Map<Instant, Map<Instant, Integer>>> mockPlannedBacklog() {

    return ProcessPathName.allPaths().stream()
        .collect(
            Collectors.toMap(
                processPathName -> processPathName,
                processPathName -> {
                  final Map<Instant, Integer> quantityByDate = new ConcurrentHashMap<>(NUMBER_OF_HOURS_RESPONSE);
                  for (int i = 0; i < NUMBER_OF_HOURS_RESPONSE; i++) {
                    quantityByDate.put(DATE_FROM.plus(i, HOURS), QUANTITY);
                  }
                  return Map.of(DATE_FROM, quantityByDate);
                }
            ));
  }

  private Map<Instant, Map<ProcessName, Integer>> mockThroughput() {
    final Map<ProcessName, Integer> quantityByProcess = new ConcurrentHashMap<>(PROCESSES.size());
    for (ProcessName processName : PROCESSES) {
      quantityByProcess.put(processName, QUANTITY);
    }
    final Map<Instant, Map<ProcessName, Integer>> quantityByDateAndProcess = new ConcurrentHashMap<>(NUMBER_OF_HOURS_RESPONSE);
    for (int i = 0; i < NUMBER_OF_HOURS_RESPONSE; i++) {
      quantityByDateAndProcess.put(DATE_FROM.plus(i, HOURS), quantityByProcess);
    }
    return quantityByDateAndProcess;
  }
}
