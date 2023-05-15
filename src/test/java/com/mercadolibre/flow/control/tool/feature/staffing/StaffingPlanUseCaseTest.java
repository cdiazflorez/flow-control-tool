package com.mercadolibre.flow.control.tool.feature.staffing;

import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.BATCH_SORTER;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.HU_ASSEMBLY;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.PACKING;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.PACKING_WALL;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.PICKING;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.SHIPPING;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.WALL_IN;
import static com.mercadolibre.flow.control.tool.feature.entity.Workflow.FBM_WMS_OUTBOUND;
import static com.mercadolibre.flow.control.tool.feature.staffing.constant.StaffingMetricType.HEADCOUNT;
import static com.mercadolibre.flow.control.tool.feature.staffing.constant.StaffingMetricType.PRODUCTIVITY;
import static com.mercadolibre.flow.control.tool.feature.staffing.constant.StaffingMetricType.THROUGHPUT;
import static com.mercadolibre.flow.control.tool.util.TestUtils.DATE_FROM;
import static com.mercadolibre.flow.control.tool.util.TestUtils.DATE_TO;
import static com.mercadolibre.flow.control.tool.util.TestUtils.LOGISTIC_CENTER_ID;
import static java.time.temporal.ChronoUnit.HOURS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.staffing.constant.StaffingMetricType;
import com.mercadolibre.flow.control.tool.feature.staffing.domain.MetricData;
import com.mercadolibre.flow.control.tool.feature.staffing.domain.StaffingOperation;
import com.mercadolibre.flow.control.tool.feature.staffing.domain.StaffingOperationValues;
import com.mercadolibre.flow.control.tool.feature.staffing.domain.StaffingPlannedData;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
class StaffingPlanUseCaseTest {

  private static final List<Instant> EXPECTED_DATES = IntStream.rangeClosed(0, 6)
      .mapToObj(i -> DATE_FROM.plus(i, HOURS))
      .toList();

  private static final Map<StaffingMetricType, Long> PLANNED_VALUE_BY_STAFFING_TYPE = Map.of(
      HEADCOUNT, 10L,
      PRODUCTIVITY, 100L,
      THROUGHPUT, 1000L
  );

  private static final List<ProcessName> PROCESS_NAMES = List.of(
      PICKING,
      BATCH_SORTER,
      WALL_IN,
      PACKING,
      PACKING_WALL,
      HU_ASSEMBLY,
      SHIPPING
  );

  @Mock
  private StaffingPlanUseCase.StaffingPlanGateway staffingPlanGateway;

  @Mock
  private StaffingPlanUseCase.StaffingMetricGateway staffingMetricGateway;

  @InjectMocks
  private StaffingPlanUseCase staffingPlanUseCase;

  @ParameterizedTest
  @MethodSource("provideMocksAndExpectedValues")
  @DisplayName("Gets planned staffing and organizes it for display in the operational plan")
  void testGetStaffingOperationOk(final Map<StaffingMetricType, List<StaffingPlannedData>> mockStaffingPlanned,
                                  final List<MetricData> mockMetricsData,
                                  final Instant viewDate) {
    //GIVEN
    when(staffingPlanGateway.getCurrentStaffing(FBM_WMS_OUTBOUND, LOGISTIC_CENTER_ID, DATE_FROM, DATE_TO))
        .thenReturn(mockStaffingPlanned);

    if (!mockMetricsData.isEmpty()) {
      when(staffingMetricGateway.getMetrics(LOGISTIC_CENTER_ID, FBM_WMS_OUTBOUND, DATE_FROM, viewDate))
          .thenReturn(mockMetricsData);
    }

    //WHEN
    final StaffingOperation staffingOperation = staffingPlanUseCase.getStaffing(
        LOGISTIC_CENTER_ID,
        FBM_WMS_OUTBOUND,
        DATE_FROM,
        DATE_TO,
        viewDate
    );

    //THEN
    final var staffingOperationValuesByProcessNamesAndStaffingType = staffingOperation.values();

    for (StaffingMetricType value : StaffingMetricType.values()) {
      assertNotNull(staffingOperationValuesByProcessNamesAndStaffingType.getOrDefault(value, null));
      final var staffingOperationDataByProcessNames = staffingOperationValuesByProcessNamesAndStaffingType
          .getOrDefault(value, Map.of());
      PROCESS_NAMES.forEach(
          processName -> assertNotNull(staffingOperationDataByProcessNames.getOrDefault(processName, null))
      );
    }

    assertHeadcount(
        staffingOperationValuesByProcessNamesAndStaffingType
            .get(HEADCOUNT)
            .get(PICKING),
        viewDate);

    assertProductivity(
        staffingOperationValuesByProcessNamesAndStaffingType
            .get(PRODUCTIVITY)
            .get(PICKING),
        viewDate
    );

    assertThroughput(
        staffingOperationValuesByProcessNamesAndStaffingType
            .get(THROUGHPUT)
            .get(PICKING),
        viewDate
    );

    verify(staffingPlanGateway, times(1)).getCurrentStaffing(FBM_WMS_OUTBOUND, LOGISTIC_CENTER_ID, DATE_FROM, DATE_TO);
    if (mockMetricsData.isEmpty()) {
      verifyNoInteractions(staffingMetricGateway);
    } else {
      verify(staffingMetricGateway, times(1)).getMetrics(LOGISTIC_CENTER_ID, FBM_WMS_OUTBOUND, DATE_FROM, viewDate);
    }

  }

  private static Stream<Arguments> provideMocksAndExpectedValues() {
    return Stream.of(
        Arguments.of(
            mockStaffingPlanned(),
            mockAllMetricsData(),
            DATE_TO
        ),
        Arguments.of(
            mockStaffingPlanned(),
            mockPartialMetricsData(),
            DATE_FROM.plus(3, HOURS)
        ),
        Arguments.of(
            mockStaffingPlanned(),
            List.of(),
            DATE_FROM.minus(1, HOURS)
        )
    );
  }

  private static Map<StaffingMetricType, List<StaffingPlannedData>> mockStaffingPlanned() {

    return Arrays.stream(StaffingMetricType.values())
        .collect(
            Collectors.toMap(
                k -> k,
                v -> PROCESS_NAMES.stream()
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

  private static List<MetricData> mockAllMetricsData() {
    return PROCESS_NAMES.stream()
        .flatMap(process -> IntStream.rangeClosed(0, 6)
            .mapToObj(iterator -> new MetricData(
                          process,
                          DATE_FROM.plus(iterator, HOURS),
                          78L,
                          945L
                      )
            )
        ).toList();
  }

  private static List<MetricData> mockPartialMetricsData() {
    return PROCESS_NAMES.stream()
        .flatMap(process -> IntStream.rangeClosed(0, 3)
            .mapToObj(iterator -> new MetricData(
                          process,
                          DATE_FROM.plus(iterator, HOURS),
                          78L,
                          945L
                      )
            )
        ).toList();
  }

  private static void assertHeadcount(final StaffingOperationValues staffingOperationValues,
                                      final Instant viewDate) {

    assertEquals(70L, staffingOperationValues.staffingOperationTotal().getPlannedSystemic());
    assertEquals(35L, staffingOperationValues.staffingOperationTotal().getPlannedNonSystemic());

    if (viewDate.equals(DATE_TO)) {
      assertEquals(84L, staffingOperationValues.staffingOperationTotal().getPresentSystemic());
      assertEquals(42L, staffingOperationValues.staffingOperationTotal().getPresentNonSystemic());
    } else {
      assertNull(staffingOperationValues.staffingOperationTotal().getPresentSystemic());
      assertNull(staffingOperationValues.staffingOperationTotal().getPresentNonSystemic());
    }

    staffingOperationValues.staffingOperationValues().forEach(
        staffingOperationData -> {
          final Instant staffingDate = staffingOperationData.getDate();
          assertTrue(EXPECTED_DATES.contains(staffingDate));
          assertEquals(10L, staffingOperationData.getPlannedSystemic());
          assertEquals(5L, staffingOperationData.getPlannedNonSystemic());
          assertFalse(staffingOperationData.getPlannedSystemicEdited());
          assertFalse(staffingOperationData.getPlannedNonSystemicEdited());
          if (!viewDate.isBefore(staffingDate)) {
            assertEquals(12L, staffingOperationData.getPresentSystemic());
            assertEquals(6L, staffingOperationData.getPresentNonSystemic());
            assertEquals(-2L, staffingOperationData.getDeviationSystemic());
            assertEquals(-1L, staffingOperationData.getDeviationNonSystemic());
          } else {
            assertNull(staffingOperationData.getPresentSystemic());
            assertNull(staffingOperationData.getPresentNonSystemic());
            assertNull(staffingOperationData.getDeviationSystemic());
            assertNull(staffingOperationData.getDeviationNonSystemic());
          }
        }
    );
  }

  private static void assertProductivity(final StaffingOperationValues staffingOperationValues,
                                         final Instant viewDate) {

    assertEquals(100L, staffingOperationValues.staffingOperationTotal().getPlanned());

    if (viewDate.equals(DATE_TO)) {
      assertEquals(78L, staffingOperationValues.staffingOperationTotal().getReal());
    } else {
      assertNull(staffingOperationValues.staffingOperationTotal().getReal());
    }

    staffingOperationValues.staffingOperationValues().forEach(
        staffingOperationData -> {
          final Instant staffingDate = staffingOperationData.getDate();
          assertTrue(EXPECTED_DATES.contains(staffingOperationData.getDate()));
          assertEquals(100L, staffingOperationData.getPlanned());
          assertFalse(staffingOperationData.getPlannedEdited());
          if (!viewDate.isBefore(staffingDate)) {
            assertEquals(78L, staffingOperationData.getReal());
            assertEquals(22L, staffingOperationData.getDeviation());
          } else {
            assertNull(staffingOperationData.getReal());
            assertNull(staffingOperationData.getDeviation());
          }
        }
    );
  }

  private static void assertThroughput(final StaffingOperationValues staffingOperationValues,
                                       final Instant viewDate) {

    assertEquals(7000L, staffingOperationValues.staffingOperationTotal().getPlanned());

    if (viewDate.equals(DATE_TO)) {
      assertEquals(6615L, staffingOperationValues.staffingOperationTotal().getReal());
    } else {
      assertNull(staffingOperationValues.staffingOperationTotal().getReal());
    }

    staffingOperationValues.staffingOperationValues().forEach(
        staffingOperationData -> {
          final Instant staffingDate = staffingOperationData.getDate();
          assertTrue(EXPECTED_DATES.contains(staffingOperationData.getDate()));
          assertEquals(1000L, staffingOperationData.getPlanned());
          if (!viewDate.isBefore(staffingDate)) {
            assertEquals(945L, staffingOperationData.getReal());
            assertEquals(55L, staffingOperationData.getDeviation());
          } else {
            assertNull(staffingOperationData.getReal());
            assertNull(staffingOperationData.getDeviation());
          }
        }
    );
  }

}
