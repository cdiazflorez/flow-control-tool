package com.mercadolibre.flow.control.tool.feature.status.usecase;

import static com.mercadolibre.flow.control.tool.feature.status.StatusTestUtils.mockAllProcessesSet;
import static com.mercadolibre.flow.control.tool.feature.status.StatusTestUtils.mockTwoProcessesSet;
import static com.mercadolibre.flow.control.tool.util.TestUtils.LOGISTIC_CENTER_ID;
import static com.mercadolibre.flow.control.tool.util.TestUtils.VIEW_DATE_INSTANT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.mercadolibre.flow.control.tool.exception.NoForecastMetadataFoundException;
import com.mercadolibre.flow.control.tool.feature.backlog.status.BacklogStatus;
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
    final BacklogStatus backlogByProcess = backlogStatusUseCase.getBacklogStatus(
        LOGISTIC_CENTER_ID,
        Workflow.FBM_WMS_OUTBOUND,
        ValueType.UNITS,
        processes,
        VIEW_DATE_INSTANT
    );

    // THEN
    assertEquals(10, backlogByProcess.backlogStatus().get(ProcessName.WAVING.getName()));
    assertEquals(10, backlogByProcess.backlogStatus().get(ProcessName.PICKING.getName()));
    assertEquals(10, backlogByProcess.backlogStatus().get(ProcessName.BATCH_SORTER.getName()));
    assertEquals(10, backlogByProcess.backlogStatus().get(ProcessName.WALL_IN.getName()));
    assertEquals(10, backlogByProcess.backlogStatus().get(ProcessName.PACKING.getName()));
    assertEquals(10, backlogByProcess.backlogStatus().get(ProcessName.PACKING_WALL.getName()));
    assertEquals(10, backlogByProcess.backlogStatus().get(ProcessName.HU_ASSEMBLY.getName()));
    assertEquals(10, backlogByProcess.backlogStatus().get(ProcessName.SHIPPING.getName()));
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
    final BacklogStatus backlogByProcess = backlogStatusUseCase.getBacklogStatus(
        LOGISTIC_CENTER_ID,
        Workflow.FBM_WMS_OUTBOUND,
        ValueType.UNITS,
        processes,
        VIEW_DATE_INSTANT
    );

    // THEN
    assertEquals(20, backlogByProcess.backlogStatus().get(ProcessName.WAVING.getName()));
    assertEquals(20, backlogByProcess.backlogStatus().get(ProcessName.PICKING.getName()));
    assertEquals(0, backlogByProcess.backlogStatus().get(ProcessName.BATCH_SORTER.getName()));
    assertEquals(0, backlogByProcess.backlogStatus().get(ProcessName.WALL_IN.getName()));
    assertEquals(0, backlogByProcess.backlogStatus().get(ProcessName.PACKING.getName()));
    assertEquals(0, backlogByProcess.backlogStatus().get(ProcessName.PACKING_WALL.getName()));
    assertEquals(0, backlogByProcess.backlogStatus().get(ProcessName.HU_ASSEMBLY.getName()));
    assertEquals(0, backlogByProcess.backlogStatus().get(ProcessName.SHIPPING.getName()));
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
    final BacklogStatus backlogByProcess = backlogStatusUseCase.getBacklogStatus(
        LOGISTIC_CENTER_ID,
        Workflow.FBM_WMS_OUTBOUND,
        ValueType.UNITS,
        processes,
        VIEW_DATE_INSTANT
    );

    // THEN
    assertEquals(5, backlogByProcess.backlogStatus().get(ProcessName.WAVING.getName()));
    assertEquals(0, backlogByProcess.backlogStatus().get(ProcessName.PICKING.getName()));
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
    final BacklogStatus backlogByProcess = backlogStatusUseCase.getBacklogStatus(
        LOGISTIC_CENTER_ID,
        Workflow.FBM_WMS_OUTBOUND,
        ValueType.ORDERS,
        processes,
        VIEW_DATE_INSTANT
    );

    // THEN
    assertEquals(2, backlogByProcess.backlogStatus().get(ProcessName.WAVING.getName()));
    assertEquals(2, backlogByProcess.backlogStatus().get(ProcessName.PICKING.getName()));
    assertEquals(2, backlogByProcess.backlogStatus().get(ProcessName.BATCH_SORTER.getName()));
    assertEquals(2, backlogByProcess.backlogStatus().get(ProcessName.WALL_IN.getName()));
    assertEquals(2, backlogByProcess.backlogStatus().get(ProcessName.PACKING.getName()));
    assertEquals(2, backlogByProcess.backlogStatus().get(ProcessName.PACKING_WALL.getName()));
    assertEquals(2, backlogByProcess.backlogStatus().get(ProcessName.HU_ASSEMBLY.getName()));
    assertEquals(2, backlogByProcess.backlogStatus().get(ProcessName.SHIPPING.getName()));
  }

  @Test
  void testGetBacklogTotalsByAllProcessOrdersOne() {
    // GIVEN
    final Set<ProcessName> processes = mockAllProcessesSet();
    when(backlogGateway.getBacklogTotalsByProcess(
        LOGISTIC_CENTER_ID,
        Workflow.FBM_WMS_OUTBOUND,
        processes,
        VIEW_DATE_INSTANT
    )).thenReturn(Arrays.stream(ProcessName.values())
        .collect(Collectors.toMap(Function.identity(), value -> 40))
    );

    when(unitsPerOrderRatioGateway
        .getUnitsPerOrderRatio(Workflow.FBM_WMS_OUTBOUND, LOGISTIC_CENTER_ID, VIEW_DATE_INSTANT))
        .thenReturn(Optional.of(1.0));

    // WHEN
    final BacklogStatus backlogByProcess = backlogStatusUseCase.getBacklogStatus(
        LOGISTIC_CENTER_ID,
        Workflow.FBM_WMS_OUTBOUND,
        ValueType.ORDERS,
        processes,
        VIEW_DATE_INSTANT
    );

    // THEN
    assertEquals(40, backlogByProcess.backlogStatus().get(ProcessName.WAVING.getName()));
    assertEquals(40, backlogByProcess.backlogStatus().get(ProcessName.PICKING.getName()));
    assertEquals(40, backlogByProcess.backlogStatus().get(ProcessName.BATCH_SORTER.getName()));
    assertEquals(40, backlogByProcess.backlogStatus().get(ProcessName.WALL_IN.getName()));
    assertEquals(40, backlogByProcess.backlogStatus().get(ProcessName.PACKING.getName()));
    assertEquals(40, backlogByProcess.backlogStatus().get(ProcessName.PACKING_WALL.getName()));
    assertEquals(40, backlogByProcess.backlogStatus().get(ProcessName.HU_ASSEMBLY.getName()));
    assertEquals(40, backlogByProcess.backlogStatus().get(ProcessName.SHIPPING.getName()));
  }

  @Test
  void testGetBacklogTotalsByAllProcessOrdersZero() {
    // GIVEN
    final Set<ProcessName> processes = mockAllProcessesSet();

    when(unitsPerOrderRatioGateway
        .getUnitsPerOrderRatio(Workflow.FBM_WMS_OUTBOUND, LOGISTIC_CENTER_ID, VIEW_DATE_INSTANT))
        .thenReturn(Optional.of(0.80));

    // THEN
    assertThrows(
        NoForecastMetadataFoundException.class, () ->
            backlogStatusUseCase
                .getBacklogStatus(
                    LOGISTIC_CENTER_ID,
                    Workflow.FBM_WMS_OUTBOUND,
                    ValueType.ORDERS,
                    processes,
                    VIEW_DATE_INSTANT
                )
    );
  }

  @Test
  public void testNoForecastMetadataFoundException() {
    // GIVEN
    final Set<ProcessName> processes = mockAllProcessesSet();

    // WHEN and THEN
    assertThrows(
        NoForecastMetadataFoundException.class, () ->
            backlogStatusUseCase
                .getBacklogStatus(
                    LOGISTIC_CENTER_ID,
                    Workflow.FBM_WMS_OUTBOUND,
                    ValueType.ORDERS,
                    processes,
                    VIEW_DATE_INSTANT
                )
    );
  }
}
