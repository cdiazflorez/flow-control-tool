package com.mercadolibre.flow.control.tool.feature.monitor;

import static com.mercadolibre.flow.control.tool.util.TestUtils.FBM_WMS_OUTBOUND;
import static com.mercadolibre.flow.control.tool.util.TestUtils.LOGISTIC_CENTER_ID;
import static com.mercadolibre.flow.control.tool.util.TestUtils.getResourceAsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mercadolibre.flow.control.tool.feature.backlog.monitor.Controller;
import java.util.Arrays;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = Controller.class)
public class ControllerTest {
  private static final String BACKLOG_MONITOR_URL = "/control_tool/logistic_center/%s/backlog/%s";

  private static final String HISTORICAL = "historical";

  private static final String PROJECTIONS = "projections";

  private static final String AVERAGE = "average";

  private static final String WORKFLOW = "workflow";

  private static final String PROCESSES = "processes";

  private static final String PROCESS_PATHS = "process_paths";

  private static final String SLAS = "slas";

  private static final String VIEW_DATE = "view_date";

  private static final String DATE_FROM = "date_from";

  private static final String DATE_TO = "date_to";

  @Autowired
  private MockMvc mvc;

  @Test
  void testGetBacklogHistorical() throws Exception {

    // WHEN
    final var result = mvc.perform(
        get(String.format(BACKLOG_MONITOR_URL, LOGISTIC_CENTER_ID, HISTORICAL))
            .param(WORKFLOW, FBM_WMS_OUTBOUND)
            .param(PROCESSES, String.join(",", Arrays.asList(
                "picking",
                "batch_sorter",
                "wall_in",
                "packing",
                "packing_wall",
                "hu_assembly",
                "shipped"
            )))
            .param(SLAS, String.join(",", Arrays.asList(
                "2023-03-20T10:00:00Z",
                "2023-03-21T10:00:00Z",
                "2023-03-22T10:00:00Z"
            )))
            .param(PROCESS_PATHS, String.join(",", Arrays.asList(
                "global",
                "non_tot_mono",
                "tot_mono"
            )))
            .param(VIEW_DATE, "2023-03-23T10:00:00Z")
            .param(DATE_FROM, "2023-03-23T07:00:00Z")
            .param(DATE_TO, "2023-03-24T20:00:00Z")
    );

    // THEN
    result.andExpect(status().isOk())
        .andExpect(
            content()
                .json(getResourceAsString("monitor/controller_response_get_backlog_historical.json"))
        );
  }

  @Test
  void testGetBacklogHistoricalWithoutParams() throws Exception {

    // WHEN
    final var result = mvc.perform(
        get(String.format(BACKLOG_MONITOR_URL, LOGISTIC_CENTER_ID, HISTORICAL))
            .param(WORKFLOW, FBM_WMS_OUTBOUND)
            .param(PROCESSES, String.join(",", Arrays.asList(
                "picking",
                "batch_sorter",
                "wall_in",
                "packing",
                "packing_wall",
                "hu_assembly",
                "shipped"
            )))
            .param(VIEW_DATE, "2023-03-23T10:00:00Z")
            .param(DATE_FROM, "2023-03-23T07:00:00Z")
            .param(DATE_TO, "2023-03-24T20:00:00Z")
    );

    // THEN
    result.andExpect(status().isOk())
        .andExpect(
            content()
                .json(getResourceAsString("monitor/controller_response_get_backlog_historical.json"))
        );
  }

  @Test
  void testGetBacklogHistoricalError() throws Exception {

    // WHEN
    final var result = mvc.perform(
        get(String.format(BACKLOG_MONITOR_URL, LOGISTIC_CENTER_ID, HISTORICAL))
            .param(WORKFLOW, FBM_WMS_OUTBOUND)
            .param(PROCESSES, String.join(",", Arrays.asList(
                "picking1",
                "batch_sorter",
                "wall_in",
                "packing",
                "packing_wall",
                "hu_assembly",
                "shipped"
            )))
            .param(SLAS, String.join(",", Arrays.asList(
                "2023-03-20T10:00:00Z",
                "2023-03-21T10:00:00Z",
                "2023-03-22T10:00:00Z"
            )))
            .param(PROCESS_PATHS, String.join(",", Arrays.asList(
                "global",
                "non_tot_mono",
                "tot_mono"
            )))
            .param(VIEW_DATE, "2023-03-23T10:00:00Z")
            .param(DATE_FROM, "2023-03-23T07:00:00Z")
            .param(DATE_TO, "2023-03-24T20:00:00Z")
    );

    // THEN
    result.andExpect(status().isBadRequest());
  }

  @Test
  void testGetBacklogProjections() throws Exception {

    // WHEN
    final var result = mvc.perform(
        get(String.format(BACKLOG_MONITOR_URL, LOGISTIC_CENTER_ID, PROJECTIONS))
            .param(WORKFLOW, FBM_WMS_OUTBOUND)
            .param(PROCESSES, String.join(",", Arrays.asList(
                "picking",
                "batch_sorter",
                "wall_in",
                "packing",
                "packing_wall",
                "hu_assembly",
                "shipped"
            )))
            .param(SLAS, String.join(",", Arrays.asList(
                "2023-03-25T10:00:00Z",
                "2023-03-26T10:00:00Z",
                "2023-03-27T10:00:00Z"
            )))
            .param(PROCESS_PATHS, String.join(",", Arrays.asList(
                "global",
                "non_tot_mono",
                "tot_mono"
            )))
            .param(VIEW_DATE, "2023-03-28T10:00:00Z")
            .param(DATE_FROM, "2023-03-28T07:00:00Z")
            .param(DATE_TO, "2023-03-29T20:00:00Z")
    );

    // THEN
    result.andExpect(status().isOk())
        .andExpect(
            content()
                .json(getResourceAsString("monitor/controller_response_get_backlog_projections.json"))
        );
  }

  @Test
  void testGetBacklogProjectionsWithoutParams() throws Exception {

    // WHEN
    final var result = mvc.perform(
        get(String.format(BACKLOG_MONITOR_URL, LOGISTIC_CENTER_ID, PROJECTIONS))
            .param(WORKFLOW, FBM_WMS_OUTBOUND)
            .param(PROCESSES, String.join(",", Arrays.asList(
                "picking",
                "batch_sorter",
                "wall_in",
                "packing",
                "packing_wall",
                "hu_assembly",
                "shipped"
            )))
            .param(VIEW_DATE, "2023-03-28T10:00:00Z")
            .param(DATE_FROM, "2023-03-28T07:00:00Z")
            .param(DATE_TO, "2023-03-29T20:00:00Z")
    );

    // THEN
    result.andExpect(status().isOk())
        .andExpect(
            content()
                .json(getResourceAsString("monitor/controller_response_get_backlog_projections.json"))
        );
  }

  @Test
  void testGetBacklogProjectionsError() throws Exception {

    // WHEN
    final var result = mvc.perform(
        get(String.format(BACKLOG_MONITOR_URL, LOGISTIC_CENTER_ID, PROJECTIONS))
            .param(WORKFLOW, FBM_WMS_OUTBOUND)
            .param(PROCESSES, String.join(",", Arrays.asList(
                "picking",
                "batch_sorter",
                "wall_in",
                "packing",
                "packing_wall",
                "hu_assembly",
                "shipped"
            )))
            .param(SLAS, String.join(",", Arrays.asList(
                "2023-03-25T10:00:00Z",
                "2023-03-26T10:00:00Z",
                "2023-03-27T10:00:00Z"
            )))
            .param(PROCESS_PATHS, String.join(",", Arrays.asList(
                "global2",
                "non_tot_mono",
                "tot_mono"
            )))
            .param(VIEW_DATE, "2023-03-28T10:00:00Z")
            .param(DATE_FROM, "2023-03-28T07:00:00Z")
            .param(DATE_TO, "2023-03-29T20:00:00Z")
    );

    // THEN
    result.andExpect(status().is5xxServerError()).andExpect(status().isInternalServerError());
  }

  @Test
  void testGetBacklogAverage() throws Exception {

    // WHEN
    final var result = mvc.perform(
        get(String.format(BACKLOG_MONITOR_URL, LOGISTIC_CENTER_ID, AVERAGE))
            .param(WORKFLOW, FBM_WMS_OUTBOUND)
            .param(PROCESSES, String.join(",", Arrays.asList(
                "picking",
                "batch_sorter",
                "wall_in",
                "packing",
                "packing_wall",
                "hu_assembly",
                "shipped"
            )))
            .param(SLAS, String.join(",", Arrays.asList(
                "2023-03-12T10:00:00Z",
                "2023-03-13T10:00:00Z",
                "2023-03-14T10:00:00Z"
            )))
            .param(PROCESS_PATHS, String.join(",", Arrays.asList(
                "global",
                "non_tot_mono",
                "tot_mono"
            )))
            .param(VIEW_DATE, "2023-03-15T10:00:00Z")
            .param(DATE_FROM, "2023-03-15T07:00:00Z")
            .param(DATE_TO, "2023-03-16T20:00:00Z")
    );

    // THEN
    result.andExpect(status().isOk())
        .andExpect(
            content()
                .json(getResourceAsString("monitor/controller_response_get_backlog_average.json"))
        );
  }

  @Test
  void testGetBacklogAverageWithoutParams() throws Exception {

    // WHEN
    final var result = mvc.perform(
        get(String.format(BACKLOG_MONITOR_URL, LOGISTIC_CENTER_ID, AVERAGE))
            .param(WORKFLOW, "FBM_WMS_OUTBOUND")
            .param(PROCESSES, String.join(",", Arrays.asList(
                "picking",
                "batch_sorter",
                "wall_in",
                "packing",
                "packing_wall",
                "hu_assembly",
                "shipped"
            )))
            .param(VIEW_DATE, "2023-03-15T10:00:00Z")
            .param(DATE_FROM, "2023-03-15T07:00:00Z")
            .param(DATE_TO, "2023-03-16T20:00:00Z")
    );

    // THEN
    result.andExpect(status().isOk())
        .andExpect(
            content()
                .json(getResourceAsString("monitor/controller_response_get_backlog_average.json"))
        );
  }

  @Test
  void testGetBacklogAverageError() throws Exception {

    // WHEN
    final var result = mvc.perform(
        get(String.format(BACKLOG_MONITOR_URL, LOGISTIC_CENTER_ID, AVERAGE))
            .param(WORKFLOW, "FBM_WM_OUTBOUND")
            .param(PROCESSES, String.join(",", Arrays.asList(
                "picking",
                "batch_sorter",
                "wall_in",
                "packing",
                "packing_wall",
                "hu_assembly",
                "shipped"
            )))
            .param(SLAS, String.join(",", Arrays.asList(
                "2023-03-12T10:00:00Z",
                "2023-03-13T10:00:00Z",
                "2023-03-14T10:00:00Z"
            )))
            .param(PROCESS_PATHS, String.join(",", Arrays.asList(
                "global",
                "non_tot_mono",
                "tot_mono"
            )))
            .param(VIEW_DATE, "2023-03-15T10:00:00Z")
            .param(DATE_FROM, "2023-03-15T07:00:00Z")
            .param(DATE_TO, "2023-03-16T20:00:00Z")
    );

    // THEN
    result.andExpect(status().is4xxClientError()).andExpect(status().isBadRequest());
  }

  @Test
  void testGetBacklogProcessNotSupportedException() throws Exception {
    // GIVEN
    final String expectedMessage = new JSONObject()
        .put("error", "bad_request")
        .put("message",
            "bad request /control_tool/logistic_center/ARTW01/backlog/historical. "
                + "Allowed values are: [WAVING, PICKING, BATCH_SORTER, WALL_IN, PACKING, PACKING_WALL, HU_ASSEMBLY, SHIPPED]")
        .put("status", 400)
        .toString();

    // WHEN
    final var result = mvc.perform(
        get(String.format(BACKLOG_MONITOR_URL, LOGISTIC_CENTER_ID, HISTORICAL))
            .param(WORKFLOW, FBM_WMS_OUTBOUND)
            .param(PROCESSES, String.join(",", Arrays.asList(
                "picking",
                " batch_sorter",
                "wall_in",
                "packing",
                "packing_wall",
                "hu_assembly",
                "shipped"
            )))
            .param(SLAS, String.join(",", Arrays.asList(
                "2023-03-20T10:00:00Z",
                "2023-03-21T10:00:00Z",
                "2023-03-22T10:00:00Z"
            )))
            .param(PROCESS_PATHS, String.join(",", Arrays.asList(
                "global",
                "non_tot_mono",
                "tot_mono"
            )))
            .param(VIEW_DATE, "2023-03-23T10:00:00Z")
            .param(DATE_FROM, "2023-03-23T07:00:00Z")
            .param(DATE_TO, "2023-03-24T20:00:00Z")
    );

    // THEN
    result.andExpect(status().isBadRequest())
        .andExpect(content().json(expectedMessage));
  }
}
