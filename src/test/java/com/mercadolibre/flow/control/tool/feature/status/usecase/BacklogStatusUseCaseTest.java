package com.mercadolibre.flow.control.tool.feature.status.usecase;

import static com.mercadolibre.flow.control.tool.feature.status.StatusTestUtils.mockAllProcessesSet;
import static com.mercadolibre.flow.control.tool.feature.status.StatusTestUtils.mockTwoProcessesSet;
import static com.mercadolibre.flow.control.tool.util.TestUtils.LOGISTIC_CENTER_ID;
import static com.mercadolibre.flow.control.tool.util.TestUtils.VIEW_DATE_INSTANT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.mercadolibre.flow.control.tool.feature.backlog.status.BacklogStatusUseCase;
import com.mercadolibre.flow.control.tool.feature.backlog.status.BacklogStatusUseCase.BacklogGateway;
import com.mercadolibre.flow.control.tool.feature.backlog.status.BacklogStatusUseCase.UnitsPerOrderRatioGateway;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.ValueType;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BacklogStatusUseCaseTest {


  @Mock
  private BacklogGateway backlogGateway;

  @Mock
  private UnitsPerOrderRatioGateway unitsPerOrderRatioGateway;

  @InjectMocks
  private BacklogStatusUseCase backlogStatusUseCase;

  @Test
  void testGetBacklogTotalsByAllProcess() {
    // GIVEN
    final Set<ProcessName> processes = mockAllProcessesSet();
    when(backlogGateway.getBacklogTotalsByProcess(
        LOGISTIC_CENTER_ID,
        Workflow.FBM_WMS_OUTBOUND,
        processes,
        VIEW_DATE_INSTANT
    )).thenReturn(Arrays.stream(ProcessName.values())
        .collect(Collectors.toMap(Function.identity(), value -> 10))
    );

    // WHEN
    final Map<ProcessName, Integer> backlogByProcess = backlogStatusUseCase.getBacklogTotalsByProcess(
        LOGISTIC_CENTER_ID,
        Workflow.FBM_WMS_OUTBOUND,
        ValueType.UNITS,
        processes,
        VIEW_DATE_INSTANT
    );

    // THEN
    assertEquals(10, backlogByProcess.get(ProcessName.WAVING));
    assertEquals(10, backlogByProcess.get(ProcessName.PICKING));
    assertEquals(10, backlogByProcess.get(ProcessName.BATCH_SORTER));
    assertEquals(10, backlogByProcess.get(ProcessName.WALL_IN));
    assertEquals(10, backlogByProcess.get(ProcessName.PACKING));
    assertEquals(10, backlogByProcess.get(ProcessName.PACKING_WALL));
    assertEquals(10, backlogByProcess.get(ProcessName.HU_ASSEMBLY));
    assertEquals(10, backlogByProcess.get(ProcessName.SHIPPED));
  }

  @Test
  void testGetBacklogTotalsByProcessWhenNoProcessStepsValues() {
    // GIVEN
    final Set<ProcessName> processes = mockAllProcessesSet();
    when(backlogGateway.getBacklogTotalsByProcess(
        LOGISTIC_CENTER_ID,
        Workflow.FBM_WMS_OUTBOUND,
        processes,
        VIEW_DATE_INSTANT
    )).thenReturn(Map.of(ProcessName.WAVING, 20, ProcessName.PICKING, 20));

    // WHEN
    final Map<ProcessName, Integer> backlogByProcess = backlogStatusUseCase.getBacklogTotalsByProcess(
        LOGISTIC_CENTER_ID,
        Workflow.FBM_WMS_OUTBOUND,
        ValueType.UNITS,
        processes,
        VIEW_DATE_INSTANT
    );

    // THEN
    assertEquals(20, backlogByProcess.get(ProcessName.WAVING));
    assertEquals(20, backlogByProcess.get(ProcessName.PICKING));
    assertEquals(0, backlogByProcess.get(ProcessName.BATCH_SORTER));
    assertEquals(0, backlogByProcess.get(ProcessName.WALL_IN));
    assertEquals(0, backlogByProcess.get(ProcessName.PACKING));
    assertEquals(0, backlogByProcess.get(ProcessName.PACKING_WALL));
    assertEquals(0, backlogByProcess.get(ProcessName.HU_ASSEMBLY));
    assertEquals(0, backlogByProcess.get(ProcessName.SHIPPED));
  }

  @Test
  void testGetBacklogTotalsByProcessWhenTwoProcessRequested() {
    // GIVEN
    final Set<ProcessName> processes = mockTwoProcessesSet();
    when(backlogGateway.getBacklogTotalsByProcess(
        LOGISTIC_CENTER_ID,
        Workflow.FBM_WMS_OUTBOUND,
        processes,
        VIEW_DATE_INSTANT
    )).thenReturn(Map.of(ProcessName.WAVING, 5));

    // WHEN
    final Map<ProcessName, Integer> backlogByProcess = backlogStatusUseCase.getBacklogTotalsByProcess(
        LOGISTIC_CENTER_ID,
        Workflow.FBM_WMS_OUTBOUND,
        ValueType.UNITS,
        processes,
        VIEW_DATE_INSTANT
    );

    // THEN
    assertEquals(5, backlogByProcess.get(ProcessName.WAVING));
    assertEquals(0, backlogByProcess.get(ProcessName.PICKING));
  }

  @Test
  void testGetBacklogTotalsByAllProcessOrders() {
    // GIVEN
    final Set<ProcessName> processes = mockAllProcessesSet();
    when(backlogGateway.getBacklogTotalsByProcess(
        LOGISTIC_CENTER_ID,
        Workflow.FBM_WMS_OUTBOUND,
        processes,
        VIEW_DATE_INSTANT
    )).thenReturn(Arrays.stream(ProcessName.values())
        .collect(Collectors.toMap(Function.identity(), value -> 10))
    );

    when(unitsPerOrderRatioGateway
        .getUnitsPerOrderRatio(Workflow.FBM_WMS_OUTBOUND, LOGISTIC_CENTER_ID, VIEW_DATE_INSTANT))
        .thenReturn(Optional.of(3.96));

    // WHEN
    final Map<ProcessName, Integer> backlogByProcess = backlogStatusUseCase.getBacklogTotalsByProcess(
        LOGISTIC_CENTER_ID,
        Workflow.FBM_WMS_OUTBOUND,
        ValueType.ORDERS,
        processes,
        VIEW_DATE_INSTANT
    );

    // THEN
    assertEquals(2, backlogByProcess.get(ProcessName.WAVING));
    assertEquals(2, backlogByProcess.get(ProcessName.PICKING));
    assertEquals(2, backlogByProcess.get(ProcessName.BATCH_SORTER));
    assertEquals(2, backlogByProcess.get(ProcessName.WALL_IN));
    assertEquals(2, backlogByProcess.get(ProcessName.PACKING));
    assertEquals(2, backlogByProcess.get(ProcessName.PACKING_WALL));
    assertEquals(2, backlogByProcess.get(ProcessName.HU_ASSEMBLY));
    assertEquals(2, backlogByProcess.get(ProcessName.SHIPPED));
  }

  @Test
  void testGetBacklogTotalsByAllProcessOrdersNull() {
    // GIVEN
    final Set<ProcessName> processes = mockAllProcessesSet();
    when(backlogGateway.getBacklogTotalsByProcess(
        LOGISTIC_CENTER_ID,
        Workflow.FBM_WMS_OUTBOUND,
        processes,
        VIEW_DATE_INSTANT
    )).thenReturn(Arrays.stream(ProcessName.values())
        .collect(Collectors.toMap(Function.identity(), value -> 30))
    );

    when(unitsPerOrderRatioGateway
        .getUnitsPerOrderRatio(Workflow.FBM_WMS_OUTBOUND, LOGISTIC_CENTER_ID, VIEW_DATE_INSTANT))
        .thenReturn(Optional.empty());

    // WHEN
    final Map<ProcessName, Integer> backlogByProcess = backlogStatusUseCase.getBacklogTotalsByProcess(
        LOGISTIC_CENTER_ID,
        Workflow.FBM_WMS_OUTBOUND,
        ValueType.ORDERS,
        processes,
        VIEW_DATE_INSTANT
    );

    // THEN
    assertEquals(Map.of(), backlogByProcess);
  }
}
