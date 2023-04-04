package com.mercadolibre.flow.control.tool.client.staffingapi;

import static com.mercadolibre.flow.control.tool.client.staffingapi.constant.StaffingWorkflow.FBM_WMS_OUTBOUND;
import static com.mercadolibre.flow.control.tool.util.TestUtils.DATE_FROM;
import static com.mercadolibre.flow.control.tool.util.TestUtils.DATE_TO;
import static com.mercadolibre.flow.control.tool.util.TestUtils.LOGISTIC_CENTER_ID;
import static com.mercadolibre.flow.control.tool.util.TestUtils.getResourceAsString;
import static com.mercadolibre.restclient.http.ContentType.APPLICATION_JSON;
import static com.mercadolibre.restclient.http.ContentType.HEADER_NAME;
import static com.mercadolibre.restclient.http.ContentType.TEXT_PLAIN;
import static com.mercadolibre.restclient.http.HttpMethod.GET;
import static java.lang.String.format;
import static java.time.temporal.ChronoUnit.HOURS;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;

import com.mercadolibre.fbm.wms.outbound.commons.rest.exception.ClientException;
import com.mercadolibre.flow.control.tool.client.config.RestClientTestUtils;
import com.mercadolibre.flow.control.tool.client.staffingapi.dto.MetricDto;
import com.mercadolibre.flow.control.tool.client.staffingapi.dto.MetricHistoryDto;
import com.mercadolibre.restclient.MockResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StaffingApiClientTest extends RestClientTestUtils {

  private static final String GET_METRIC_HISTORY_URL = "/logistic_centers/%s/metrics/history";

  private StaffingApiClient client;

  @BeforeEach
  void setUp() throws IOException {
    client = new StaffingApiClient(getRestClientTest());
  }

  @Test
  @DisplayName("Get a historical metric from staffing api")
  void testGetMetricHistoryOk() {
    //GIVEN
    MockResponse.builder()
        .withMethod(GET)
        .withURL(BASE_URL.concat(format(GET_METRIC_HISTORY_URL, LOGISTIC_CENTER_ID)))
        .withStatusCode(OK.value())
        .withResponseHeader(HEADER_NAME, APPLICATION_JSON.toString())
        .withResponseBody(getResourceAsString("client/get_metric_history.json"))
        .build();
    //WHEN
    final List<MetricHistoryDto>
        metricsHistory = client.getMetricsHistory(LOGISTIC_CENTER_ID, FBM_WMS_OUTBOUND, DATE_FROM, DATE_TO);
    //THEN
    metricsHistory.forEach(
        metricHistory -> {
          assertAll(
              () -> assertEquals(FBM_WMS_OUTBOUND, metricHistory.getWorkflow()),
              () -> assertFalse(metricHistory.getMetrics().isEmpty()),
              () -> IntStream.rangeClosed(0, 6).forEach(
                  iterator -> {
                    final MetricDto metric = metricHistory.getMetrics().get(iterator);
                    assertEquals(DATE_FROM.plus(iterator, HOURS), metric.getDate());
                    assertEquals(100L, metric.getEffProductivity());
                    assertEquals(2000L, metric.getThroughput());
                  }
              )
          );
        }
    );
  }

  @Test
  @DisplayName("Generates an exception when gets the metric history")
  void testGetMetricHistoryException() {
    //GIVEN
    MockResponse.builder()
        .withMethod(GET)
        .withURL(BASE_URL.concat(format(GET_METRIC_HISTORY_URL, LOGISTIC_CENTER_ID)))
        .withStatusCode(INTERNAL_SERVER_ERROR.value())
        .withResponseHeader(HEADER_NAME, TEXT_PLAIN.toString())
        .withResponseBody("Error")
        .shouldFail();
    //WHEN - THEN
    assertThrows(ClientException.class, () -> client.getMetricsHistory(LOGISTIC_CENTER_ID, FBM_WMS_OUTBOUND, DATE_FROM, DATE_TO));
  }

}
