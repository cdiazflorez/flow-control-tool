package com.mercadolibre.flow.control.tool.feature.status.usecase;

import static com.mercadolibre.flow.control.tool.feature.status.StatusTestUtils.mockAllProcessesSet;
import static com.mercadolibre.flow.control.tool.feature.status.StatusTestUtils.mockTwoProcessesSet;
import static com.mercadolibre.flow.control.tool.util.TestUtils.LOGISTIC_CENTER_ID;
import static com.mercadolibre.flow.control.tool.util.TestUtils.VIEW_DATE_INSTANT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.mercadolibre.flow.control.tool.feature.status.usecase.BacklogStatusUseCase.BacklogGateway;
import com.mercadolibre.flow.control.tool.feature.status.usecase.BacklogStatusUseCase.UnitsPerOrderRatioGateway;
import com.mercadolibre.flow.control.tool.feature.status.usecase.constant.Processes;
import com.mercadolibre.flow.control.tool.feature.status.usecase.constant.ValueType;
import com.mercadolibre.flow.control.tool.feature.status.usecase.constant.Workflow;
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
    final Set<Processes> processes = mockAllProcessesSet();
    when(backlogGateway.getBacklogTotalsByProcess(
        LOGISTIC_CENTER_ID,
        Workflow.FBM_WMS_OUTBOUND,
        processes,
        VIEW_DATE_INSTANT
    )).thenReturn(Arrays.stream(Processes.values())
        .collect(Collectors.toMap(Function.identity(), value -> 10))
    );

    // WHEN
    final Map<Processes, Integer> backlogByProcess = backlogStatusUseCase.getBacklogTotalsByProcess(
        LOGISTIC_CENTER_ID,
        Workflow.FBM_WMS_OUTBOUND,
        ValueType.UNITS,
        processes,
        VIEW_DATE_INSTANT
    );

    // THEN
    assertEquals(10, backlogByProcess.get(Processes.WAVING));
    assertEquals(10, backlogByProcess.get(Processes.PICKING));
    assertEquals(10, backlogByProcess.get(Processes.BATCH_SORTER));
    assertEquals(10, backlogByProcess.get(Processes.WALL_IN));
    assertEquals(10, backlogByProcess.get(Processes.PACKING));
    assertEquals(10, backlogByProcess.get(Processes.PACKING_WALL));
    assertEquals(10, backlogByProcess.get(Processes.HU_ASSEMBLY));
    assertEquals(10, backlogByProcess.get(Processes.SHIPPED));
  }

  @Test
  void testGetBacklogTotalsByProcessWhenNoProcessStepsValues() {
    // GIVEN
    final Set<Processes> processes = mockAllProcessesSet();
    when(backlogGateway.getBacklogTotalsByProcess(
        LOGISTIC_CENTER_ID,
        Workflow.FBM_WMS_OUTBOUND,
        processes,
        VIEW_DATE_INSTANT
    )).thenReturn(Map.of(Processes.WAVING, 20, Processes.PICKING, 20));

    // WHEN
    final Map<Processes, Integer> backlogByProcess = backlogStatusUseCase.getBacklogTotalsByProcess(
        LOGISTIC_CENTER_ID,
        Workflow.FBM_WMS_OUTBOUND,
        ValueType.UNITS,
        processes,
        VIEW_DATE_INSTANT
    );

    // THEN
    assertEquals(20, backlogByProcess.get(Processes.WAVING));
    assertEquals(20, backlogByProcess.get(Processes.PICKING));
    assertEquals(0, backlogByProcess.get(Processes.BATCH_SORTER));
    assertEquals(0, backlogByProcess.get(Processes.WALL_IN));
    assertEquals(0, backlogByProcess.get(Processes.PACKING));
    assertEquals(0, backlogByProcess.get(Processes.PACKING_WALL));
    assertEquals(0, backlogByProcess.get(Processes.HU_ASSEMBLY));
    assertEquals(0, backlogByProcess.get(Processes.SHIPPED));
  }

  @Test
  void testGetBacklogTotalsByProcessWhenTwoProcessRequested() {
    // GIVEN
    final Set<Processes> processes = mockTwoProcessesSet();
    when(backlogGateway.getBacklogTotalsByProcess(
        LOGISTIC_CENTER_ID,
        Workflow.FBM_WMS_OUTBOUND,
        processes,
        VIEW_DATE_INSTANT
    )).thenReturn(Map.of(Processes.WAVING, 5));

    // WHEN
    final Map<Processes, Integer> backlogByProcess = backlogStatusUseCase.getBacklogTotalsByProcess(
        LOGISTIC_CENTER_ID,
        Workflow.FBM_WMS_OUTBOUND,
        ValueType.UNITS,
        processes,
        VIEW_DATE_INSTANT
    );

    // THEN
    assertEquals(5, backlogByProcess.get(Processes.WAVING));
    assertEquals(0, backlogByProcess.get(Processes.PICKING));
  }

  @Test
  void testGetBacklogTotalsByAllProcessOrders() {
    // GIVEN
    final Set<Processes> processes = mockAllProcessesSet();
    when(backlogGateway.getBacklogTotalsByProcess(
        LOGISTIC_CENTER_ID,
        Workflow.FBM_WMS_OUTBOUND,
        processes,
        VIEW_DATE_INSTANT
    )).thenReturn(Arrays.stream(Processes.values())
        .collect(Collectors.toMap(Function.identity(), value -> 10))
    );

    when(unitsPerOrderRatioGateway
        .getUnitsPerOrderRatio(Workflow.FBM_WMS_OUTBOUND, LOGISTIC_CENTER_ID, VIEW_DATE_INSTANT))
        .thenReturn(Optional.of(3.96));

    // WHEN
    final Map<Processes, Integer> backlogByProcess = backlogStatusUseCase.getBacklogTotalsByProcess(
        LOGISTIC_CENTER_ID,
        Workflow.FBM_WMS_OUTBOUND,
        ValueType.ORDERS,
        processes,
        VIEW_DATE_INSTANT
    );

    // THEN
    assertEquals(2, backlogByProcess.get(Processes.WAVING));
    assertEquals(2, backlogByProcess.get(Processes.PICKING));
    assertEquals(2, backlogByProcess.get(Processes.BATCH_SORTER));
    assertEquals(2, backlogByProcess.get(Processes.WALL_IN));
    assertEquals(2, backlogByProcess.get(Processes.PACKING));
    assertEquals(2, backlogByProcess.get(Processes.PACKING_WALL));
    assertEquals(2, backlogByProcess.get(Processes.HU_ASSEMBLY));
    assertEquals(2, backlogByProcess.get(Processes.SHIPPED));
  }

  @Test
  void testGetBacklogTotalsByAllProcessOrdersNull() {
    // GIVEN
    final Set<Processes> processes = mockAllProcessesSet();
    when(backlogGateway.getBacklogTotalsByProcess(
        LOGISTIC_CENTER_ID,
        Workflow.FBM_WMS_OUTBOUND,
        processes,
        VIEW_DATE_INSTANT
    )).thenReturn(Arrays.stream(Processes.values())
        .collect(Collectors.toMap(Function.identity(), value -> 30))
    );

    when(unitsPerOrderRatioGateway
        .getUnitsPerOrderRatio(Workflow.FBM_WMS_OUTBOUND, LOGISTIC_CENTER_ID, VIEW_DATE_INSTANT))
        .thenReturn(Optional.of(1.0));

    // WHEN
    final Map<Processes, Integer> backlogByProcess = backlogStatusUseCase.getBacklogTotalsByProcess(
        LOGISTIC_CENTER_ID,
        Workflow.FBM_WMS_OUTBOUND,
        ValueType.ORDERS,
        processes,
        VIEW_DATE_INSTANT
    );

    // THEN
    assertEquals(30, backlogByProcess.get(Processes.WAVING));
    assertEquals(30, backlogByProcess.get(Processes.PICKING));
    assertEquals(30, backlogByProcess.get(Processes.BATCH_SORTER));
    assertEquals(30, backlogByProcess.get(Processes.WALL_IN));
    assertEquals(30, backlogByProcess.get(Processes.PACKING));
    assertEquals(30, backlogByProcess.get(Processes.PACKING_WALL));
    assertEquals(30, backlogByProcess.get(Processes.HU_ASSEMBLY));
    assertEquals(30, backlogByProcess.get(Processes.SHIPPED));
  }
}
