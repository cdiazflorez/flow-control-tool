package com.mercadolibre.flow.control.tool.feature.monitor;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import com.mercadolibre.flow.control.tool.feature.backlog.monitor.GetHistoricalBacklogUseCase;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.BacklogMonitor;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.ProcessPathMonitor;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.ProcessesMonitor;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.SlasMonitor;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessPath;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetHistoricalBacklogUseCaseTest {

  private static final Map<ProcessPath, Integer> BACKLOG_BY_PROCESS_PATH = Map.of(
      ProcessPath.NON_TOT_MONO,
      10,
      ProcessPath.TOT_MONO,
      20
  );
  private static final int NO_PROCESS = 0;

  private static final Instant CPT1 = Instant.parse("2023-04-15T18:00:00Z");

  private static final Instant CPT2 = Instant.parse("2023-04-15T19:00:00Z");

  private static final Map<ProcessName, Map<Instant, Map<ProcessPath, Integer>>> BACKLOG = Map.of(
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
      )
  );

  private static final Instant DATE1 = Instant.parse("2023-04-14T18:00:00Z");

  private static final Instant DATE2 = Instant.parse("2023-04-14T19:00:00Z");

  private static final String LOGISTIC_CENTER_ID = "ARTW01";

  private static final Set<ProcessName> PROCESS_NAMES = Set.of(ProcessName.PICKING, ProcessName.PACKING);
  @InjectMocks
  private GetHistoricalBacklogUseCase backlogHistoricalUseCase;

  @Mock
  private GetHistoricalBacklogUseCase.BacklogGateway backlogGateway;

  @Test
  void obtainBacklogHistorical() {
    final var dateFrom = DATE1;
    final var dateTo = DATE2;

    when(backlogGateway.getBacklogByDateProcessAndPP(
        Workflow.FBM_WMS_OUTBOUND, LOGISTIC_CENTER_ID, PROCESS_NAMES, dateFrom, dateTo))
        .thenReturn(backlogMock());

    final var res = backlogHistoricalUseCase.backlogHistoricalMonitor(
        Workflow.FBM_WMS_OUTBOUND, LOGISTIC_CENTER_ID, PROCESS_NAMES, dateFrom, dateTo);

    assertNotNull(res);
    assertionsBacklogMonitor(backlogMonitorsResponseMock(), res);
  }

  @Test
  void obtainBacklogHistoricalEmpty() {
    final var dateFrom = DATE1;
    final var dateTo = DATE2;

    when(backlogGateway.getBacklogByDateProcessAndPP(
        Workflow.FBM_WMS_OUTBOUND, LOGISTIC_CENTER_ID, PROCESS_NAMES, dateFrom, dateTo))
        .thenReturn(emptyMap());

    var res = backlogHistoricalUseCase.backlogHistoricalMonitor(
        Workflow.FBM_WMS_OUTBOUND, LOGISTIC_CENTER_ID, PROCESS_NAMES, dateFrom, dateTo);

    assertEquals(emptyList(), res);
  }

  @Test
  void obtainBacklogHistoricalEmptyGroups() {
    final var dateFrom = DATE1;
    final var dateTo = DATE2;

    when(backlogGateway.getBacklogByDateProcessAndPP(
        Workflow.FBM_WMS_OUTBOUND, LOGISTIC_CENTER_ID, PROCESS_NAMES, dateFrom, dateTo))
        .thenReturn(backlogNullProcessMock());

    var response = backlogHistoricalUseCase.backlogHistoricalMonitor(
        Workflow.FBM_WMS_OUTBOUND, LOGISTIC_CENTER_ID, PROCESS_NAMES, dateFrom, dateTo);

    assertionEmptyGroups(response);
  }

  private void assertionEmptyGroups(final List<BacklogMonitor> response) {
    final var processSize1 = response.stream().filter(res -> DATE1.equals(res.date())).findAny()
        .map(backlog -> backlog.processes().size())
        .orElseThrow();

    final var processSize2 = response.stream().filter(res -> DATE1.equals(res.date())).findAny()
        .map(backlog -> backlog.processes().size())
        .orElseThrow();

    assertEquals(NO_PROCESS, processSize1);
    assertEquals(NO_PROCESS, processSize2);
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

  private Map<Instant, Map<ProcessName, Map<Instant, Map<ProcessPath, Integer>>>> backlogMock() {
    return Map.of(DATE1, BACKLOG, DATE2, BACKLOG);
  }

  private Map<Instant, Map<ProcessName, Map<Instant, Map<ProcessPath, Integer>>>> backlogNullProcessMock() {
    return Map.of(
        DATE1, emptyMap(),
        DATE2, emptyMap());
  }

  private List<BacklogMonitor> backlogMonitorsResponseMock() {
    final var backlogMonitors = new ArrayList<BacklogMonitor>(2);
    backlogMonitors.add(
        new BacklogMonitor(
            DATE1,
            processesMonitors()
        )
    );

    backlogMonitors.add(
        new BacklogMonitor(
            DATE2,
            processesMonitors()
        )
    );

    return backlogMonitors;
  }

  private List<ProcessesMonitor> processesMonitors() {
    final var processMonitors = new ArrayList<ProcessesMonitor>(1);
    processMonitors.add(new ProcessesMonitor(
        ProcessName.PICKING,
        60,
        slasMonitors()
    ));

    processMonitors.add(new ProcessesMonitor(
        ProcessName.PACKING,
        60,
        slasMonitors()
    ));

    return processMonitors;
  }

  private List<SlasMonitor> slasMonitors() {
    final var processPathMonitors = new ArrayList<ProcessPathMonitor>(2);
    processPathMonitors.add(new ProcessPathMonitor(
        ProcessPath.NON_TOT_MONO,
        10
    ));

    processPathMonitors.add(new ProcessPathMonitor(
        ProcessPath.TOT_MONO,
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
