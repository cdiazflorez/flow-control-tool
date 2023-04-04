package com.mercadolibre.flow.control.tool.client.planningmodelapi;

import static com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.EntityFilter.ABILITY_LEVEL;
import static com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.EntityFilter.PROCESSING_TYPE;
import static com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.EntityType.HEADCOUNT;
import static com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.EntityType.PRODUCTIVITY;
import static com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.EntityType.THROUGHPUT;
import static com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.OutboundProcessName.PACKING;
import static com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.OutboundProcessName.PICKING;
import static com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.PlanningWorkflow.FBM_WMS_OUTBOUND;
import static com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.ProcessingType.EFFECTIVE_WORKERS;
import static com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.ProcessingType.EFFECTIVE_WORKERS_NS;
import static com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.Source.FORECAST;
import static com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.Source.SIMULATION;
import static com.mercadolibre.flow.control.tool.util.TestUtils.LOGISTIC_CENTER_ID;
import static com.mercadolibre.flow.control.tool.util.TestUtils.objectMapper;
import static java.time.temporal.ChronoUnit.HOURS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mercadolibre.fbm.wms.outbound.commons.rest.HttpRequest;
import com.mercadolibre.fbm.wms.outbound.commons.rest.exception.ClientException;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.adapter.StaffingPlanAdapter;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.EntityType;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.OutboundProcessName;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.ProcessingType;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.Source;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.EntityDataDto;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.EntityRequestDto;
import com.mercadolibre.flow.control.tool.exception.ForecastNotFoundException;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import com.mercadolibre.flow.control.tool.feature.staffing.constant.StaffingType;
import com.mercadolibre.flow.control.tool.feature.staffing.domain.StaffingPlannedData;
import com.mercadolibre.restclient.Response;
import com.mercadolibre.restclient.http.Headers;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class StaffingPlanAdapterTest {

  private static final String PROCESS_PATH_VALUE = "global";

  private static final String HEADCOUNT_METRIC_UNIT_VALUE = "workers";

  private static final long HEADCOUNT_SYSTEMIC_QUANTITY = 10;

  private static final long HEADCOUNT_NON_SYSTEMIC_QUANTITY = 5;

  private static final long HEADCOUNT_SIMULATION_SYSTEMIC_QUANTITY = 99;

  private static final long HEADCOUNT_SIMULATION_NON_SYSTEMIC_QUANTITY = 99;

  private static final long PRODUCTIVITY_QUANTITY = 100;

  private static final long PRODUCTIVITY_SIMULATION_QUANTITY = 999;

  private static final long THROUGHPUT_QUANTITY = 1000;

  private static final long THROUGHPUT_SIMULATION_QUANTITY = 9999;

  private static final Instant DATE_ONE = Instant.parse("2023-03-23T00:00:00Z");

  private static final Instant DATE_TWO = DATE_ONE.plus(1, HOURS);

  private static final Instant DATE_THREE = DATE_TWO.plus(1, HOURS);

  @Mock
  private PlanningModelApiClient client;

  @InjectMocks
  private StaffingPlanAdapter staffingPlanAdapter;

  @Test
  @DisplayName("Gets the all planned staffing of PAPI client")
  void testGetStaffingPlannedOk() {
    //GIVEN
    final var expectedPickingHeadcountDateOne = new StaffingPlannedData(
        DATE_ONE,
        ProcessName.PICKING,
        HEADCOUNT_SYSTEMIC_QUANTITY,
        HEADCOUNT_SIMULATION_NON_SYSTEMIC_QUANTITY,
        false,
        true
    );

    final var expectedPickingHeadcountDateTwo = new StaffingPlannedData(
        DATE_TWO,
        ProcessName.PICKING,
        HEADCOUNT_SIMULATION_SYSTEMIC_QUANTITY,
        HEADCOUNT_NON_SYSTEMIC_QUANTITY,
        true,
        false
    );

    final var expectedPickingHeadcountDateThree = new StaffingPlannedData(
        DATE_THREE,
        ProcessName.PICKING,
        HEADCOUNT_SIMULATION_SYSTEMIC_QUANTITY,
        HEADCOUNT_SIMULATION_NON_SYSTEMIC_QUANTITY,
        true,
        true
    );

    final var expectedPackingProductivityDateOne = new StaffingPlannedData(
        DATE_ONE,
        ProcessName.PACKING,
        PRODUCTIVITY_SIMULATION_QUANTITY,
        0,
        true,
        false
    );

    final var expectedPackingProductivityDateTwo = new StaffingPlannedData(
        DATE_TWO,
        ProcessName.PACKING,
        PRODUCTIVITY_QUANTITY,
        0,
        false,
        false
    );

    final var expectedPackingProductivityDateThree = new StaffingPlannedData(
        DATE_THREE,
        ProcessName.PACKING,
        PRODUCTIVITY_QUANTITY,
        0,
        false,
        false
    );

    when(client.searchEntities(any(EntityRequestDto.class)))
        .thenReturn(mockEntitiesResponse());

    //WHEN
    final var staffingPlannedByType = staffingPlanAdapter.getStaffingPlanned(
        Workflow.FBM_WMS_OUTBOUND,
        LOGISTIC_CENTER_ID,
        DATE_ONE,
        DATE_ONE
    );

    //THEN
    final var staffingPlannedHeadcount = staffingPlannedByType.get(StaffingType.HEADCOUNT);
    final var staffingPlannedProductivity = staffingPlannedByType.get(StaffingType.PRODUCTIVITY);

    final var staffingPlannedHeadcountDateOne = staffingPlannedHeadcount.stream()
        .filter(filterStaffingPlannedByDateAndProcess(DATE_ONE, ProcessName.PICKING))
        .toList()
        .get(0);

    assertionStaffingPlanned(expectedPickingHeadcountDateOne, staffingPlannedHeadcountDateOne);

    final var staffingPlannedHeadcountDateTwo = staffingPlannedHeadcount.stream()
        .filter(filterStaffingPlannedByDateAndProcess(DATE_TWO, ProcessName.PICKING))
        .toList()
        .get(0);

    assertionStaffingPlanned(expectedPickingHeadcountDateTwo, staffingPlannedHeadcountDateTwo);

    final var staffingPlannedHeadcountDateThree = staffingPlannedHeadcount.stream()
        .filter(filterStaffingPlannedByDateAndProcess(DATE_THREE, ProcessName.PICKING))
        .toList()
        .get(0);

    assertionStaffingPlanned(expectedPickingHeadcountDateThree, staffingPlannedHeadcountDateThree);

    final var staffingPlannedProductivityDateOne = staffingPlannedProductivity.stream()
        .filter(filterStaffingPlannedByDateAndProcess(DATE_ONE, ProcessName.PACKING))
        .toList()
        .get(0);

    assertionStaffingPlanned(expectedPackingProductivityDateOne, staffingPlannedProductivityDateOne);

    final var staffingPlannedProductivityDateTwo = staffingPlannedProductivity.stream()
        .filter(filterStaffingPlannedByDateAndProcess(DATE_TWO, ProcessName.PACKING))
        .toList()
        .get(0);

    assertionStaffingPlanned(expectedPackingProductivityDateTwo, staffingPlannedProductivityDateTwo);

    final var staffingPlannedProductivityDateThree = staffingPlannedProductivity.stream()
        .filter(filterStaffingPlannedByDateAndProcess(DATE_THREE, ProcessName.PACKING))
        .toList()
        .get(0);

    assertionStaffingPlanned(expectedPackingProductivityDateThree, staffingPlannedProductivityDateThree);
  }

  private void assertionStaffingPlanned(final StaffingPlannedData expectedStaffingPlanned,
                                        final StaffingPlannedData staffingPlannedData) {
    assertEquals(expectedStaffingPlanned.date(), staffingPlannedData.date(), "It is not the expected date");
    assertEquals(expectedStaffingPlanned.planned(), staffingPlannedData.planned(), "Not the expected planned quantity");
    assertEquals(
        expectedStaffingPlanned.plannedNonSystemic(),
        staffingPlannedData.plannedNonSystemic(),
        "Not the expected planned non-systemic quantity"
    );
    assertEquals(
        expectedStaffingPlanned.plannedEdited(),
        staffingPlannedData.plannedEdited(),
        "Does not contain the expected planned edit"
    );
    assertEquals(
        expectedStaffingPlanned.plannedNonSystemic(),
        staffingPlannedData.plannedNonSystemic(),
        "Does not contain the expected non-systemic planned edit"
    );
  }

  @ParameterizedTest
  @MethodSource("provideExceptions")
  @DisplayName("Catches the client exception.")
  void testGetStaffingPlannedError(
      final Class<? extends Exception> exceptionClass,
      final ClientException exception
  ) {
    //GIVEN
    when(client.searchEntities(
             new EntityRequestDto(
                 FBM_WMS_OUTBOUND,
                 Arrays.stream(EntityType.values()).toList(),
                 LOGISTIC_CENTER_ID,
                 DATE_ONE,
                 DATE_ONE,
                 Arrays.stream(OutboundProcessName.values()).toList(),
                 Map.of(
                     HEADCOUNT, Map.of(
                         PROCESSING_TYPE.getName(), List.of(
                             EFFECTIVE_WORKERS.getName(),
                             EFFECTIVE_WORKERS_NS.getName()
                         )
                     ),
                     PRODUCTIVITY, Map.of(
                         ABILITY_LEVEL.getName(), List.of("1")
                     )
                 )
             )
         )
    ).thenThrow(exception);

    //WHEN and THEN
    assertThrows(
        exceptionClass,
        () -> staffingPlanAdapter.getStaffingPlanned(Workflow.FBM_WMS_OUTBOUND, LOGISTIC_CENTER_ID, DATE_ONE, DATE_ONE)
    );
  }

  private Map<EntityType, List<EntityDataDto>> mockEntitiesResponse() {

    final List<EntityDataDto> headcountData = List.of(
        buildEntityData(DATE_ONE, PICKING, EFFECTIVE_WORKERS, FORECAST, HEADCOUNT_SYSTEMIC_QUANTITY),
        buildEntityData(DATE_ONE, PICKING, EFFECTIVE_WORKERS_NS, FORECAST, HEADCOUNT_NON_SYSTEMIC_QUANTITY),
        buildEntityData(DATE_ONE, PICKING, EFFECTIVE_WORKERS_NS, SIMULATION, HEADCOUNT_SIMULATION_NON_SYSTEMIC_QUANTITY),
        buildEntityData(DATE_ONE, PACKING, EFFECTIVE_WORKERS, FORECAST, HEADCOUNT_SYSTEMIC_QUANTITY),
        buildEntityData(DATE_ONE, PACKING, EFFECTIVE_WORKERS_NS, FORECAST, HEADCOUNT_NON_SYSTEMIC_QUANTITY),
        buildEntityData(DATE_ONE, PACKING, EFFECTIVE_WORKERS_NS, SIMULATION, HEADCOUNT_SIMULATION_NON_SYSTEMIC_QUANTITY),
        buildEntityData(DATE_TWO, PICKING, EFFECTIVE_WORKERS, FORECAST, HEADCOUNT_SYSTEMIC_QUANTITY),
        buildEntityData(DATE_TWO, PICKING, EFFECTIVE_WORKERS_NS, FORECAST, HEADCOUNT_NON_SYSTEMIC_QUANTITY),
        buildEntityData(DATE_TWO, PICKING, EFFECTIVE_WORKERS, SIMULATION, HEADCOUNT_SIMULATION_SYSTEMIC_QUANTITY),
        buildEntityData(DATE_TWO, PACKING, EFFECTIVE_WORKERS, FORECAST, HEADCOUNT_SYSTEMIC_QUANTITY),
        buildEntityData(DATE_TWO, PACKING, EFFECTIVE_WORKERS_NS, FORECAST, HEADCOUNT_NON_SYSTEMIC_QUANTITY),
        buildEntityData(DATE_TWO, PACKING, EFFECTIVE_WORKERS, SIMULATION, HEADCOUNT_SIMULATION_SYSTEMIC_QUANTITY),
        buildEntityData(DATE_THREE, PICKING, EFFECTIVE_WORKERS, FORECAST, HEADCOUNT_SYSTEMIC_QUANTITY),
        buildEntityData(DATE_THREE, PICKING, EFFECTIVE_WORKERS_NS, FORECAST, HEADCOUNT_NON_SYSTEMIC_QUANTITY),
        buildEntityData(DATE_THREE, PICKING, EFFECTIVE_WORKERS, SIMULATION, HEADCOUNT_SIMULATION_SYSTEMIC_QUANTITY),
        buildEntityData(DATE_THREE, PICKING, EFFECTIVE_WORKERS_NS, SIMULATION, HEADCOUNT_SIMULATION_NON_SYSTEMIC_QUANTITY),
        buildEntityData(DATE_THREE, PACKING, EFFECTIVE_WORKERS, FORECAST, HEADCOUNT_SYSTEMIC_QUANTITY),
        buildEntityData(DATE_THREE, PACKING, EFFECTIVE_WORKERS_NS, FORECAST, HEADCOUNT_NON_SYSTEMIC_QUANTITY),
        buildEntityData(DATE_THREE, PACKING, EFFECTIVE_WORKERS, SIMULATION, HEADCOUNT_SIMULATION_SYSTEMIC_QUANTITY),
        buildEntityData(DATE_THREE, PACKING, EFFECTIVE_WORKERS_NS, SIMULATION, HEADCOUNT_SIMULATION_NON_SYSTEMIC_QUANTITY)
    );

    final List<EntityDataDto> productivityData = List.of(
        buildEntityData(DATE_ONE, PICKING, null, FORECAST, PRODUCTIVITY_QUANTITY),
        buildEntityData(DATE_ONE, PICKING, null, SIMULATION, PRODUCTIVITY_SIMULATION_QUANTITY),
        buildEntityData(DATE_TWO, PICKING, null, FORECAST, PRODUCTIVITY_QUANTITY),
        buildEntityData(DATE_THREE, PICKING, null, FORECAST, PRODUCTIVITY_QUANTITY),
        buildEntityData(DATE_ONE, PACKING, null, FORECAST, PRODUCTIVITY_QUANTITY),
        buildEntityData(DATE_ONE, PACKING, null, SIMULATION, PRODUCTIVITY_SIMULATION_QUANTITY),
        buildEntityData(DATE_TWO, PACKING, null, FORECAST, PRODUCTIVITY_QUANTITY),
        buildEntityData(DATE_THREE, PACKING, null, FORECAST, PRODUCTIVITY_QUANTITY)
    );

    final List<EntityDataDto> throughputData = List.of(
        buildEntityData(DATE_ONE, PICKING, null, SIMULATION, THROUGHPUT_SIMULATION_QUANTITY),
        buildEntityData(DATE_TWO, PICKING, null, FORECAST, THROUGHPUT_QUANTITY),
        buildEntityData(DATE_THREE, PICKING, null, FORECAST, THROUGHPUT_QUANTITY),
        buildEntityData(DATE_ONE, PACKING, null, SIMULATION, THROUGHPUT_SIMULATION_QUANTITY),
        buildEntityData(DATE_TWO, PACKING, null, FORECAST, THROUGHPUT_QUANTITY),
        buildEntityData(DATE_THREE, PACKING, null, FORECAST, THROUGHPUT_QUANTITY)
    );

    return Map.of(
        HEADCOUNT, headcountData,
        PRODUCTIVITY, productivityData,
        THROUGHPUT, throughputData
    );
  }

  private EntityDataDto buildEntityData(final Instant date,
                                        final OutboundProcessName processName,
                                        final ProcessingType processingType,
                                        final Source source,
                                        final long quantity) {
    return new EntityDataDto(
        FBM_WMS_OUTBOUND,
        date,
        PROCESS_PATH_VALUE,
        processName,
        processingType,
        HEADCOUNT_METRIC_UNIT_VALUE,
        source,
        quantity);
  }

  private Predicate<StaffingPlannedData> filterStaffingPlannedByDateAndProcess(final Instant date, final ProcessName processName) {
    return staffingPlannedData -> staffingPlannedData.date().equals(date)
        && staffingPlannedData.processName().equals(processName);
  }

  private static Stream<Arguments> provideExceptions() throws JsonProcessingException {
    return Stream.of(
        Arguments.of(
            ForecastNotFoundException.class, new ClientException(
                "PLANNING_MODEL_API",
                HttpRequest.builder()
                    .url("URL")
                    .build(),
                new Response(404, new Headers(Map.of()), objectMapper().writeValueAsBytes("forecast_not_found"))
            )
        ),
        Arguments.of(
            ClientException.class, new ClientException(
                "PLANNING_MODEL_API",
                HttpRequest.builder()
                    .url("URL")
                    .build(),
                new Throwable("Error")
            )
        )
    );
  }
}
