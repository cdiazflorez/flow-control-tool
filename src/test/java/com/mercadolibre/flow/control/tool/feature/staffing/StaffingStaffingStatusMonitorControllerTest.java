package com.mercadolibre.flow.control.tool.feature.staffing;

import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.BATCH_SORTER;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.HU_ASSEMBLY;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.PACKING;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.PACKING_WALL;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.PICKING;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.SHIPPING;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.WALL_IN;
import static com.mercadolibre.flow.control.tool.feature.entity.Workflow.FBM_WMS_OUTBOUND;
import static com.mercadolibre.flow.control.tool.feature.staffing.constant.StaffingType.HEADCOUNT;
import static com.mercadolibre.flow.control.tool.feature.staffing.constant.StaffingType.PRODUCTIVITY;
import static com.mercadolibre.flow.control.tool.feature.staffing.constant.StaffingType.THROUGHPUT;
import static com.mercadolibre.flow.control.tool.util.TestUtils.DATE_FROM;
import static com.mercadolibre.flow.control.tool.util.TestUtils.DATE_TO;
import static com.mercadolibre.flow.control.tool.util.TestUtils.LOGISTIC_CENTER_ID;
import static com.mercadolibre.flow.control.tool.util.TestUtils.getResourceAsString;
import static java.time.temporal.ChronoUnit.HOURS;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.staffing.domain.StaffingOperation;
import com.mercadolibre.flow.control.tool.feature.staffing.domain.StaffingOperationData;
import com.mercadolibre.flow.control.tool.feature.staffing.domain.StaffingOperationValues;
import com.mercadolibre.flow.control.tool.integration.ControllerTest;
import com.mercadolibre.flow.control.tool.util.TestUtils;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class StaffingStaffingStatusMonitorControllerTest extends ControllerTest {

  private static final String STAFFING_OPERATION_URL = "/control_tool/logistic_center/%s/plan/staffing";

  private static final String WORKFLOW_PARAM = "workflow";

  private static final String DATE_FROM_PARAM = "date_from";

  private static final String DATE_TO_PARAM = "date_to";

  private static final List<ProcessName> STAFFING_PROCESS_NAMES = List.of(
      PICKING,
      PACKING,
      PACKING_WALL,
      BATCH_SORTER,
      WALL_IN,
      HU_ASSEMBLY,
      SHIPPING
  );

  @Autowired
  private MockMvc mvc;

  @MockBean
  private StaffingPlanUseCase staffingPlanUseCase;

  @Test
  @DisplayName("Gets the operational staffing plan")
  void testGetStaffingOperationOk() throws Exception {
    //GIVEN
    when(staffingPlanUseCase.getStaffing(LOGISTIC_CENTER_ID, FBM_WMS_OUTBOUND, DATE_FROM, DATE_TO))
        .thenReturn(mockStaffingOperationDto());

    //WHEN
    final var result = mvc.perform(
        get(String.format(STAFFING_OPERATION_URL, LOGISTIC_CENTER_ID))
            .param(WORKFLOW_PARAM, TestUtils.FBM_WMS_OUTBOUND)
            .param(DATE_FROM_PARAM, DATE_FROM.toString())
            .param(DATE_TO_PARAM, DATE_TO.toString())
    );

    //THEN
    result.andExpect(status().isOk()).andExpect(
        content().json(getResourceAsString("staffing/controller_response_get_staffing.json"))
    );
  }

  @Test
  @DisplayName("An exception occurs when obtaining the operational plan")
  void testGetStaffingOperationException() throws Exception {
    //GIVEN

    //WHEN
    final var result = mvc.perform(
        get(String.format(STAFFING_OPERATION_URL, LOGISTIC_CENTER_ID))
            .param(WORKFLOW_PARAM, TestUtils.FBM_WMS_OUTBOUND)
            .param(DATE_FROM_PARAM, DATE_FROM.toString())
            .param(DATE_TO_PARAM, DATE_TO.toString())
    );

    //THEN
    result.andExpect(status().is5xxServerError()).andExpect(status().isInternalServerError());
  }

  public StaffingOperation mockStaffingOperationDto() {

    final List<StaffingOperationData> allHeadcountValues = IntStream.rangeClosed(0, 6)
        .mapToObj(iterator -> StaffingOperationData.builder()
            .date(DATE_FROM.plus(iterator, HOURS))
            .plannedSystemic(10L)
            .plannedSystemicEdited(false)
            .plannedNonSystemic(3L)
            .plannedNonSystemicEdited(false)
            .build())
        .toList();

    final List<StaffingOperationData> allProductivityValues = IntStream.rangeClosed(0, 6)
        .mapToObj(iterator -> StaffingOperationData.builder()
            .date(DATE_FROM.plus(iterator, HOURS))
            .planned(110L)
            .plannedEdited(false)
            .build())
        .toList();

    final List<StaffingOperationData> allThroughputValues = IntStream.rangeClosed(0, 6)
        .mapToObj(iterator -> StaffingOperationData.builder()
            .date(DATE_FROM.plus(iterator, HOURS))
            .planned(1100L)
            .build())
        .toList();

    final StaffingOperationData allHeadcountTotal = StaffingOperationData.builder()
        .plannedSystemic(
            allHeadcountValues.stream()
                .mapToLong(StaffingOperationData::getPlannedSystemic)
                .sum()
        )
        .plannedNonSystemic(
            allHeadcountValues.stream()
                .mapToLong(StaffingOperationData::getPlannedNonSystemic)
                .sum()
        )
        .build();

    final StaffingOperationData allProductivityTotal = StaffingOperationData.builder()
        .planned(
            (long) allProductivityValues.stream()
                .mapToLong(StaffingOperationData::getPlanned)
                .average()
                .orElse(0D)
        )
        .build();

    final StaffingOperationData allThroughputTotal = StaffingOperationData.builder()
        .planned(
            allThroughputValues.stream()
                .mapToLong(StaffingOperationData::getPlanned)
                .sum()
        )
        .build();

    return new StaffingOperation(
        Instant.parse("2023-03-24T11:45:00Z"),
        Map.of(
            HEADCOUNT, getStaffingOperationValues(allHeadcountTotal, allHeadcountValues),
            PRODUCTIVITY, getStaffingOperationValues(allProductivityTotal, allProductivityValues),
            THROUGHPUT, getStaffingOperationValues(allThroughputTotal, allThroughputValues)
        )
    );
  }

  private Map<ProcessName, StaffingOperationValues> getStaffingOperationValues(
      final StaffingOperationData staffingOperationData,
      final List<StaffingOperationData> staffingOperationDataList
  ) {
    return STAFFING_PROCESS_NAMES.stream()
        .collect(
            Collectors.toMap(
                k -> k,
                v -> new StaffingOperationValues(
                    staffingOperationData,
                    staffingOperationDataList
                )
            )
        );
  }

}
