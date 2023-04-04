package com.mercadolibre.flow.control.tool.feature.staffing;

import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.PICKING;
import static com.mercadolibre.flow.control.tool.feature.entity.Workflow.FBM_WMS_OUTBOUND;
import static com.mercadolibre.flow.control.tool.feature.staffing.constant.StaffingType.HEADCOUNT;
import static com.mercadolibre.flow.control.tool.feature.staffing.constant.StaffingType.PRODUCTIVITY;
import static com.mercadolibre.flow.control.tool.feature.staffing.constant.StaffingType.THROUGHPUT;
import static com.mercadolibre.flow.control.tool.util.TestUtils.DATE_FROM;
import static com.mercadolibre.flow.control.tool.util.TestUtils.DATE_TO;
import static com.mercadolibre.flow.control.tool.util.TestUtils.LOGISTIC_CENTER_ID;
import static java.time.temporal.ChronoUnit.HOURS;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.staffing.constant.StaffingType;
import com.mercadolibre.flow.control.tool.feature.staffing.domain.StaffingOperation;
import com.mercadolibre.flow.control.tool.feature.staffing.domain.StaffingPlannedData;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StaffingPlanUseCaseTest {

  private static final Map<StaffingType, Long> PLANNED_VALUE_BY_STAFFING_TYPE = Map.of(
      HEADCOUNT, 10L,
      PRODUCTIVITY, 100L,
      THROUGHPUT, 1000L
  );

  @Mock
  private StaffingPlanUseCase.StaffingPlanGateway staffingPlanGateway;

  @InjectMocks
  private StaffingPlanUseCase staffingPlanUseCase;

  @Test
  @DisplayName("Gets planned staffing and organizes it for display in the operational plan")
  void testGetStaffingOperationOk() {
    //GIVEN
    when(staffingPlanGateway.getStaffingPlanned(FBM_WMS_OUTBOUND, LOGISTIC_CENTER_ID, DATE_FROM, DATE_TO))
        .thenReturn(mockStaffingPlanned());

    List<Instant> expectedDates = IntStream.rangeClosed(0, 6)
        .mapToObj(i -> DATE_FROM.plus(i, HOURS))
        .toList();

    //WHEN
    final StaffingOperation staffingOperation = staffingPlanUseCase.getStaffing(
        LOGISTIC_CENTER_ID,
        FBM_WMS_OUTBOUND,
        DATE_FROM,
        DATE_TO
    );
    //THEN
    final var staffingOperationValuesByProcessNamesAndStaffingType = staffingOperation.values();

    for (StaffingType value : StaffingType.values()) {
      assertNotNull(staffingOperationValuesByProcessNamesAndStaffingType.getOrDefault(value, null));
      final var staffingOperationDataByProcessNames = staffingOperationValuesByProcessNamesAndStaffingType
          .getOrDefault(value, Map.of());
      for (ProcessName staffingProcessName : ProcessName.values()) {
        assertNotNull(staffingOperationDataByProcessNames.getOrDefault(staffingProcessName, null));
      }
    }

    final var pickingHeadcountData = staffingOperationValuesByProcessNamesAndStaffingType
        .get(HEADCOUNT)
        .get(PICKING);
    assertEquals(70L, pickingHeadcountData.staffingOperationTotal().getPlannedSystemic());
    assertEquals(35L, pickingHeadcountData.staffingOperationTotal().getPlannedNonSystemic());
    pickingHeadcountData.staffingOperationValues().forEach(
        staffingOperationData -> assertAll(
            () -> assertTrue(expectedDates.contains(staffingOperationData.getDate())),
            () -> assertEquals(10L, staffingOperationData.getPlannedSystemic()),
            () -> assertEquals(5L, staffingOperationData.getPlannedNonSystemic()),
            () -> assertFalse(staffingOperationData.getPlannedSystemicEdited()),
            () -> assertFalse(staffingOperationData.getPlannedNonSystemicEdited())
        )
    );

    final var pickingProductivityData = staffingOperationValuesByProcessNamesAndStaffingType
        .get(PRODUCTIVITY)
        .get(PICKING);
    assertEquals(100L, pickingProductivityData.staffingOperationTotal().getPlanned());
    pickingProductivityData.staffingOperationValues().forEach(
        staffingOperationData -> assertAll(
            () -> assertTrue(expectedDates.contains(staffingOperationData.getDate())),
            () -> assertEquals(100L, staffingOperationData.getPlanned()),
            () -> assertFalse(staffingOperationData.getPlannedEdited())
        )
    );

    final var pickingThroughputData = staffingOperationValuesByProcessNamesAndStaffingType
        .get(THROUGHPUT)
        .get(PICKING);
    assertEquals(7000L, pickingThroughputData.staffingOperationTotal().getPlanned());
    pickingThroughputData.staffingOperationValues().forEach(
        staffingOperationData -> assertAll(
            () -> assertTrue(expectedDates.contains(staffingOperationData.getDate())),
            () -> assertEquals(1000L, staffingOperationData.getPlanned())
        )
    );

  }

  private Map<StaffingType, List<StaffingPlannedData>> mockStaffingPlanned() {

    return Arrays.stream(StaffingType.values())
        .collect(
            Collectors.toMap(
                k -> k,
                v -> Arrays.stream(ProcessName.values())
                    .flatMap(process -> IntStream.rangeClosed(0, 6)
                        .mapToObj(iterator -> new StaffingPlannedData(
                            DATE_FROM.plus(iterator, HOURS),
                            process,
                            PLANNED_VALUE_BY_STAFFING_TYPE.get(v),
                            v.equals(HEADCOUNT) ? 5L : 0L,
                            false,
                            false
                        ))
                    ).toList()
            )
        );
  }


}
