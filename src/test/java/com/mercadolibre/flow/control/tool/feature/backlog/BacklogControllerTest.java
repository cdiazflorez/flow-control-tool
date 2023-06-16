package com.mercadolibre.flow.control.tool.feature.backlog;

import static com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName.NON_TOT_MONO;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName.TOT_MONO;
import static com.mercadolibre.flow.control.tool.util.TestUtils.FBM_WMS_OUTBOUND;
import static com.mercadolibre.flow.control.tool.util.TestUtils.LOGISTIC_CENTER_ID;
import static com.mercadolibre.flow.control.tool.util.TestUtils.getResourceAsString;
import static java.time.temporal.ChronoUnit.HOURS;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.ProcessPathMonitor;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.SlasMonitor;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.TotalBacklogMonitor;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.ValueType;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = BacklogController.class)
public class BacklogControllerTest {
  private static final String PROJECTIONS = "projections";

  private static final String WORKFLOW = "workflow";

  private static final String VIEW_DATE = "view_date";

  private static final String DATE_FROM = "date_from";

  private static final String DATE_TO = "date_to";

  private static final String DATE_FROM_STRING = "2023-03-23T07:00:00Z";

  private static final String DATE_TO_LOWER_DURATION_STRING = "2023-03-23T07:30:00Z";

  private static final String DATE_TO_STRING = "2023-03-24T08:00:00Z";

  private static final String PICKING = "picking";

  private static final String BATCH_SORTER = "batch_sorter";

  private static final String WALL_IN = "wall_in";

  private static final String PACKING = "packing";

  private static final String PACKING_WALL = "packing_wall";

  private static final String BACKLOG_URL = "/control_tool/logistic_center/%s/backlog/%s";

  private static final String TOTAL = "total";

  private static final String BACKLOG_PROCESSES = "backlog_processes";

  private static final String THROUGHPUT_PROCESSES = "throughput_processes";

  private static final String VALUE_TYPE = "value_type";

  private static final String UNITS = "units";

  @MockBean
  private BacklogProjectedTotalUseCase backlogProjectedTotalUseCase;

  @Autowired
  private MockMvc mvc;

  private static Stream<Arguments> paramError() {
    return Stream.of(
        Arguments.of("workflow", DATE_FROM_STRING, DATE_TO_STRING, "FBM-WMS-OUTBOUND"),
        Arguments.of(
            "date_from date_to, interval duration below 1 hour", DATE_FROM_STRING, DATE_TO_LOWER_DURATION_STRING, FBM_WMS_OUTBOUND),
        Arguments.of("date_from date_to, date_from after date_to", DATE_TO_STRING, DATE_FROM_STRING, FBM_WMS_OUTBOUND)
    );
  }

  @Test
  void testGetTotalBacklogProjection() throws Exception {
    //GIVEN
    final Instant dateFromValue = Instant.parse(DATE_FROM_STRING);
    final Instant dateToValue = Instant.parse(DATE_TO_STRING);

    final Set<ProcessName> processes = Set.of(
        ProcessName.PICKING,
        ProcessName.BATCH_SORTER,
        ProcessName.WALL_IN,
        ProcessName.PACKING,
        ProcessName.PACKING_WALL);

    final Set<ProcessName> throughputProcess = Set.of(ProcessName.PACKING, ProcessName.PACKING_WALL);

    final List<ProcessPathMonitor> processPathMonitors = List.of(
        new ProcessPathMonitor(
            TOT_MONO,
            5
        ),
        new ProcessPathMonitor(
            NON_TOT_MONO,
            5
        )
    );

    final List<SlasMonitor> slasMonitors = List.of(
        new SlasMonitor(
            Instant.parse("2023-03-23T07:00:00Z"),
            10,
            processPathMonitors
        ),
        new SlasMonitor(
            Instant.parse("2023-03-23T09:00:00Z"),
            10,
            processPathMonitors
        )
    );

    final List<TotalBacklogMonitor> totalBacklogResponse = IntStream.rangeClosed(0, 2)
        .mapToObj(
            iterator -> new TotalBacklogMonitor(
                dateFromValue.plus(iterator, HOURS),
                20,
                slasMonitors
            ))
        .toList();

    when(backlogProjectedTotalUseCase.getTotalProjection(LOGISTIC_CENTER_ID,
        Workflow.FBM_WMS_OUTBOUND,
        processes,
        throughputProcess,
        ValueType.UNITS,
        dateFromValue,
        dateToValue,
        dateFromValue))
        .thenReturn(totalBacklogResponse);

    // WHEN
    final var result = mvc.perform(
        get(String.format(BACKLOG_URL + "/%s", LOGISTIC_CENTER_ID, PROJECTIONS, TOTAL))
            .param(WORKFLOW, FBM_WMS_OUTBOUND)
            .param(BACKLOG_PROCESSES, String.join(",", Arrays.asList(
                PICKING,
                BATCH_SORTER,
                WALL_IN,
                PACKING,
                PACKING_WALL
            )))
            .param(THROUGHPUT_PROCESSES, String.join(",", Arrays.asList(
                PACKING,
                PACKING_WALL
            )))
            .param(VALUE_TYPE, UNITS)
            .param(VIEW_DATE, DATE_FROM_STRING)
            .param(DATE_FROM, DATE_FROM_STRING)
            .param(DATE_TO, DATE_TO_STRING)
    );

    // THEN
    result.andExpect(status().isOk())
        .andExpect(
            content()
                .json(getResourceAsString("backlog/controller_response_get_total_backlog.json"))
        );
  }

  @MethodSource("paramError")
  @ParameterizedTest(name = "Fail parameter {0}")
  void testGetTotalBacklogProjectionError(final String nameTest, final String dateFrom, final String dateTo, final String workflow)
      throws Exception {

    // WHEN
    final var result = mvc.perform(
        get(String.format(BACKLOG_URL + "/%s", LOGISTIC_CENTER_ID, PROJECTIONS, TOTAL))
            .param(WORKFLOW, workflow)
            .param(BACKLOG_PROCESSES, String.join(",", Arrays.asList(
                PICKING,
                BATCH_SORTER,
                WALL_IN,
                PACKING,
                PACKING_WALL
            )))
            .param(THROUGHPUT_PROCESSES, String.join(",", Arrays.asList(
                PACKING,
                PACKING_WALL
            )))
            .param(VALUE_TYPE, UNITS)
            .param(VIEW_DATE, dateFrom)
            .param(DATE_FROM, dateFrom)
            .param(DATE_TO, dateTo)
    );

    // THEN
    result.andExpect(status().is4xxClientError()).andExpect(status().isBadRequest());
  }
}
