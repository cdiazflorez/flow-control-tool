package com.mercadolibre.flow.control.tool.feature.status.controller;

import static com.mercadolibre.flow.control.tool.feature.status.StatusTestUtils.mockAllProcessesSet;
import static com.mercadolibre.flow.control.tool.feature.status.StatusTestUtils.mockBacklogTotalsByProcess;
import static com.mercadolibre.flow.control.tool.util.TestUtils.FBM_WMS_OUTBOUND;
import static com.mercadolibre.flow.control.tool.util.TestUtils.LOGISTIC_CENTER_ID;
import static com.mercadolibre.flow.control.tool.util.TestUtils.VIEW_DATE_INSTANT;
import static com.mercadolibre.flow.control.tool.util.TestUtils.getResourceAsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mercadolibre.flow.control.tool.feature.backlog.status.BacklogStatusUseCase;
import com.mercadolibre.flow.control.tool.feature.backlog.status.StatusController;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.ValueType;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import java.util.Arrays;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = StatusController.class)
class BacklogStatusControllerTest {

  private static final String BACKLOG_STATUS_URL = "/control_tool/logistic_center/%s/backlog/status";

  private static final String WORKFLOW = "workflow";

  private static final String TYPE = "type";

  private static final String UNITS = "units";

  private static final String PROCESSES = "processes";

  private static final String WAVING = "waving";

  private static final String PICKING = "picking";

  private static final String BATCH_SORTER = "batch_sorter";

  private static final String WALL_IN = "wall_in";

  private static final String PACKING = "packing";

  private static final String PACKING_WALL = "packing_wall";

  private static final String HU_ASSEMBLY = "hu_assembly";

  private static final String SHIPPED = "shipped";

  private static final String VIEW_DATE = "view_date";


  @Autowired
  private MockMvc mvc;

  @MockBean
  private BacklogStatusUseCase backlogStatusUseCase;

  @Test
  void testGetBacklogStatusAllProcesses() throws Exception {
    // GIVEN
    final Set<ProcessName> processes = mockAllProcessesSet();
    when(backlogStatusUseCase.getBacklogStatus(
        LOGISTIC_CENTER_ID,
        Workflow.FBM_WMS_OUTBOUND,
        ValueType.UNITS,
        processes,
        VIEW_DATE_INSTANT
    )).thenReturn(mockBacklogTotalsByProcess());

    // WHEN
    final var result = mvc.perform(
        get(String.format(BACKLOG_STATUS_URL, LOGISTIC_CENTER_ID))
            .param(WORKFLOW, FBM_WMS_OUTBOUND)
            .param(TYPE, UNITS)
            .param(PROCESSES, String.join(",", Arrays.asList(
                WAVING,
                PICKING,
                BATCH_SORTER,
                WALL_IN,
                PACKING,
                PACKING_WALL,
                HU_ASSEMBLY,
                SHIPPED
            )))
            .param(VIEW_DATE, "2023-03-06T10:00:00Z")
    );

    // THEN
    result.andExpect(status().isOk()).andExpect(content()
        .json(getResourceAsString("monitor/controller_response_get_backlog_status.json")));
  }

  @Test
  void testWhenWorkflowIsNotSuported() throws Exception {
    final var result = mvc.perform(
        get(String.format(BACKLOG_STATUS_URL, LOGISTIC_CENTER_ID))
            .param(WORKFLOW, "NOT_SUPPORTED_WORKFLOW")
            .param(TYPE, UNITS)
            .param(PROCESSES, String.join(",", Arrays.asList(
                WAVING,
                PICKING,
                BATCH_SORTER,
                WALL_IN,
                PACKING,
                PACKING_WALL,
                HU_ASSEMBLY,
                SHIPPED
            )))
            .param(VIEW_DATE, "2023-03-06T10:00:00Z")
    );

    // THEN
    result.andExpect(status().isBadRequest());
  }
}
