package com.mercadolibre.flow.control.tool.feature.monitor;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mercadolibre.flow.control.tool.feature.backlog.genericgateway.UnitsPerOrderRatioGateway;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.GetHistoricalBacklogUseCase;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.BacklogMonitor;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.ProcessPathMonitor;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.ProcessesMonitor;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.SlasMonitor;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetHistoricalBacklogUseCaseTest {

  private static final Map<ProcessPathName, Integer> BACKLOG_BY_PROCESS_PATH = Map.of(
      ProcessPathName.NON_TOT_MONO,
      10,
      ProcessPathName.TOT_MONO,
      20
  );

  private static final int N_PROCESSES = ProcessName.values().length;

  private static final Instant CPT1 = Instant.parse("2023-04-15T18:00:00Z");

  private static final Instant CPT2 = Instant.parse("2023-04-15T19:00:00Z");

  private static final Workflow WORKFLOW = Workflow.FBM_WMS_OUTBOUND;

  private static final String LOGISTIC_CENTER = "ARTW01";

  private static final Instant VIEW_DATE = Instant.parse("2023-05-03T08:00:00Z");

  private static final Map<ProcessName, Map<Instant, Map<ProcessPathName, Integer>>> BACKLOG = Map.of(
      ProcessName.PICKING,
      Map.of(
          CPT1,
          BACKLOG_BY_PROCESS_PATH,
          CPT2,
          BACKLOG_BY_PROCESS_PATH
      ),
      ProcessName.PACKING,
      Map.of(
          CPT1,
          BACKLOG_BY_PROCESS_PATH,
          CPT2,
          BACKLOG_BY_PROCESS_PATH
      ),
      ProcessName.HU_ASSEMBLY,
      Map.of(
          CPT1,
          BACKLOG_BY_PROCESS_PATH,
          CPT2,
          BACKLOG_BY_PROCESS_PATH
      ),
      ProcessName.SHIPPING,
      Map.of(
          CPT1,
          BACKLOG_BY_PROCESS_PATH,
          CPT2,
          BACKLOG_BY_PROCESS_PATH
      )
  );

  private static final Instant DATE1 = Instant.parse("2023-04-14T18:00:00Z");

  private static final Instant DATE2 = Instant.parse("2023-05-03T08:00:00Z");

  private static final Instant DATE_FROM = Instant.parse("2023-04-14T18:00:00Z");

  private static final Instant DATE_TO = Instant.parse("2023-04-14T20:00:00Z");

  private static final String LOGISTIC_CENTER_ID = "ARTW01";

  private static final Set<ProcessName> PROCESS_NAMES =
      Set.of(ProcessName.PICKING, ProcessName.PACKING, ProcessName.HU_ASSEMBLY, ProcessName.SHIPPING);

  private static final Set<ProcessName> PROCESS_NAMES_2 =
      Set.of(ProcessName.PICKING, ProcessName.PACKING);

  @InjectMocks
  private GetHistoricalBacklogUseCase backlogHistoricalUseCase;

  @Mock
  private GetHistoricalBacklogUseCase.BacklogGateway backlogGateway;

  @Mock
  private UnitsPerOrderRatioGateway unitsPerOrderRatioGateway;

  private static List<ProcessesMonitor> createEmptyProcessesList() {

    return Arrays.stream(ProcessName.values())
        .map(processName -> new ProcessesMonitor(processName, 0, emptyList()))
        .collect(Collectors.toList());
  }

  @Test
  void obtainBacklogHistorical() {
    final var dateFrom = DATE_FROM;
    final var dateTo = DATE_TO;

    when(backlogGateway.getBacklogByDateProcessAndPP(
        Workflow.FBM_WMS_OUTBOUND, LOGISTIC_CENTER_ID, PROCESS_NAMES, dateFrom, dateTo))
        .thenReturn(backlogMock());

    when(unitsPerOrderRatioGateway.getUnitsPerOrderRatio(WORKFLOW, LOGISTIC_CENTER, VIEW_DATE))
        .thenReturn(Optional.of(3.96));

    final var res = backlogHistoricalUseCase.backlogHistoricalMonitor(
        Workflow.FBM_WMS_OUTBOUND, LOGISTIC_CENTER_ID, PROCESS_NAMES, dateFrom, dateTo, VIEW_DATE);

    assertNotNull(res);
    assertionsBacklogMonitor(backlogMonitorsResponseMock(), res);
  }

  @Test
  void obtainBacklogHistoricalEmpty() {
    final var dateFrom = DATE_FROM;
    final var dateTo = DATE_TO;

    final var expectedEntries = Duration.between(dateFrom, dateTo).toHours() + 1;

    when(backlogGateway.getBacklogByDateProcessAndPP(
        Workflow.FBM_WMS_OUTBOUND, LOGISTIC_CENTER_ID, PROCESS_NAMES, dateFrom, dateTo))
        .thenReturn(emptyMap());

    when(unitsPerOrderRatioGateway.getUnitsPerOrderRatio(WORKFLOW, LOGISTIC_CENTER, VIEW_DATE))
        .thenReturn(Optional.of(3.96));

    var res = backlogHistoricalUseCase.backlogHistoricalMonitor(
        Workflow.FBM_WMS_OUTBOUND, LOGISTIC_CENTER_ID, PROCESS_NAMES, dateFrom, dateTo, VIEW_DATE);

    assertEquals(expectedEntries, res.size());
    assertEquals(N_PROCESSES, res.get(0).processes().size());
    assertEquals(0, res.get(0).processes().get(0).quantity());
  }

  @Test
  void obtainBacklogHistoricalEmptyGroups() {
    final var dateFrom = DATE1;
    final var dateTo = DATE2;

    when(backlogGateway.getBacklogByDateProcessAndPP(
        Workflow.FBM_WMS_OUTBOUND, LOGISTIC_CENTER_ID, PROCESS_NAMES, dateFrom, dateTo))
        .thenReturn(backlogNullProcessMock());

    when(unitsPerOrderRatioGateway.getUnitsPerOrderRatio(WORKFLOW, LOGISTIC_CENTER, VIEW_DATE))
        .thenReturn(Optional.of(3.96));

    var response = backlogHistoricalUseCase.backlogHistoricalMonitor(
        Workflow.FBM_WMS_OUTBOUND, LOGISTIC_CENTER_ID, PROCESS_NAMES, dateFrom, dateTo, VIEW_DATE);

    assertionEmptyGroups(response);
  }

  @Test
  void obtainBacklogHistoricalException() {
    // GIVEN
    final var dateFrom = DATE_FROM;
    final var dateTo = DATE_TO;
    final var expected = emptyBacklogMonitorsResponseMock();
    when(backlogGateway.getBacklogByDateProcessAndPP(
        Workflow.FBM_WMS_OUTBOUND, LOGISTIC_CENTER_ID, PROCESS_NAMES_2, dateFrom, dateTo))
        .thenReturn(backlogNullProcessMock());

    when(unitsPerOrderRatioGateway.getUnitsPerOrderRatio(WORKFLOW, LOGISTIC_CENTER, VIEW_DATE))
        .thenReturn(Optional.empty());

    // WHEN
    var response = backlogHistoricalUseCase
        .backlogHistoricalMonitor(Workflow.FBM_WMS_OUTBOUND, LOGISTIC_CENTER_ID, PROCESS_NAMES, dateFrom, dateTo, VIEW_DATE);

    // THEN
    //When we call not obtain the ratio then should call getBacklogByDateProcessAndPP() method without shipping process.
    verify(backlogGateway, times(1))
        .getBacklogByDateProcessAndPP(Workflow.FBM_WMS_OUTBOUND, LOGISTIC_CENTER_ID, PROCESS_NAMES_2, dateFrom, dateTo);
    assertEquals(expected, response);
  }

  private void assertionEmptyGroups(final List<BacklogMonitor> response) {
    final var processSize1 = response.stream().filter(res -> DATE1.equals(res.date())).findAny()
        .map(backlog -> backlog.processes().size())
        .orElseThrow();

    final var processSize2 = response.stream().filter(res -> DATE1.equals(res.date())).findAny()
        .map(backlog -> backlog.processes().size())
        .orElseThrow();

    assertEquals(N_PROCESSES, processSize1);
    assertEquals(N_PROCESSES, processSize2);
  }

  private void assertionsBacklogMonitor(final List<BacklogMonitor> expected, final List<BacklogMonitor> response) {
    expected.sort(Comparator.comparing(BacklogMonitor::date));
    response.sort(Comparator.comparing(BacklogMonitor::date));

    for (int i = 0; i < expected.size(); i++) {
      assertEquals(expected.get(i).date(), response.get(i).date());
      assertionProcess(expected.get(i).processes(), response.get(i).processes());
    }
  }

  private void assertionProcess(final List<ProcessesMonitor> expected, final List<ProcessesMonitor> response) {
    response.sort(Comparator.comparing(ProcessesMonitor::name));
    expected.sort(Comparator.comparing(ProcessesMonitor::name));

    for (int i = 0; i < expected.size(); i++) {
      assertEquals(expected.get(i).name(), response.get(i).name());
      assertEquals(expected.get(i).quantity(), response.get(i).quantity());
      assertionSlaMonitor(expected.get(i).slas(), response.get(i).slas());
    }
  }

  private void assertionSlaMonitor(final List<SlasMonitor> expected, final List<SlasMonitor> response) {
    response.sort(Comparator.comparing(SlasMonitor::date));
    expected.sort(Comparator.comparing(SlasMonitor::date));

    for (int i = 0; i < expected.size(); i++) {
      assertEquals(expected.get(i).date(), response.get(i).date());
      assertEquals(expected.get(i).quantity(), response.get(i).quantity());
      assertionProcessPathMonitor(expected.get(i).processPaths(), response.get(i).processPaths());
    }
  }

  private void assertionProcessPathMonitor(final List<ProcessPathMonitor> expected, final List<ProcessPathMonitor> response) {
    response.sort(Comparator.comparing(ProcessPathMonitor::name));
    expected.sort(Comparator.comparing(ProcessPathMonitor::name));

    for (int i = 0; i < expected.size(); i++) {
      assertEquals(expected.get(i).name(), response.get(i).name());
      assertEquals(expected.get(i).quantity(), response.get(i).quantity());

    }
  }

  private Map<Instant, Map<ProcessName, Map<Instant, Map<ProcessPathName, Integer>>>> backlogMock() {

    return Stream.iterate(DATE_FROM.truncatedTo(ChronoUnit.HOURS), instant -> instant.plus(Duration.ofHours(1)))
        .limit(Duration.between(DATE_FROM, DATE_TO).toHours() + 1)
        .collect(Collectors.toMap(Function.identity(), hour -> BACKLOG));
  }

  private Map<Instant, Map<ProcessName, Map<Instant, Map<ProcessPathName, Integer>>>> backlogNullProcessMock() {
    return Map.of(
        DATE1, emptyMap(),
        DATE2, emptyMap());
  }

  private List<BacklogMonitor> backlogMonitorsResponseMock() {

    return Stream.iterate(DATE_FROM.truncatedTo(ChronoUnit.HOURS), instant -> instant.plus(Duration.ofHours(1)))
        .limit(Duration.between(DATE_FROM, DATE_TO).toHours() + 1)
        .map(hour -> new BacklogMonitor(
                hour,
                processesMonitors()
            )
        ).collect(Collectors.toList());
  }

  private List<BacklogMonitor> emptyBacklogMonitorsResponseMock() {

    return Stream.iterate(DATE_FROM.truncatedTo(ChronoUnit.HOURS), instant -> instant.plus(Duration.ofHours(1)))
        .limit(Duration.between(DATE_FROM, DATE_TO).toHours() + 1)
        .map(hour -> new BacklogMonitor(
                hour,
                createEmptyProcessesList()
            )
        ).collect(Collectors.toList());
  }

  private List<ProcessesMonitor> processesMonitors() {
    final var processMonitors = new ArrayList<ProcessesMonitor>(4);

    processMonitors.add(new ProcessesMonitor(
        ProcessName.WAVING,
        0,
        emptyList()
    ));

    processMonitors.add(new ProcessesMonitor(
        ProcessName.PICKING,
        60,
        slasMonitors()
    ));

    processMonitors.add(new ProcessesMonitor(
        ProcessName.BATCH_SORTER,
        0,
        emptyList()
    ));

    processMonitors.add(new ProcessesMonitor(
        ProcessName.WALL_IN,
        0,
        emptyList()
    ));

    processMonitors.add(new ProcessesMonitor(
        ProcessName.PACKING,
        60,
        slasMonitors()
    ));

    processMonitors.add(new ProcessesMonitor(
        ProcessName.PACKING_WALL,
        0,
        emptyList()
    ));

    processMonitors.add(new ProcessesMonitor(
        ProcessName.HU_ASSEMBLY,
        15,
        slasMonitors()
    ));

    processMonitors.add(new ProcessesMonitor(
        ProcessName.SHIPPING,
        15,
        slasMonitors()
    ));

    return processMonitors;
  }

  private List<SlasMonitor> slasMonitors() {
    final var processPathMonitors = new ArrayList<ProcessPathMonitor>(2);
    processPathMonitors.add(new ProcessPathMonitor(
        ProcessPathName.NON_TOT_MONO,
        10
    ));

    processPathMonitors.add(new ProcessPathMonitor(
        ProcessPathName.TOT_MONO,
        20
    ));

    final var slaMonitors = new ArrayList<SlasMonitor>(1);

    slaMonitors.add(
        new SlasMonitor(
            CPT1,
            30,
            processPathMonitors
        )
    );

    slaMonitors.add(
        new SlasMonitor(
            CPT2,
            30,
            processPathMonitors
        )
    );

    return slaMonitors;
  }
}
