package com.mercadolibre.flow.control.tool.client;

import static com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName.NON_TOT_MONO;
import static com.mercadolibre.flow.control.tool.feature.entity.Workflow.FBM_WMS_OUTBOUND;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.mercadolibre.flow.control.tool.client.planningmodelapi.PlanningModelApiClient;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.PlanningModelApiClient.Throughput;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.adapter.PlannedEntitiesAdapter;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.OutboundProcessName;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.BacklogPlannedRequest;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.BacklogPlannedResponse;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.domain.PlannedBacklog;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
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
class PlannedEntitiesAdapterTest {

  private static final Workflow WORKFLOW = FBM_WMS_OUTBOUND;
  private static final String LOGISTIC_CENTER_ID = "ARBA01";
  private static final Throughput THROUGHPUT_PACKING = new Throughput(200);
  private static final Throughput THROUGHPUT_PICKING = new Throughput(100);
  private static final Instant SLA_1 = Instant.parse("2023-08-08T10:00:00Z");
  private static final Instant SLA_2 = Instant.parse("2023-08-08T11:00:00Z");
  private static final Instant DATE_FROM = Instant.parse("2023-05-18T08:00:00Z");
  private static final Instant DATE_TO = Instant.parse("2023-05-18T10:00:00Z");
  private static final Set<ProcessName> PROCESSES = Set.of(ProcessName.PICKING, ProcessName.PACKING);

  @InjectMocks
  private PlannedEntitiesAdapter plannedEntitiesAdapter;

  @Mock
  private PlanningModelApiClient planningModelApiClient;

  @ParameterizedTest
  @MethodSource("provideThroughputData")
  void testGetThroughput(
      final Map<ProcessPathName, Map<OutboundProcessName, Map<Instant, PlanningModelApiClient.Throughput>>> throughputData,
      final Map<Instant, Map<ProcessName, Integer>> throughputExpected
  ) {
    when(planningModelApiClient.getThroughputByPPAndProcessAndDate(
        FBM_WMS_OUTBOUND,
        LOGISTIC_CENTER_ID,
        SLA_1,
        SLA_2,
        PROCESSES,
        Set.of(ProcessPathName.GLOBAL))).thenReturn(throughputData);

    final Map<Instant, Map<ProcessName, Integer>> result = plannedEntitiesAdapter.getThroughput(
        WORKFLOW, LOGISTIC_CENTER_ID, SLA_1, SLA_2, PROCESSES);

    assertEquals(throughputExpected.size(), result.size());
    assertEquals(throughputExpected, result);
  }

  private static Stream<Arguments> provideThroughputData() {
    return Stream.of(
        Arguments.of(
            mockMetadata(),
            tphExpected()
        ),
        Arguments.of(
            mockIsNullMetadata(),
            emptyMap()
        )
    );
  }

  private static Map<ProcessPathName, Map<OutboundProcessName, Map<Instant, Throughput>>> mockMetadata() {
    return Map.of(
        ProcessPathName.GLOBAL,
        Map.of(
            OutboundProcessName.PACKING, Map.of(SLA_2, THROUGHPUT_PACKING),
            OutboundProcessName.PICKING, Map.of(SLA_1, THROUGHPUT_PICKING)
        )
    );
  }

  private static Map<ProcessPathName, Map<OutboundProcessName, Map<Instant, Throughput>>> mockIsNullMetadata() {
    return Map.of(
        ProcessPathName.GLOBAL,
        emptyMap()
    );
  }

  private static Map<Instant, Map<ProcessName, Integer>> tphExpected() {
    return Map.of(
        SLA_1, Map.of(ProcessName.PICKING, 100),
        SLA_2, Map.of(ProcessName.PACKING, 200));
  }

  @Test
  void testBacklogPlanUnitAdapter() {
    //GIVEN
    final int hours = (int) HOURS.between(DATE_FROM, DATE_TO);

    final List<BacklogPlannedResponse> plannedMock = IntStream.rangeClosed(0, hours)
        .mapToObj(hour -> new BacklogPlannedResponse(
            new BacklogPlannedResponse.GroupKey(
                NON_TOT_MONO,
                DATE_FROM.plus(hour, HOURS),
                DATE_TO.plus(hour, HOURS)),
            50.55D
        )).toList();

    final List<PlannedBacklog> expectedPlanUnits = IntStream.rangeClosed(0, hours)
        .mapToObj(hour -> new PlannedBacklog(
            NON_TOT_MONO,
            DATE_FROM.plus(hour, HOURS),
            DATE_TO.plus(hour, HOURS),
            51
        )).toList();

    when(planningModelApiClient.getBacklogPlanned(any(BacklogPlannedRequest.class))).thenReturn(plannedMock);

    //WHEN
    final List<PlannedBacklog> planUnits = plannedEntitiesAdapter.getPlannedBacklog(FBM_WMS_OUTBOUND,
                                                                                     LOGISTIC_CENTER_ID,
                                                                                     DATE_FROM,
                                                                                     DATE_TO);
    //THEN
    assertFalse(planUnits.isEmpty());
    assertEquals(expectedPlanUnits, planUnits);
  }

}
