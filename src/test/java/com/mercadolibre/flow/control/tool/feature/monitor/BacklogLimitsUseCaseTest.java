package com.mercadolibre.flow.control.tool.feature.monitor;

import static com.mercadolibre.flow.control.tool.util.TestUtils.LOGISTIC_CENTER_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.OutboundProcessName;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.ProcessingType;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.BacklogLimitsUseCase;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.BacklogLimit;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.ProcessLimit;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BacklogLimitsUseCaseTest {

  private static final Instant DATE_FROM = Instant.parse("2023-06-06T12:00:00Z");

  private static final Instant DATE_TO = Instant.parse("2023-06-06T13:00:00Z");

  private static final long PICKING_UPPER_LIMIT = 1000L;

  private static final long PICKING_LOWER_LIMIT = 200L;

  private static final long PACKING_UPPER_LIMIT = 500L;

  private static final long PACKING_LOWER_LIMIT = 100L;

  private static final long BATCH_SORTER_UPPER_LIMIT = 1000L;

  private static final long IGNORE_LIMIT = -1;

  @InjectMocks
  private BacklogLimitsUseCase backlogLimitsUseCase;

  @Mock
  private BacklogLimitsUseCase.GetBacklogLimitGateway backlogLimitGateway;

  private static Map<Instant, Map<OutboundProcessName, Map<ProcessingType, Long>>> getBacklogLimitsEntityDataMapExpected() {
    return Map.of(
        DATE_FROM,
        Map.of(
            OutboundProcessName.PICKING,
            Map.of(
                ProcessingType.BACKLOG_LOWER_LIMIT,
                PICKING_LOWER_LIMIT,
                ProcessingType.BACKLOG_UPPER_LIMIT,
                PICKING_UPPER_LIMIT
            ),
            OutboundProcessName.PACKING,
            Map.of(
                ProcessingType.BACKLOG_LOWER_LIMIT,
                PACKING_LOWER_LIMIT,
                ProcessingType.BACKLOG_UPPER_LIMIT,
                PACKING_UPPER_LIMIT
            ),
            OutboundProcessName.BATCH_SORTER,
            Map.of(
                ProcessingType.BACKLOG_LOWER_LIMIT,
                IGNORE_LIMIT,
                ProcessingType.BACKLOG_UPPER_LIMIT,
                IGNORE_LIMIT
            )
        ),
        DATE_TO,
        Map.of(
            OutboundProcessName.PICKING,
            Map.of(
                ProcessingType.BACKLOG_LOWER_LIMIT,
                PICKING_LOWER_LIMIT,
                ProcessingType.BACKLOG_UPPER_LIMIT,
                PICKING_UPPER_LIMIT
            ),
            OutboundProcessName.PACKING,
            Map.of(
                ProcessingType.BACKLOG_LOWER_LIMIT,
                PACKING_LOWER_LIMIT,
                ProcessingType.BACKLOG_UPPER_LIMIT,
                PACKING_UPPER_LIMIT
            ),
            OutboundProcessName.BATCH_SORTER,
            Map.of(
                ProcessingType.BACKLOG_LOWER_LIMIT,
                IGNORE_LIMIT,
                ProcessingType.BACKLOG_UPPER_LIMIT,
                BATCH_SORTER_UPPER_LIMIT
            )
        )
    );
  }

  private static List<BacklogLimit> getExpectedBacklogLimits() {
    final ProcessLimit limitPacking = new ProcessLimit(ProcessName.PACKING, PACKING_LOWER_LIMIT, PACKING_UPPER_LIMIT);
    final ProcessLimit limitPicking = new ProcessLimit(ProcessName.PICKING, PICKING_LOWER_LIMIT, PICKING_UPPER_LIMIT);
    final ProcessLimit limitBatchSorter = new ProcessLimit(ProcessName.BATCH_SORTER, null, BATCH_SORTER_UPPER_LIMIT);
    final List<ProcessLimit> processLimitsDateFrom = List.of(limitPicking, limitPacking);
    final List<ProcessLimit> processLimitsDateTo = List.of(limitPicking, limitBatchSorter, limitPacking);

    final BacklogLimit backlogLimitDateFrom = new BacklogLimit(DATE_FROM, processLimitsDateFrom);
    final BacklogLimit backlogLimitDateTo = new BacklogLimit(DATE_TO, processLimitsDateTo);

    return List.of(backlogLimitDateFrom, backlogLimitDateTo);
  }

  @Test
  void executeTest() {

    // GIVEN
    final var backlogLimitsEntityDataMap = getBacklogLimitsEntityDataMapExpected();

    final var expectedBacklogLimits = getExpectedBacklogLimits();

    when(backlogLimitGateway.getBacklogLimitsEntityDataMap(
        LOGISTIC_CENTER_ID,
        Workflow.FBM_WMS_OUTBOUND,
        Set.of(ProcessName.PICKING, ProcessName.PACKING),
        DATE_FROM,
        DATE_TO
    )).thenReturn(backlogLimitsEntityDataMap);

    //WHEN
    final List<BacklogLimit> backlogLimits = backlogLimitsUseCase.execute(
        LOGISTIC_CENTER_ID,
        Workflow.FBM_WMS_OUTBOUND,
        Set.of(ProcessName.PICKING, ProcessName.PACKING),
        DATE_FROM,
        DATE_TO
    );

    //THEN
    assertEquals(expectedBacklogLimits, backlogLimits);

  }
}
