package com.mercadolibre.flow.control.tool.integration;

import static com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.PlanningWorkflow.FBM_WMS_OUTBOUND;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.HU_ASSEMBLY;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.SHIPPING;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName.NON_TOT_MONO;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName.NON_TOT_MULTI_ORDER;
import static com.mercadolibre.flow.control.tool.util.TestUtils.LOGISTIC_CENTER_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;

import com.mercadolibre.flow.control.tool.exception.ApiError;
import com.mercadolibre.flow.control.tool.exception.ApiException;
import com.mercadolibre.flow.control.tool.exception.ForecastNotFoundException;
import com.mercadolibre.flow.control.tool.exception.NoForecastMetadataFoundException;
import com.mercadolibre.flow.control.tool.exception.NoUnitsPerOrderRatioFound;
import com.mercadolibre.flow.control.tool.exception.ProjectionInputsNotFoundException;
import com.mercadolibre.flow.control.tool.exception.RealMetricsException;
import com.mercadolibre.flow.control.tool.exception.ThroughputNotFoundException;
import com.mercadolibre.flow.control.tool.exception.TotalProjectionException;
import com.mercadolibre.flow.control.tool.feature.PingController;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.MonitorController;
import com.mercadolibre.flow.control.tool.feature.backlog.status.StatusController;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.ValueType;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import com.mercadolibre.flow.control.tool.feature.staffing.StaffingController;
import com.mercadolibre.flow.control.tool.util.TestUtils;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class ControllerExceptionHandlerTest extends ControllerTest {

  private static final String BACKLOG_URL = "/control_tool/logistic_center/ARTW01/backlog"
      + "/status?workflow=FBM_WMS_OUTBOUND&type=orders&view_date=2023-03-23T08:25:00Z"
      + "&processes=HU_ASSEMBLY, SHIPPING";

  private static final String TOTAL_PRLOJECTION_BACKLOG_URL = "/control_tool/logistic_center/ARTW01/backlog/projections/total"
      + "?workflow=FBM_WMS_OUTBOUND"
      + "&backlog_processes=waving,picking,batch_sorter,wall_in,packing,packing_wall"
      + "&throughput_processes=packing,packing_wall"
      + "&value_type=units"
      + "&view_date=2023-03-28T08:00:00Z&date_from=2023-03-28T08:00:00Z&date_to=2023-03-28T15:00:00Z";

  private static final String PROJECTION_BACKLOG_URL = "/control_tool/logistic_center/ARTW01/backlog/projections"
      + "?workflow=FBM_WMS_OUTBOUND"
      + "&processes=&slas="
      + "&backlog_processes=waving,picking,batch_sorter,wall_in,packing,packing_wall,hu_assembly,shipping"
      + "&view_date=2023-03-28T08:00:00Z&date_from=2023-03-28T08:00:00Z&date_to=2023-03-28T15:00:00Z";

  private static final String PROJECTION_BACKLOG_URL_THRUOGHPUT = "/control_tool/logistic_center/ARTW01/backlog/projections"
      + "?workflow=FBM_WMS_OUTBOUND"
      + "&slas=&process_paths="
      + "&processes=waving,picking,batch_sorter,wall_in,packing,packing_wall,hu_assembly,shipping"
      + "&view_date=2023-03-28T08:00:00Z&date_from=2023-03-28T08:00:00Z&date_to=2023-03-28T15:00:00Z";

  private static final String NOT_SUPPORTED_URL = "/control_tool/logistic_center/ARTW01/backlog"
      + "/status?workflow=NOT_SUPPORTED_WORKFLOW&type=orders&view_date=2023-03-23T08:25:00Z"
      + "&processes=HU_ASSEMBLY, SHIPPING";

  private static final String HISTORICAL_ERROR = "/control_tool/logistic_center/ARTW01/backlog"
      + "/historical?workflow=NOT_SUPPORTED_WORKFLOW&type=orders&view_date=2023-03-23T08:25:00Z"
      + "&processes=HU_ASSEMBLY,SHIPPING&date_from=2023-02-02T08:25:00Z&date_to=2023-01-01T08:25:00Z"
      + "&process_paths=NON_TOT_MULTI_ORDER,NON_TOT_MONO";

  private static final String HISTORICAL_URL = "/control_tool/logistic_center/ARTW01/backlog"
      + "/historical?workflow=FBM_WMS_OUTBOUND&view_date=2023-03-23T08:25:00Z"
      + "&processes=HU_ASSEMBLY,SHIPPING&date_from=2023-02-01T08:25:00Z&date_to=2023-02-04T08:25:00Z"
      + "&process_paths=NON_TOT_MULTI_ORDER,NON_TOT_MONO";

  private static final String STAFFING_URL = "/control_tool/logistic_center/%s/plan/staffing";

  @SpyBean
  private PingController pingController;

  @SpyBean
  private StatusController backlogStatusController;

  @SpyBean
  private MonitorController monitorController;

  @SpyBean
  private StaffingController staffingController;

  @Test
  void notFound() {
    // When
    ResponseEntity<ApiError> responseEntity =
        this.testRestTemplate.exchange(
            "/fake", HttpMethod.GET, this.getDefaultRequestEntity(), ApiError.class);

    // Then
    assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
  }

  @Test
  void testBadRequest() {
    // When
    ResponseEntity<ApiError> responseEntity =
        this.testRestTemplate.exchange(
            NOT_SUPPORTED_URL,
            HttpMethod.GET,
            this.getDefaultRequestEntity(),
            ApiError.class
        );

    // Then
    assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());


  }

  @Test
  void testUnhandledException() {
    // Given
    doThrow(new RuntimeException()).when(pingController).ping();

    // When
    ResponseEntity<ApiError> responseEntity =
        this.testRestTemplate.exchange(
            "/ping", HttpMethod.GET, this.getDefaultRequestEntity(), ApiError.class);

    // Then
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
  }

  @Test
  void testApiExceptionError() {
    // Given
    doThrow(new ApiException("error", "error", HttpStatus.INTERNAL_SERVER_ERROR.value()))
        .when(pingController)
        .ping();

    // When
    ResponseEntity<ApiError> responseEntity =
        this.testRestTemplate.exchange(
            "/ping", HttpMethod.GET, this.getDefaultRequestEntity(), ApiError.class);

    // Then
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
  }

  @Test
  void testApiExceptionWarn() {
    // Given
    doThrow(new ApiException("warn", "warn", HttpStatus.BAD_REQUEST.value()))
        .when(pingController)
        .ping();

    // When
    ResponseEntity<ApiError> responseEntity =
        this.testRestTemplate.exchange(
            "/ping", HttpMethod.GET, this.getDefaultRequestEntity(), ApiError.class);

    // Then
    assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
  }

  @Test
  void testNoForecastMetadataFound() {
    // Given
    final String date = "2023-03-23T08:25:00Z";
    final Instant viewDate = Instant.parse(date);
    doThrow(new NoForecastMetadataFoundException("ARTW01"))
        .when(backlogStatusController)
        .getBacklogStatus(
            "ARTW01",
            Workflow.FBM_WMS_OUTBOUND,
            ValueType.ORDERS,
            Set.of(HU_ASSEMBLY, SHIPPING),
            viewDate
        );

    // When
    ResponseEntity<ApiError> responseEntity =
        this.testRestTemplate.exchange(
            BACKLOG_URL,
            HttpMethod.GET,
            this.getDefaultRequestEntity(),
            ApiError.class
        );

    // Then
    assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
  }

  @Test
  void testNoUnitPerOrderRatioFound() {
    // Given
    doThrow(new NoUnitsPerOrderRatioFound(LOGISTIC_CENTER_ID))
        .when(monitorController)
        .getBacklogHistorical(
            LOGISTIC_CENTER_ID,
            Workflow.FBM_WMS_OUTBOUND,
            Set.of(HU_ASSEMBLY, SHIPPING),
            null,
            Set.of(NON_TOT_MULTI_ORDER, NON_TOT_MONO),
            Instant.parse("2023-02-01T08:25:00Z"),
            Instant.parse("2023-02-04T08:25:00Z"),
            Instant.parse("2023-03-23T08:25:00Z")
        );

    // When
    ResponseEntity<ApiError> responseEntity =
        this.testRestTemplate.exchange(
            HISTORICAL_URL,
            HttpMethod.GET,
            this.getDefaultRequestEntity(),
            ApiError.class
        );

    // Then
    assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
  }

  @Test
  @DisplayName("A ForecastNotFound exception test")
  void testForecastNotFound() {
    //GIVEN
    final Instant dateFrom = Instant.parse("2023-03-28T08:00:00Z");
    final Instant dateTo = Instant.parse("2023-03-28T10:00:00Z");
    final Instant viewDate = Instant.parse("2023-03-28T09:00:00Z");
    final String queryParams = String.format(
        "?workflow=%s&date_from=%s&date_to=%s&view_date=%s",
        TestUtils.FBM_WMS_OUTBOUND,
        dateFrom.toString(),
        dateTo.toString(),
        viewDate.toString()
    );
    doThrow(new ForecastNotFoundException(LOGISTIC_CENTER_ID, FBM_WMS_OUTBOUND.getName()))
        .when(staffingController).getStaffingOperation(LOGISTIC_CENTER_ID, Workflow.FBM_WMS_OUTBOUND, dateFrom, dateTo, viewDate);

    //WHEN
    final ResponseEntity<ApiError> responseEntity = this.testRestTemplate.exchange(
        String.format(STAFFING_URL, LOGISTIC_CENTER_ID).concat(queryParams),
        HttpMethod.GET,
        this.getDefaultRequestEntity(),
        ApiError.class
    );

    //THEN
    assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
  }

  @Test
  void testDateTimeException() {
    // Given
    doThrow(new DateTimeException("dateFrom must be less than dateTo"))
        .when(monitorController)
        .getBacklogHistorical(
            "ARTW01",
            Workflow.FBM_WMS_OUTBOUND,
            Set.of(HU_ASSEMBLY, SHIPPING),
            null,
            Set.of(NON_TOT_MULTI_ORDER, NON_TOT_MONO),
            Instant.parse("2023-02-23T08:25:00Z"),
            Instant.parse("2023-01-01T08:25:00Z"),
            Instant.parse("2023-03-23T08:25:00Z")
        );

    // When
    final ResponseEntity<ApiError> responseEntity =
        this.testRestTemplate.exchange(
            HISTORICAL_ERROR,
            HttpMethod.GET,
            this.getDefaultRequestEntity(),
            ApiError.class
        );

    // Then
    assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
  }

  @Test
  @DisplayName("A RealMetricsNotFound exception test")
  void testRealMetricsNotFound() {
    //GIVEN
    final Instant dateFrom = Instant.parse("2023-03-28T08:00:00Z");
    final Instant dateTo = Instant.parse("2023-03-28T10:00:00Z");
    final Instant viewDate = Instant.parse("2023-03-28T09:00:00Z");
    final String queryParams = String.format(
        "?workflow=%s&date_from=%s&date_to=%s&view_date=%s",
        TestUtils.FBM_WMS_OUTBOUND,
        dateFrom.toString(),
        dateTo.toString(),
        viewDate.toString()
    );

    doThrow(new RealMetricsException(LOGISTIC_CENTER_ID, FBM_WMS_OUTBOUND.getName(), new Throwable("Error"), 404))
        .when(staffingController).getStaffingOperation(LOGISTIC_CENTER_ID, Workflow.FBM_WMS_OUTBOUND, dateFrom, dateTo, viewDate);


    //WHEN
    final ResponseEntity<ApiError> responseEntity = this.testRestTemplate.exchange(
        String.format(STAFFING_URL, LOGISTIC_CENTER_ID).concat(queryParams),
        HttpMethod.GET,
        this.getDefaultRequestEntity(),
        ApiError.class
    );

    //THEN
    assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
  }

  @Test
  void testBacklogPlannedException() {
    //GIVEN
    final Instant dateFrom = Instant.parse("2023-03-28T08:00:00Z");
    final Instant dateTo = Instant.parse("2023-03-28T15:00:00Z");
    final Instant viewDate = Instant.parse("2023-03-28T08:00:00Z");
    final Set<ProcessName> backlogProcesses = Set.of(
        ProcessName.WAVING,
        ProcessName.PICKING,
        ProcessName.BATCH_SORTER,
        ProcessName.WALL_IN,
        ProcessName.PACKING,
        ProcessName.PACKING_WALL,
        HU_ASSEMBLY,
        SHIPPING
    );

    doThrow(new ProjectionInputsNotFoundException("Forecast sales", LOGISTIC_CENTER_ID, "FBM_WMS_OUTBOUND", new Throwable("Error")))
        .when(monitorController).getBacklogProjections(
            LOGISTIC_CENTER_ID,
            Workflow.FBM_WMS_OUTBOUND,
            backlogProcesses,
            Set.of(),
            Set.of(),
            dateFrom,
            dateTo,
            viewDate);

    //WHEN
    final ResponseEntity<ApiError> responseEntity = this.testRestTemplate.exchange(
        PROJECTION_BACKLOG_URL,
        HttpMethod.GET,
        this.getDefaultRequestEntity(),
        ApiError.class
    );

    //THEN
    assertEquals(HttpStatus.FAILED_DEPENDENCY, responseEntity.getStatusCode());
  }

  @Test
  void testTotalProjectionException() {
    //GIVEN
    final Instant dateFrom = Instant.parse("2023-03-28T08:00:00Z");
    final Instant dateTo = Instant.parse("2023-03-28T15:00:00Z");
    final Instant viewDate = Instant.parse("2023-03-28T08:00:00Z");
    final Set<ProcessName> backlogProcesses = Set.of(
        ProcessName.WAVING,
        ProcessName.PICKING,
        ProcessName.BATCH_SORTER,
        ProcessName.WALL_IN,
        ProcessName.PACKING,
        ProcessName.PACKING_WALL
    );

    final Set<ProcessName> throughputProcesses = Set.of(ProcessName.PACKING, ProcessName.PACKING_WALL);
    final ValueType valueType = ValueType.UNITS;

    doThrow(new TotalProjectionException(LOGISTIC_CENTER_ID, new Throwable("Error"), 404))
        .when(monitorController).getTotalBacklogProjections(
            LOGISTIC_CENTER_ID,
            Workflow.FBM_WMS_OUTBOUND,
            backlogProcesses,
            throughputProcesses,
            valueType,
            dateFrom,
            dateTo,
            viewDate);

    //WHEN
    final ResponseEntity<ApiError> responseEntity = this.testRestTemplate.exchange(
        TOTAL_PRLOJECTION_BACKLOG_URL,
        HttpMethod.GET,
        this.getDefaultRequestEntity(),
        ApiError.class
    );

    //THEN
    assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
  }

  @Test
  void testThroughputException() {
    //GIVEN
    final Instant dateFrom = Instant.parse("2023-03-28T08:00:00Z");
    final Instant dateTo = Instant.parse("2023-03-28T15:00:00Z");
    final Instant viewDate = Instant.parse("2023-03-28T08:00:00Z");
    final Set<ProcessName> backlogProcesses = Set.of(
        ProcessName.WAVING,
        ProcessName.PICKING,
        ProcessName.BATCH_SORTER,
        ProcessName.WALL_IN,
        ProcessName.PACKING,
        ProcessName.PACKING_WALL,
        HU_ASSEMBLY,
        SHIPPING
    );

    doThrow(new ThroughputNotFoundException("Global ProcessPathName not found"))
        .when(monitorController).getBacklogProjections(
            LOGISTIC_CENTER_ID,
            Workflow.FBM_WMS_OUTBOUND,
            backlogProcesses,
            Set.of(),
            Set.of(),
            dateFrom,
            dateTo,
            viewDate);

    //WHEN
    final ResponseEntity<ApiError> responseEntity = this.testRestTemplate.exchange(
        PROJECTION_BACKLOG_URL_THRUOGHPUT,
        HttpMethod.GET,
        this.getDefaultRequestEntity(),
        ApiError.class
    );

    //THEN
    assertEquals(HttpStatus.FAILED_DEPENDENCY, responseEntity.getStatusCode());
  }
}
