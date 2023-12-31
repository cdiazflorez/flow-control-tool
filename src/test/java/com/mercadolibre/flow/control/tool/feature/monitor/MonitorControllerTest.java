package com.mercadolibre.flow.control.tool.feature.monitor;

import static com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName.NON_TOT_MONO;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName.TOT_MONO;
import static com.mercadolibre.flow.control.tool.util.TestUtils.FBM_WMS_OUTBOUND;
import static com.mercadolibre.flow.control.tool.util.TestUtils.LOGISTIC_CENTER_ID;
import static com.mercadolibre.flow.control.tool.util.TestUtils.getResourceAsString;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mercadolibre.flow.control.tool.feature.backlog.monitor.BacklogLimitsUseCase;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.BacklogProjectedUseCase;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.GetHistoricalBacklogUseCase;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.MonitorController;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.BacklogLimit;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.BacklogMonitor;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.ProcessLimit;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.ProcessPathMonitor;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.ProcessesMonitor;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.SlasMonitor;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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

  private static final String LIMITS = "limits";

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

  private static final Long PICKING_UPPER_LIMIT = 1000L;

  private static final Long PICKING_LOWER_LIMIT = 200L;

  private static final Long PACKING_UPPER_LIMIT = 500L;

  private static final Long PACKING_LOWER_LIMIT = 100L;


  private static final Set<ProcessName> PROCESS_NAMES = Set.of(
      ProcessName.PICKING,
      ProcessName.BATCH_SORTER,
      ProcessName.WALL_IN,
      ProcessName.PACKING,
      ProcessName.PACKING_WALL,
      ProcessName.HU_ASSEMBLY,
      ProcessName.SHIPPING);

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
                                  new ProcessPathMonitor(TOT_MONO, 0),
                                  new ProcessPathMonitor(NON_TOT_MONO, 0)
                              )
                          ),
                          new SlasMonitor(
                              DATE.plus(1, HOURS),
                              5,
                              List.of(
                                  new ProcessPathMonitor(TOT_MONO, 2),
                                  new ProcessPathMonitor(NON_TOT_MONO, 3)
                              )
                          ),
                          new SlasMonitor(
                              DATE.plus(2, HOURS),
                              5,
                              List.of(
                                  new ProcessPathMonitor(TOT_MONO, 2),
                                  new ProcessPathMonitor(NON_TOT_MONO, 3)
                              )
                          ),
                          new SlasMonitor(
                              Instant.parse("2023-03-23T08:00:00Z"),
                              5,
                              List.of(
                                  new ProcessPathMonitor(TOT_MONO, 2),
                                  new ProcessPathMonitor(NON_TOT_MONO, 3)
                              )
                          )
                      )
                  )
              )
          )
      );

  private static final List<BacklogMonitor> BACKLOG_PROJECTED_MOCK =
      List.of(new BacklogMonitor(
              Instant.parse(DATE_FROM_STRING),
              List.of(
                  new ProcessesMonitor(
                      ProcessName.WALL_IN,
                      20,
                      List.of(
                          new SlasMonitor(
                              Instant.parse(DATE_FROM_STRING),
                              0,
                              emptyList()
                          ),
                          new SlasMonitor(
                              Instant.parse(DATE_FROM_STRING).plus(1, HOURS),
                              5,
                              emptyList()
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

  @MockBean
  private BacklogProjectedUseCase backlogProjectedUseCase;

  @MockBean
  private BacklogLimitsUseCase backlogLimitsUseCase;


  private static Stream<Arguments> backlogLimitsProvider() {
    return Stream.of(
        Arguments.of("FBM_WMS_OUTBOUND",
            Arrays.asList("picking1", "batch_sorter", "wall_in", "packing", "packing_wall", "hu_assembly", "shipped"),
            DATE_FROM_STRING,
            DATE_TO_STRING),
        Arguments.of("FBM_WM_OUTBOUND",
            Arrays.asList("picking", "batch_sorter", "wall_in", "packing", "packing_wall", "hu_assembly", "shipped"),
            DATE_FROM_STRING,
            DATE_TO_STRING)
    );
  }

  private static List<BacklogLimit> expectedBacklogLimits(final Instant dateFrom, final Instant dateTo) {
    final ProcessLimit processLimit2 = new ProcessLimit(ProcessName.PICKING, PICKING_LOWER_LIMIT, PICKING_UPPER_LIMIT);
    final ProcessLimit processLimit1 = new ProcessLimit(ProcessName.PACKING, PACKING_LOWER_LIMIT, PACKING_UPPER_LIMIT);
    final List<ProcessLimit> processLimits1 = List.of(processLimit1, processLimit2);

    final BacklogLimit backlogLimitDateFrom = new BacklogLimit(dateFrom, processLimits1);
    final BacklogLimit backlogLimitDateFrom1 = new BacklogLimit(dateFrom.plus(Duration.ofHours(1)), processLimits1);
    final BacklogLimit backlogLimitDateFrom2 = new BacklogLimit(dateFrom.plus(Duration.ofHours(2)), processLimits1);
    final BacklogLimit backlogLimitDateTo = new BacklogLimit(dateTo, processLimits1);

    return Stream.of(backlogLimitDateTo, backlogLimitDateFrom, backlogLimitDateFrom1, backlogLimitDateFrom2)
        .sorted(Comparator.comparing(BacklogLimit::date))
        .toList();
  }

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
        Instant.parse(DATE_TO_STRING),
        Instant.parse(VIEW_DATE_STRING)
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
        Instant.parse("2023-03-24T13:00:00Z"),
        Instant.parse(VIEW_DATE_STRING)
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
        Instant.parse(DATE_TO_STRING),
        Instant.parse(VIEW_DATE_STRING)
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
        Assertions.assertEquals(String.format("DateFrom [%s] is after dateTo [%s]", DATE_TO_STRING, DATE_FROM_STRING),
            Objects.requireNonNull(r.getResolvedException()).getMessage())
    );
  }

  private void whenBacklogProjectionUseCase() {
    when(backlogProjectedUseCase.getBacklogProjected(Instant.parse(DATE_FROM_STRING),
        Instant.parse(DATE_TO_STRING),
        LOGISTIC_CENTER_ID,
        Workflow.FBM_WMS_OUTBOUND,
        PROCESS_NAMES,
        Instant.parse(VIEW_DATE_STRING)
    )).thenReturn(BACKLOG_PROJECTED_MOCK);
  }

  private void whenBacklogProjectionUseCaseEmptyPP() {
    when(backlogProjectedUseCase.getBacklogProjected(Instant.parse(DATE_FROM_STRING),
        Instant.parse(DATE_TO_STRING),
        LOGISTIC_CENTER_ID,
        Workflow.FBM_WMS_OUTBOUND,
        PROCESS_NAMES,
        Instant.parse(VIEW_DATE_STRING)
    )).thenReturn(BACKLOG_PROJECTED_MOCK);
  }

  @Test
  void testGetBacklogProjections() throws Exception {

    // WHEN
    whenBacklogProjectionUseCase();

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
            .param(VIEW_DATE, VIEW_DATE_STRING)
            .param(DATE_FROM, DATE_FROM_STRING)
            .param(DATE_TO, DATE_TO_STRING)
    );

    // THEN
    result.andExpect(status().isOk())
        .andExpect(
            content().json(getResourceAsString("monitor/controller_response_get_backlog_projections.json"))
        );
  }

  @Test
  void testGetBacklogProjectionsWithoutParams() throws Exception {

    // WHEN
    whenBacklogProjectionUseCaseEmptyPP();

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
            .param(VIEW_DATE, VIEW_DATE_STRING)
            .param(DATE_FROM, DATE_FROM_STRING)
            .param(DATE_TO, DATE_TO_STRING)
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
    result.andExpect(status().isBadRequest());
  }

  @Test
  void testGetBacklogProjectionsBadRequestDate() throws Exception {

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
            .param(VIEW_DATE, VIEW_DATE_STRING)
            .param(DATE_FROM, DATE_TO_STRING)
            .param(DATE_TO, DATE_FROM_STRING)
    );

    // THEN
    result.andExpect(status().isBadRequest());
    result.andExpect(r ->
        Assertions.assertEquals(String.format("DateFrom [%s] is after dateTo [%s]", DATE_TO_STRING, DATE_FROM_STRING),
            Objects.requireNonNull(r.getResolvedException()).getMessage())
    );
  }

  @Test
  void testGetBacklogProjectionsBadRequestFormatDate() throws Exception {

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
            .param(VIEW_DATE, VIEW_DATE_STRING)
            .param(DATE_FROM, "adsadsa")
            .param(DATE_TO, DATE_FROM_STRING)
    );

    // THEN
    result.andExpect(status().isBadRequest());
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
            .param(DATE_FROM, DATE_FROM_STRING)
            .param(DATE_TO, DATE_TO_STRING)
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
            .param(DATE_FROM, DATE_FROM_STRING)
            .param(DATE_TO, DATE_TO_STRING)
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
            .param(DATE_FROM, DATE_FROM_STRING)
            .param(DATE_TO, DATE_TO_STRING)
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

  @Test
  void testGetBacklogLimits() throws Exception {
    //GIVEN
    final Instant dateFrom = Instant.parse(DATE_FROM_STRING);
    final Instant dateTo = dateFrom.plus(3, HOURS);
    when(backlogLimitsUseCase.execute(
        LOGISTIC_CENTER_ID,
        Workflow.FBM_WMS_OUTBOUND,
        Set.of(
            ProcessName.WAVING,
            ProcessName.PICKING,
            ProcessName.BATCH_SORTER,
            ProcessName.WALL_IN,
            ProcessName.PACKING,
            ProcessName.PACKING_WALL,
            ProcessName.HU_ASSEMBLY,
            ProcessName.SHIPPING
        ),
        dateFrom,
        dateTo
    )).thenReturn(expectedBacklogLimits(dateFrom, dateTo));

    // WHEN
    final var result = mvc.perform(
        get(String.format(BACKLOG_MONITOR_URL, LOGISTIC_CENTER_ID, LIMITS))
            .param(WORKFLOW, FBM_WMS_OUTBOUND)
            .param(PROCESSES, String.join(",", Arrays.asList(
                "picking",
                "batch_sorter",
                "wall_in",
                "packing",
                "packing_wall",
                "hu_assembly",
                "shipping",
                "waving"
            )))
            .param(DATE_FROM, DATE_FROM_STRING)
            .param(DATE_TO, "2023-03-23T10:00:00Z")
    );

    // THEN
    result.andExpect(status().isOk())
        .andExpect(
            content()
                .json(getResourceAsString("monitor/controller_response_get_backlog_limits.json"))
        );
  }

  @Test
  void testGetBacklogLimitsError() throws Exception {

    // WHEN
    final var result = mvc.perform(
        get(String.format(BACKLOG_MONITOR_URL, LOGISTIC_CENTER_ID, LIMITS))
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
            .param(DATE_FROM, DATE_FROM_STRING)
            .param(DATE_TO, DATE_TO_STRING)
    );

    // THEN
    result.andExpect(status().is4xxClientError()).andExpect(status().isBadRequest());
  }

  @ParameterizedTest
  @MethodSource("backlogLimitsProvider")
  void testGetBacklogLimitsErrorParams(final String workflow,
                                       final List<String> processes,
                                       final Instant fromDate,
                                       final Instant toDate) throws Exception {

    // WHEN
    final var result = mvc.perform(
        get(String.format(BACKLOG_MONITOR_URL, LOGISTIC_CENTER_ID, LIMITS))
            .param(WORKFLOW, workflow)
            .param(PROCESSES, String.join(",", processes))
            .param(DATE_FROM, fromDate.toString())
            .param(DATE_TO, toDate.toString())
    );

    // THEN
    result.andExpect(status().isBadRequest());
  }

}
