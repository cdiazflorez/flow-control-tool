package com.mercadolibre.flow.control.tool.integration;

import static com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.PlanningWorkflow.FBM_WMS_OUTBOUND;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.HU_ASSEMBLY;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.SHIPPING;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessPath.NON_TOT_MONO;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessPath.NON_TOT_MULTI_ORDER;
import static com.mercadolibre.flow.control.tool.util.TestUtils.LOGISTIC_CENTER_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;

import com.mercadolibre.flow.control.tool.exception.ApiError;
import com.mercadolibre.flow.control.tool.exception.ApiException;
import com.mercadolibre.flow.control.tool.exception.ForecastNotFoundException;
import com.mercadolibre.flow.control.tool.exception.NoForecastMetadataFoundException;
import com.mercadolibre.flow.control.tool.feature.PingController;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.MonitorController;
import com.mercadolibre.flow.control.tool.feature.backlog.status.StatusController;
import com.mercadolibre.flow.control.tool.feature.entity.ValueType;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import com.mercadolibre.flow.control.tool.feature.staffing.StaffingController;
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

  private static final String NOT_SUPPORTED_URL = "/control_tool/logistic_center/ARTW01/backlog"
      + "/status?workflow=NOT_SUPPORTED_WORKFLOW&type=orders&view_date=2023-03-23T08:25:00Z"
      + "&processes=HU_ASSEMBLY, SHIPPING";

  private static final String HISTORICAL_ERROR = "/control_tool/logistic_center/ARTW01/backlog"
      + "/historical?workflow=NOT_SUPPORTED_WORKFLOW&type=orders&view_date=2023-03-23T08:25:00Z"
      + "&processes=HU_ASSEMBLY,SHIPPING&date_from=2023-02-02T08:25:00Z&date_to=2023-01-01T08:25:00Z"
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
  @DisplayName("A ForecastNotFound exception test")
  void testForecastNotFound() {
    //GIVEN
    final String queryParams = "?workflow=fbm_wms_outbound&date_from=2023-03-28T08:00:00Z&date_to=2023-03-28T10:00:00Z";
    final Instant dateFrom = Instant.parse("2023-03-28T08:00:00Z");
    final Instant dateTo = Instant.parse("2023-03-28T10:00:00Z");
    doThrow(new ForecastNotFoundException(LOGISTIC_CENTER_ID, FBM_WMS_OUTBOUND.getName()))
        .when(staffingController).getStaffingOperation(LOGISTIC_CENTER_ID, Workflow.FBM_WMS_OUTBOUND, dateFrom, dateTo);

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
}
