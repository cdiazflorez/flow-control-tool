package com.mercadolibre.flow.control.tool.feature.monitor;

import static com.mercadolibre.flow.control.tool.util.TestUtils.FBM_WMS_OUTBOUND;
import static com.mercadolibre.flow.control.tool.util.TestUtils.LOGISTIC_CENTER_ID;
import static com.mercadolibre.flow.control.tool.util.TestUtils.getResourceAsString;
import static java.time.temporal.ChronoUnit.HOURS;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mercadolibre.flow.control.tool.feature.backlog.monitor.GetHistoricalBacklogUseCase;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.MonitorController;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.BacklogMonitor;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.ProcessPathMonitor;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.ProcessesMonitor;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.SlasMonitor;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessPath;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = MonitorController.class)
public class MonitorControllerTest {
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

  private static final Instant DATE = Instant.parse("2023-04-01T08:00:00Z");

  private static final String VIEW_DATE_STRING = "2023-03-23T10:00:00Z";

  private static final String DATE_FROM_STRING = "2023-03-23T07:00:00Z";

  private static final String DATE_TO_STRING = "2023-03-24T08:00:00Z";

  private static final String DATE_TO_STRING_PLUS = "2023-03-24T22:00:00Z";

  private static final String PICKING = "picking";

  private static final String BATCH_SORTER = "batch_sorter";

  private static final String WALL_IN = "wall_in";

  private static final String PACKING = "packing";

  private static final String PACKING_WALL = "packing_wall";

  private static final String HU_ASSEMBLY = "hu_assembly";

  private static final String SHIPPING = "shipping";

  private static final String ERROR = "error";

  private static final List<BacklogMonitor> HISTORICAL_BACKLOG_MOCK =
      List.of(
          new BacklogMonitor(
              DATE,
              List.of(
                  new ProcessesMonitor(
                      ProcessName.PICKING,
                      20,
                      List.of(
                          new SlasMonitor(
                              DATE,
                              0,
                              List.of(
                                  new ProcessPathMonitor(ProcessPath.TOT_MONO, 0),
                                  new ProcessPathMonitor(ProcessPath.NON_TOT_MONO, 0)
                              )
                          ),
                          new SlasMonitor(
                              DATE.plus(1, HOURS),
                              5,
                              List.of(
                                  new ProcessPathMonitor(ProcessPath.TOT_MONO, 2),
                                  new ProcessPathMonitor(ProcessPath.NON_TOT_MONO, 3)
                              )
                          ),
                          new SlasMonitor(
                              DATE.plus(2, HOURS),
                              5,
                              List.of(
                                  new ProcessPathMonitor(ProcessPath.TOT_MONO, 2),
                                  new ProcessPathMonitor(ProcessPath.NON_TOT_MONO, 3)
                              )
                          ),
                          new SlasMonitor(
                              Instant.parse("2023-03-23T08:00:00Z"),
                              5,
                              List.of(
                                  new ProcessPathMonitor(ProcessPath.TOT_MONO, 2),
                                  new ProcessPathMonitor(ProcessPath.NON_TOT_MONO, 3)
                              )
                          )
                      )
                  )
              )
          )
      );


  @Autowired
  private MockMvc mvc;

  @MockBean
  private GetHistoricalBacklogUseCase getHistoricalBacklogUseCase;

  @Test
  void testGetBacklogHistorical() throws Exception {

    //GIVEN
    when(getHistoricalBacklogUseCase.backlogHistoricalMonitor(
        Workflow.FBM_WMS_OUTBOUND,
        LOGISTIC_CENTER_ID,
        Set.of(
            ProcessName.PICKING,
            ProcessName.BATCH_SORTER,
            ProcessName.WALL_IN,
            ProcessName.PACKING,
            ProcessName.PACKING_WALL,
            ProcessName.HU_ASSEMBLY,
            ProcessName.SHIPPING
        ),
        Instant.parse(DATE_FROM_STRING),
        Instant.parse(DATE_TO_STRING)
    )).thenReturn(HISTORICAL_BACKLOG_MOCK);

    // WHEN
    final var result = mvc.perform(
        get(String.format(BACKLOG_MONITOR_URL, LOGISTIC_CENTER_ID, HISTORICAL))
            .param(WORKFLOW, FBM_WMS_OUTBOUND)
            .param(PROCESSES, String.join(",", Arrays.asList(
                PICKING,
                BATCH_SORTER,
                WALL_IN,
                PACKING,
                PACKING_WALL,
                HU_ASSEMBLY,
                SHIPPING
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
            .param(VIEW_DATE, VIEW_DATE_STRING)
            .param(DATE_FROM, DATE_FROM_STRING)
            .param(DATE_TO, DATE_TO_STRING)
    );

    // THEN
    result.andExpect(status().isOk())
        .andExpect(
            content()
                .json(getResourceAsString("monitor/controller_response_get_backlog_historical.json"))
        );
  }

  @Test
  void testGetBacklogHistoricalDate() throws Exception {

    //GIVEN
    when(getHistoricalBacklogUseCase.backlogHistoricalMonitor(
        Workflow.FBM_WMS_OUTBOUND,
        LOGISTIC_CENTER_ID,
        Set.of(
            ProcessName.PICKING,
            ProcessName.BATCH_SORTER,
            ProcessName.WALL_IN,
            ProcessName.PACKING,
            ProcessName.PACKING_WALL,
            ProcessName.HU_ASSEMBLY,
            ProcessName.SHIPPING
        ),
        Instant.parse(DATE_FROM_STRING),
        Instant.parse("2023-03-24T13:00:00Z")
    )).thenReturn(HISTORICAL_BACKLOG_MOCK);

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
                "shipping"
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
            .param(VIEW_DATE, VIEW_DATE_STRING)
            .param(DATE_FROM, DATE_FROM_STRING)
            .param(DATE_TO, DATE_TO_STRING_PLUS)
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
    //GIVEN
    when(getHistoricalBacklogUseCase.backlogHistoricalMonitor(
        Workflow.FBM_WMS_OUTBOUND,
        LOGISTIC_CENTER_ID,
        Set.of(
            ProcessName.PICKING,
            ProcessName.BATCH_SORTER,
            ProcessName.WALL_IN,
            ProcessName.PACKING,
            ProcessName.PACKING_WALL,
            ProcessName.HU_ASSEMBLY,
            ProcessName.SHIPPING),
        Instant.parse(DATE_FROM_STRING),
        Instant.parse(DATE_TO_STRING)
    )).thenReturn(HISTORICAL_BACKLOG_MOCK);

    // WHEN
    final var result = mvc.perform(
        get(String.format(BACKLOG_MONITOR_URL, LOGISTIC_CENTER_ID, HISTORICAL))
            .param(WORKFLOW, FBM_WMS_OUTBOUND)
            .param(PROCESSES, String.join(",", Arrays.asList(
                PICKING,
                BATCH_SORTER,
                WALL_IN,
                PACKING,
                PACKING_WALL,
                HU_ASSEMBLY,
                SHIPPING
            )))
            .param(VIEW_DATE, VIEW_DATE_STRING)
            .param(DATE_FROM, DATE_FROM_STRING)
            .param(DATE_TO, DATE_TO_STRING)
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
                ERROR,
                BATCH_SORTER,
                WALL_IN,
                PACKING,
                PACKING_WALL,
                HU_ASSEMBLY,
                SHIPPING
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
            .param(VIEW_DATE, VIEW_DATE_STRING)
            .param(DATE_FROM, DATE_FROM_STRING)
            .param(DATE_TO, DATE_TO_STRING)
    );

    // THEN
    result.andExpect(status().isBadRequest());
  }

  @Test
  void testGetBacklogHistoricalErrorDate() throws Exception {

    // WHEN
    final var result = mvc.perform(
        get(String.format(BACKLOG_MONITOR_URL, LOGISTIC_CENTER_ID, HISTORICAL))
            .param(WORKFLOW, FBM_WMS_OUTBOUND)
            .param(PROCESSES, String.join(",", Arrays.asList(
                "batch_sorter",
                "wall_in",
                "packing",
                "packing_wall",
                "hu_assembly",
                "shipping"
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
            .param(VIEW_DATE, VIEW_DATE_STRING)
            .param(DATE_FROM, DATE_TO_STRING)
            .param(DATE_TO, DATE_FROM_STRING)
    );

    // THEN
    result.andExpect(status().isBadRequest());
    result.andExpect(r ->
        Assertions.assertEquals("dateFrom must be less than dateTo", r.getResolvedException().getMessage())
    );
  }

  @Test
  void testGetBacklogProjections() throws Exception {

    // WHEN
    final var result = mvc.perform(
        get(String.format(BACKLOG_MONITOR_URL, LOGISTIC_CENTER_ID, PROJECTIONS))
            .param(WORKFLOW, FBM_WMS_OUTBOUND)
            .param(PROCESSES, String.join(",", Arrays.asList(
                PICKING,
                BATCH_SORTER,
                WALL_IN,
                PACKING,
                PACKING_WALL,
                HU_ASSEMBLY,
                SHIPPING
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
                PICKING,
                BATCH_SORTER,
                WALL_IN,
                PACKING,
                PACKING_WALL,
                HU_ASSEMBLY,
                SHIPPING
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
                PICKING,
                BATCH_SORTER,
                WALL_IN,
                PACKING,
                PACKING_WALL,
                HU_ASSEMBLY,
                SHIPPING
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
                PICKING,
                BATCH_SORTER,
                WALL_IN,
                PACKING,
                PACKING_WALL,
                HU_ASSEMBLY,
                SHIPPING
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
                PICKING,
                BATCH_SORTER,
                WALL_IN,
                PACKING,
                PACKING_WALL,
                HU_ASSEMBLY,
                SHIPPING
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
                PICKING,
                BATCH_SORTER,
                WALL_IN,
                PACKING,
                PACKING_WALL,
                HU_ASSEMBLY,
                SHIPPING
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
                + "Allowed values are: [WAVING, PICKING, BATCH_SORTER, WALL_IN, PACKING, PACKING_WALL, HU_ASSEMBLY, SHIPPING]")
        .put("status", 400)
        .toString();

    // WHEN
    final var result = mvc.perform(
        get(String.format(BACKLOG_MONITOR_URL, LOGISTIC_CENTER_ID, HISTORICAL))
            .param(WORKFLOW, FBM_WMS_OUTBOUND)
            .param(PROCESSES, String.join(",", Arrays.asList(
                PICKING,
                BATCH_SORTER,
                WALL_IN,
                ERROR,
                PACKING_WALL,
                HU_ASSEMBLY,
                SHIPPING
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
            .param(VIEW_DATE, VIEW_DATE_STRING)
            .param(DATE_FROM, DATE_FROM_STRING)
            .param(DATE_TO, DATE_TO_STRING)
    );

    // THEN
    result.andExpect(status().isBadRequest())
        .andExpect(content().json(expectedMessage));
  }
}
