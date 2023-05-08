package com.mercadolibre.flow.control.tool.client.planningmodelapi;

import static com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.EntityType.HEADCOUNT;
import static com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.EntityType.PRODUCTIVITY;
import static com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.EntityType.THROUGHPUT;
import static com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.OutboundProcessName.PICKING;
import static com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.PlanningWorkflow.FBM_WMS_OUTBOUND;
import static com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.ProcessingType.EFFECTIVE_WORKERS;
import static com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.Source.FORECAST;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName.TOT_MONO;
import static com.mercadolibre.flow.control.tool.util.TestUtils.LOGISTIC_CENTER_ID;
import static com.mercadolibre.flow.control.tool.util.TestUtils.getResourceAsString;
import static com.mercadolibre.flow.control.tool.util.TestUtils.objectMapper;
import static com.mercadolibre.restclient.http.ContentType.APPLICATION_JSON;
import static com.mercadolibre.restclient.http.ContentType.HEADER_NAME;
import static com.mercadolibre.restclient.http.HttpMethod.GET;
import static com.mercadolibre.restclient.http.HttpMethod.POST;
import static java.lang.String.format;
import static java.time.ZonedDateTime.now;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.OK;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mercadolibre.fbm.wms.outbound.commons.rest.exception.ClientException;
import com.mercadolibre.flow.control.tool.client.config.RestClientTestUtils;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.EntityType;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.BacklogProjectionRequest;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.BacklogProjectionRequest.Backlog;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.BacklogProjectionRequest.PlannedUnit;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.BacklogProjectionRequest.Process;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.BacklogProjectionRequest.ProcessPath;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.BacklogProjectionRequest.Quantity;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.BacklogProjectionRequest.Throughput;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.BacklogProjectionResponse;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.EntityDataDto;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.EntityRequestDto;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.Metadata;
import com.mercadolibre.restclient.MockResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PlanningModelApiClientTest extends RestClientTestUtils {

  private static final String GET_BACKLOG_PROJECTION = "/logistic_center/%s/projections/backlog";

  private static final String GET_FORECAST_METADATA_URL = "/planning/model/workflows/%s/metadata";

  private static final String GET_ALL_STAFFING_DATA_URL = "/planning/model/workflows/%s/entities/search";

  private static final Instant DATE_FROM = Instant.parse("2023-03-17T14:00:00Z");

  private static final Instant DATE_TO = Instant.parse("2023-03-17T15:00:00Z");

  private static final Integer PICKING_TOTAL = 50;

  private static final String ARTW01 = "ARTW01";

  private PlanningModelApiClient planningModelApiClient;

  @BeforeEach
  void setUp() throws IOException {
    planningModelApiClient = new PlanningModelApiClient(getRestClientTest(), objectMapper());
  }

  @AfterEach
  void cleanUp() {
    super.cleanMocks();
  }

  /**
   * Test the getBacklogProjection method in PlanningModelApiClient.
   * It uses a known JSON response file to mock the Client response.
   */
  @Test
  void testGetBacklogProjection() throws JsonProcessingException {
    //GIVEN
    final String jsonResponseBacklogProjection = getResourceAsString(
        "client/response_get_backlog_projection.json"
    );
    final ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    final List<BacklogProjectionResponse> expectedBacklogProjectionResponse = objectMapper.readValue(
        jsonResponseBacklogProjection,
        objectMapper.getTypeFactory().constructCollectionType(List.class, BacklogProjectionResponse.class)
    );

    MockResponse.builder()
        .withMethod(POST)
        .withURL(BASE_URL.concat(format(GET_BACKLOG_PROJECTION, ARTW01)))
        .withStatusCode(OK.value())
        .withResponseHeader(HEADER_NAME, APPLICATION_JSON.toString())
        .withResponseBody(jsonResponseBacklogProjection)
        .build();

    final BacklogProjectionRequest request = mockBacklogProjectionRequest();

    //WHEN
    List<BacklogProjectionResponse> response = planningModelApiClient.getBacklogProjection(LOGISTIC_CENTER_ID, request);
    //THEN
    assertNotNull(response);
    assertEquals(expectedBacklogProjectionResponse, response);

  }

  /**
   * Test the getBacklogProjection method in PlanningModelApiClient when the client fails
   */
  @Test
  void testGetBacklogProjectionException() {
    //GIVEN
    final String jsonResponseBacklogProjection = getResourceAsString(
        "client/response_get_backlog_projection.json"
    );

    final String expectedMessage = "[http_method: POST] Error calling api.";

    MockResponse.builder()
        .withMethod(POST)
        .withURL(BASE_URL.concat(format(GET_BACKLOG_PROJECTION, ARTW01)))
        .withStatusCode(OK.value())
        .withResponseHeader(HEADER_NAME, APPLICATION_JSON.toString())
        .withResponseBody(jsonResponseBacklogProjection)
        .shouldFail();

    final BacklogProjectionRequest request = mockBacklogProjectionRequest();

    //WHEN
    final ClientException response = assertThrows(ClientException.class, () ->
        planningModelApiClient.getBacklogProjection(LOGISTIC_CENTER_ID, request)
    );
    //THEN
    assertTrue(response.getMessage().contains(expectedMessage));
  }

  @Test
  void testGetForecastMetadataClient() {

    // GIVEN
    MockResponse.builder()
        .withMethod(GET)
        .withURL(BASE_URL + format(GET_FORECAST_METADATA_URL, FBM_WMS_OUTBOUND))
        .withStatusCode(OK.value())
        .withResponseHeader(HEADER_NAME, APPLICATION_JSON.toString())
        .withResponseBody(
            getResourceAsString("client/forecast_metadata_response.json"))
        .build();


    //WHEN
    final List<Metadata> forecastMetadata =
        planningModelApiClient.getForecastMetadata(
            FBM_WMS_OUTBOUND,
            LOGISTIC_CENTER_ID,
            now());

    //THEN
    assertNotNull(forecastMetadata);
    assertEquals(12, forecastMetadata.size());
    forecastMetadataEqualTo(forecastMetadata.get(0), "week", "11-2023");
    forecastMetadataEqualTo(forecastMetadata.get(1), "warehouse_id", ARTW01);
    forecastMetadataEqualTo(forecastMetadata.get(2), "version", "2.0");
    forecastMetadataEqualTo(forecastMetadata.get(3), "units_per_order_ratio", "3.96");
    forecastMetadataEqualTo(forecastMetadata.get(4), "outbound_wall_in_productivity", "100");
    forecastMetadataEqualTo(forecastMetadata.get(5), "outbound_picking_productivity", "80");
    forecastMetadataEqualTo(forecastMetadata.get(6), "outbound_packing_wall_productivity", "90");
    forecastMetadataEqualTo(forecastMetadata.get(7), "outbound_packing_productivity", "100");
    forecastMetadataEqualTo(forecastMetadata.get(8), "outbound_batch_sorter_productivity", "100");
    forecastMetadataEqualTo(forecastMetadata.get(9), "multi_order_distribution", "26");
    forecastMetadataEqualTo(forecastMetadata.get(10), "multi_batch_distribution", "32");
    forecastMetadataEqualTo(forecastMetadata.get(11), "mono_order_distribution", "42");
  }

  @Test
  void testGetForecastMetadataException() {

    // GIVEN
    final String expectedMessage = "[http_method: GET] Error calling api.";
    MockResponse.builder()
        .withMethod(GET)
        .withURL(BASE_URL + format(GET_FORECAST_METADATA_URL, FBM_WMS_OUTBOUND))
        .withStatusCode(OK.value())
        .withResponseHeader(HEADER_NAME, APPLICATION_JSON.toString())
        .withResponseBody(
            getResourceAsString("client/forecast_metadata_response.json"))
        .shouldFail();

    //WHEN
    final ClientException response = assertThrows(ClientException.class, () ->
        planningModelApiClient.getForecastMetadata(
            FBM_WMS_OUTBOUND,
            LOGISTIC_CENTER_ID,
            now()));

    //THEN
    assertTrue(response.getMessage().contains(expectedMessage));
  }

  @Test
  void testSearchEntitiesData() {
    //GIVEN
    MockResponse.builder()
        .withMethod(POST)
        .withURL(BASE_URL.concat(format(GET_ALL_STAFFING_DATA_URL, FBM_WMS_OUTBOUND)))
        .withStatusCode(OK.value())
        .withResponseHeader(HEADER_NAME, APPLICATION_JSON.toString())
        .withResponseBody(getResourceAsString("client/staffing_data_response.json"))
        .build();

    final EntityRequestDto request = new EntityRequestDto(
        FBM_WMS_OUTBOUND,
        List.of(),
        LOGISTIC_CENTER_ID,
        DATE_FROM,
        DATE_TO,
        List.of(),
        Map.of()
    );

    final EntityDataDto expectedHeadcountDataResponse = new EntityDataDto(
        FBM_WMS_OUTBOUND,
        DATE_FROM,
        "global",
        PICKING,
        EFFECTIVE_WORKERS,
        "workers",
        FORECAST,
        92);

    //WHEN
    final Map<EntityType, List<EntityDataDto>> response = planningModelApiClient.searchEntities(request);
    //THEN
    final var headcountData = response.get(HEADCOUNT);

    assertFalse(headcountData.isEmpty());
    assertEquals(expectedHeadcountDataResponse, headcountData.get(0));

    assertFalse(response.get(PRODUCTIVITY).isEmpty());
    assertFalse(response.get(THROUGHPUT).isEmpty());
  }

  private BacklogProjectionRequest mockBacklogProjectionRequest() {

    final Quantity processQuantity = new Quantity(null, DATE_TO, PICKING_TOTAL);
    final ProcessPath processPath = new ProcessPath(TOT_MONO, List.of(processQuantity));
    final Process process = new Process(PICKING, List.of(processPath), null);
    final Backlog backlog = new Backlog(List.of(process));

    final Quantity processPathQuantity = new Quantity(DATE_FROM, DATE_TO, PICKING_TOTAL);
    final ProcessPath plannedUnitProcessPath =
        new ProcessPath(TOT_MONO, List.of(processPathQuantity));
    final PlannedUnit plannedUnit = new PlannedUnit(List.of(plannedUnitProcessPath));

    final Process throughputProcess = new Process(PICKING, null, PICKING_TOTAL);
    final Throughput throughput = new Throughput(DATE_FROM, List.of(throughputProcess));

    return new BacklogProjectionRequest(
        backlog,
        plannedUnit,
        List.of(throughput),
        DATE_FROM,
        DATE_TO,
        FBM_WMS_OUTBOUND
    );
  }

  private void forecastMetadataEqualTo(final Metadata output,
                                       final String key,
                                       final String value) {
    assertEquals(key, output.key());
    assertEquals(value, output.value());
  }

}
