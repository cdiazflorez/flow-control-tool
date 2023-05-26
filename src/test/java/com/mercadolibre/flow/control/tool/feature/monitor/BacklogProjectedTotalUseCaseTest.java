package com.mercadolibre.flow.control.tool.feature.monitor;

import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.BATCH_SORTER;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.PACKING;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.PACKING_WALL;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.PICKING;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.WALL_IN;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.WAVING;
import static com.mercadolibre.flow.control.tool.feature.entity.ValueType.UNITS;
import static com.mercadolibre.flow.control.tool.feature.entity.Workflow.FBM_WMS_OUTBOUND;
import static com.mercadolibre.flow.control.tool.util.TestUtils.DATE_FROM;
import static com.mercadolibre.flow.control.tool.util.TestUtils.DATE_TO;
import static com.mercadolibre.flow.control.tool.util.TestUtils.LOGISTIC_CENTER_ID;
import static java.time.temporal.ChronoUnit.HOURS;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.mercadolibre.flow.control.tool.feature.backlog.monitor.BacklogProjectedGateway;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.BacklogProjectedTotalUseCase;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.PlannedEntitiesGateway;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.domain.ProjectionTotal;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.ProcessPathMonitor;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.SlasMonitor;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.TotalBacklogMonitor;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BacklogProjectedTotalUseCaseTest {

  private static final Set<ProcessName> PROCESSES = Set.of(
      WAVING,
      PICKING,
      BATCH_SORTER,
      WALL_IN,
      PACKING,
      PACKING_WALL
  );

  private static final Set<ProcessName> THROUGHPUT_PROCESS = Set.of(
      PACKING,
      PACKING_WALL
  );

  private static final int NUMBER_OF_HOURS = (int) HOURS.between(DATE_FROM, DATE_TO);

  private static final int NUMBER_OF_PATHS = ProcessPathName.allPaths().size();

  private static final int QUANTITY = 10;

  @Mock
  private BacklogProjectedGateway backlogProjectedGateway;

  @Mock
  private PlannedEntitiesGateway plannedEntitiesGateway;

  @Mock
  private BacklogProjectedTotalUseCase.TotalBacklogProjectionGateway totalBacklogProjectionGateway;

  @InjectMocks
  private BacklogProjectedTotalUseCase backlogProjectedTotalUseCase;

  @Test
  @DisplayName("Should get total backlog projection")
  void testGetTotalProjectionOk() {
    //GIVEN
    final List<ProcessPathMonitor> processPathMonitors = ProcessPathName.allPaths().stream()
        .map(processPath -> new ProcessPathMonitor(processPath, QUANTITY))
        .toList();

    final List<SlasMonitor> slasMonitors = IntStream.rangeClosed(0, NUMBER_OF_HOURS - 1)
        .mapToObj(hour -> new SlasMonitor(DATE_FROM.plus(hour, HOURS),
                                          processPathMonitors.stream().mapToInt(ProcessPathMonitor::quantity).sum(),
                                          processPathMonitors))
        .toList();

    final List<TotalBacklogMonitor> expectedTotalMonitor = IntStream.rangeClosed(0, NUMBER_OF_HOURS - 1)
        .mapToObj(hour -> new TotalBacklogMonitor(DATE_FROM.plus(hour, HOURS),
                                                  slasMonitors.stream().mapToInt(SlasMonitor::quantity).sum(),
                                                  slasMonitors))
        .toList();

    when(backlogProjectedGateway.getBacklogTotalsByProcessAndPPandSla(LOGISTIC_CENTER_ID, FBM_WMS_OUTBOUND, PROCESSES, DATE_FROM))
        .thenReturn(mockBacklogTotalsByProcessAndPPandSla());

    when(plannedEntitiesGateway.getPlannedUnitByPPDateInAndDateOut(FBM_WMS_OUTBOUND, LOGISTIC_CENTER_ID, DATE_FROM, DATE_TO))
        .thenReturn(mockPlannedBacklog());

    when(plannedEntitiesGateway.getThroughputByDateAndProcess(FBM_WMS_OUTBOUND, LOGISTIC_CENTER_ID, DATE_FROM, DATE_TO, THROUGHPUT_PROCESS))
        .thenReturn(mockThroughput());

    when(totalBacklogProjectionGateway.getTotalProjection(eq(LOGISTIC_CENTER_ID),
                                                          eq(DATE_FROM),
                                                          eq(DATE_TO),
                                                          anyMap(),
                                                          anyMap(),
                                                          anyMap())).thenReturn(mockProjectionTotal());

    //WHEN
    final List<TotalBacklogMonitor> totalBacklogMonitors = backlogProjectedTotalUseCase.getTotalProjection(LOGISTIC_CENTER_ID,
                                                                                                           FBM_WMS_OUTBOUND,
                                                                                                           PROCESSES,
                                                                                                           THROUGHPUT_PROCESS,
                                                                                                           UNITS,
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

  private Map<ProcessName, Map<ProcessPathName, Map<Instant, Integer>>> mockBacklogTotalsByProcessAndPPandSla() {

    final Map<Instant, Integer> quantityByDate = new ConcurrentHashMap<>(NUMBER_OF_HOURS);
    for (int i = 0; i < NUMBER_OF_HOURS; i++) {
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
                  final Map<Instant, Integer> quantityByDate = new ConcurrentHashMap<>(NUMBER_OF_HOURS);
                  for (int i = 0; i < NUMBER_OF_HOURS; i++) {
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
    final Map<Instant, Map<ProcessName, Integer>> quantityByDateAndProcess = new ConcurrentHashMap<>(NUMBER_OF_HOURS);
    for (int i = 0; i < NUMBER_OF_HOURS; i++) {
      quantityByDateAndProcess.put(DATE_FROM.plus(i, HOURS), quantityByProcess);
    }
    return quantityByDateAndProcess;
  }

  private List<ProjectionTotal> mockProjectionTotal() {
    final List<ProjectionTotal.Path> paths = ProcessPathName.allPaths().stream()
        .map(path -> new ProjectionTotal.Path(path, QUANTITY))
        .toList();
    final List<ProjectionTotal.SlaProjected> slasProjected = IntStream.rangeClosed(0, NUMBER_OF_HOURS - 1)
        .mapToObj(iterator -> new ProjectionTotal.SlaProjected(
            DATE_FROM.plus(iterator, HOURS),
            paths.stream().mapToInt(ProjectionTotal.Path::quantity).sum(),
            paths))
        .toList();
    return IntStream.rangeClosed(0, NUMBER_OF_HOURS - 1)
        .mapToObj(iterator -> new ProjectionTotal(DATE_FROM.plus(iterator, HOURS), slasProjected))
        .toList();
  }

}
