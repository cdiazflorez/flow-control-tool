package com.mercadolibre.flow.control.tool.client.planningmodelapi;

import static com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.EntityType.HEADCOUNT;
import static com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.EntityType.PRODUCTIVITY;
import static com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.EntityType.THROUGHPUT;
import static com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.PlanningWorkflow.FBM_WMS_OUTBOUND;
import static com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.ProcessingType.EFFECTIVE_WORKERS;
import static com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.Source.FORECAST;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.BATCH_SORTER;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.HU_ASSEMBLY;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.PACKING;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.PACKING_WALL;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.PICKING;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.SHIPPING;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.WALL_IN;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.WAVING;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName.GLOBAL;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName.NON_TOT_MONO;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName.NON_TOT_MULTI_BATCH;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName.NON_TOT_MULTI_ORDER;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName.TOT_MONO;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName.TOT_MULTI_BATCH;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName.TOT_MULTI_ORDER;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName.TOT_SINGLE_SKU;
import static com.mercadolibre.flow.control.tool.util.TestUtils.LOGISTIC_CENTER_ID;
import static com.mercadolibre.flow.control.tool.util.TestUtils.getResourceAsString;
import static com.mercadolibre.flow.control.tool.util.TestUtils.objectMapper;
import static com.mercadolibre.restclient.http.ContentType.APPLICATION_JSON;
import static com.mercadolibre.restclient.http.ContentType.HEADER_NAME;
import static com.mercadolibre.restclient.http.HttpMethod.GET;
import static com.mercadolibre.restclient.http.HttpMethod.POST;
import static java.lang.String.format;
import static java.time.ZonedDateTime.now;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.REQUEST_TIMEOUT;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mercadolibre.fbm.wms.outbound.commons.rest.exception.ClientException;
import com.mercadolibre.flow.control.tool.client.config.RestClientTestUtils;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.EntityType;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.OutboundProcessName;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.PlannedGrouper;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.BacklogPlannedRequest;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.BacklogPlannedResponse;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.BacklogProjectionRequest;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.BacklogProjectionRequest.Backlog;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.BacklogProjectionRequest.Backlog.Process;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.BacklogProjectionRequest.Backlog.Process.ProcessPathByDateOut;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.BacklogProjectionRequest.Backlog.Process.ProcessPathByDateOut.QuantityByDateOut;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.BacklogProjectionRequest.PlannedUnit;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.BacklogProjectionRequest.PlannedUnit.ProcessPathByDateInOut;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.BacklogProjectionRequest.PlannedUnit.ProcessPathByDateInOut.QuantityByDateInOut;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.BacklogProjectionRequest.Throughput;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.BacklogProjectionRequest.Throughput.QuantityByProcessName;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.BacklogProjectionResponse;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.EntityDataDto;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.EntityRequestDto;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.Metadata;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.TotalBacklogProjectionRequest;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.TotalBacklogProjectionResponse;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import com.mercadolibre.restclient.MockResponse;
import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PlanningModelApiClientTest extends RestClientTestUtils {

  private static final String GET_THROUGHPUT_BY_PP_PROCESS_DATE_URL = "/logistic_center/%s/plan/staffing/throughput";

  private static final String GET_BACKLOG_PROJECTION = "/logistic_center/%s/projections/backlog";

  private static final String GET_FORECAST_METADATA_URL = "/planning/model/workflows/%s/metadata";

  private static final String GET_ALL_STAFFING_DATA_URL = "/planning/model/workflows/%s/entities/search";

  private static final String GET_BACKLOG_PLANNED_URL = "/logistic_center/%s/plan/units";

  private static final String GET_BACKLOG_PROJECTION_TOTAL_URL = GET_BACKLOG_PROJECTION + "/total";

  private static final Instant DATE_FROM = Instant.parse("2023-03-17T14:00:00Z");

  private static final Instant DATE_TO = Instant.parse("2023-03-17T15:00:00Z");

  private static final Instant VIEW_DATE = DATE_FROM;

  private static final Instant DATE_IN = DATE_FROM;

  private static final Instant DATE_OUT = Instant.parse("2023-03-18T08:00:00Z");

  private static final Integer PICKING_TOTAL = 50;

  private static final Integer WAVING_TOTAL = 25;

  private static final String ARTW01 = "ARTW01";

  private static final Set<ProcessName> PROCESS_NAME_SET = Set.of(
      WAVING,
      PICKING,
      BATCH_SORTER,
      WALL_IN,
      PACKING,
      PACKING_WALL,
      HU_ASSEMBLY,
      SHIPPING
  );

  private static final Set<ProcessPathName> PROCESS_PATH_NAME_SET = Set.of(
      TOT_MONO,
      NON_TOT_MONO,
      TOT_MULTI_BATCH,
      NON_TOT_MULTI_BATCH,
      TOT_MULTI_ORDER,
      NON_TOT_MULTI_ORDER,
      TOT_SINGLE_SKU,
      GLOBAL
  );

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
    final String expectedMessage = "[http_method: POST] Error calling api.";

    MockResponse.builder()
        .withMethod(POST)
        .withURL(BASE_URL.concat(format(GET_BACKLOG_PROJECTION, ARTW01)))
        .withStatusCode(REQUEST_TIMEOUT.value())
        .shouldFail();

    final BacklogProjectionRequest request = mockBacklogProjectionRequest();

    //WHEN
    final ClientException response = assertThrows(ClientException.class, () ->
        planningModelApiClient.getBacklogProjection(LOGISTIC_CENTER_ID, request)
    );
    //THEN
    assertTrue(response.getMessage().contains(expectedMessage));
  }

  /**
   * Test the getThroughputByPPAndProcessAndDate method in PlanningModelApiClient with happy ending.
   * Where the total were set for WAVING -> TOT_MONO equals to 50.
   */
  @Test
  void testGetThroughputByPPAndProcessAndDate() {
    //GIVEN
    final String jsonResponseBacklogProjection = getResourceAsString(
        "client/response_get_papi_tph_by_pp_process_date.json"
    );

    MockResponse.builder()
        .withMethod(GET)
        .withURL(BASE_URL.concat(format(GET_THROUGHPUT_BY_PP_PROCESS_DATE_URL, ARTW01)))
        .withStatusCode(OK.value())
        .withResponseHeader(HEADER_NAME, APPLICATION_JSON.toString())
        .withResponseBody(jsonResponseBacklogProjection)
        .build();

    //WHEN
    final var response = planningModelApiClient.getThroughputByPPAndProcessAndDate(
        Workflow.FBM_WMS_OUTBOUND,
        LOGISTIC_CENTER_ID,
        DATE_FROM,
        DATE_TO,
        PROCESS_NAME_SET,
        PROCESS_PATH_NAME_SET
    );
    //THEN
    assertEquals(PROCESS_PATH_NAME_SET.size(), response.size());
    assertEquals(PROCESS_NAME_SET.size(), response.get(GLOBAL).size());
    assertEquals(WAVING_TOTAL, response.get(GLOBAL).get(OutboundProcessName.WAVING).get(DATE_FROM).quantity());
    assertEquals(PICKING_TOTAL, response.get(GLOBAL).get(OutboundProcessName.PICKING).get(DATE_FROM).quantity());
    assertEquals(PICKING_TOTAL, response.get(TOT_MONO).get(OutboundProcessName.WAVING).get(DATE_FROM).quantity());
  }

  /**
   * Test the getThroughputByPPAndProcessAndDate method in PlanningModelApiClient with happy ending.
   * Where the PP were set only for GLOBAL equals to 50.
   */
  @Test
  void testGetThroughputByPPAndProcessAndDateNoPP() {
    //GIVEN
    final String jsonResponseThroughput = getResourceAsString(
        "client/response_get_papi_tph_by_pp_process_date_no_pp.json"
    );

    MockResponse.builder()
        .withMethod(GET)
        .withURL(BASE_URL.concat(format(GET_THROUGHPUT_BY_PP_PROCESS_DATE_URL, ARTW01)))
        .withStatusCode(OK.value())
        .withResponseHeader(HEADER_NAME, APPLICATION_JSON.toString())
        .withResponseBody(jsonResponseThroughput)
        .build();

    //WHEN
    final var response = planningModelApiClient.getThroughputByPPAndProcessAndDate(
        Workflow.FBM_WMS_OUTBOUND,
        LOGISTIC_CENTER_ID,
        DATE_FROM,
        DATE_TO,
        PROCESS_NAME_SET,
        null
    );
    //THEN
    assertEquals(1, response.size());
    assertEquals(PROCESS_NAME_SET.size(), response.get(GLOBAL).size());
    assertEquals(WAVING_TOTAL, response.get(GLOBAL).get(OutboundProcessName.WAVING).get(DATE_FROM).quantity());
    assertEquals(PICKING_TOTAL, response.get(GLOBAL).get(OutboundProcessName.PICKING).get(DATE_FROM).quantity());
  }

  /**
   * Test the getThroughputByPPAndProcessAndDate method in PlanningModelApiClient when response is an empty {}.
   */
  @Test
  void testGetThroughputByPPAndProcessAndDateEmptyResponse() {
    //GIVEN
    MockResponse.builder()
        .withMethod(GET)
        .withURL(BASE_URL.concat(format(GET_THROUGHPUT_BY_PP_PROCESS_DATE_URL, ARTW01)))
        .withStatusCode(OK.value())
        .withResponseHeader(HEADER_NAME, APPLICATION_JSON.toString())
        .withResponseBody("{}")
        .build();

    //WHEN
    final var response = planningModelApiClient.getThroughputByPPAndProcessAndDate(
        Workflow.FBM_WMS_OUTBOUND,
        LOGISTIC_CENTER_ID,
        DATE_FROM,
        DATE_TO,
        PROCESS_NAME_SET,
        Set.of(GLOBAL)
    );
    //THEN
    assertEquals(emptyMap(), response);
  }

  /**
   * Test the getThroughputByPPAndProcessAndDate method in PlanningModelApiClient when the client fails
   */
  @Test
  void testGetThroughputByPPAndProcessAndDateException() {
    //GIVEN
    final String expectedMessage = "[http_method: GET] Error calling api.";

    MockResponse.builder()
        .withMethod(GET)
        .withURL(BASE_URL.concat(format(GET_THROUGHPUT_BY_PP_PROCESS_DATE_URL, ARTW01)))
        .shouldFail();

    //WHEN
    final ClientException response = assertThrows(ClientException.class, () ->
        planningModelApiClient.getThroughputByPPAndProcessAndDate(
            Workflow.FBM_WMS_OUTBOUND,
            LOGISTIC_CENTER_ID,
            DATE_FROM,
            DATE_TO,
            PROCESS_NAME_SET,
            PROCESS_PATH_NAME_SET
        )
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
    final ZonedDateTime now = now();
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
            now
        ));

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
        OutboundProcessName.PICKING,
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

  @Test
  @DisplayName("Test that obtains the planned backlog.")
  void testGetBacklogPlanned() {
    //GIVEN
    final Set<ProcessPathName> processPathNames = Set.of(
        TOT_MONO,
        NON_TOT_MONO,
        TOT_MULTI_BATCH,
        NON_TOT_MULTI_BATCH
    );

    final Set<PlannedGrouper> plannedGroupers = Set.of(
        PlannedGrouper.DATE_IN,
        PlannedGrouper.DATE_OUT,
        PlannedGrouper.PROCESS_PATH
    );

    final BacklogPlannedRequest request = new BacklogPlannedRequest(
        LOGISTIC_CENTER_ID,
        FBM_WMS_OUTBOUND,
        processPathNames,
        DATE_FROM,
        DATE_TO,
        Optional.empty(),
        Optional.empty(),
        plannedGroupers
    );

    final List<BacklogPlannedResponse> expectedResponse = processPathNames.stream()
        .map(processPathName -> new BacklogPlannedResponse(
            new BacklogPlannedResponse.GroupKey(
                processPathName,
                DATE_IN,
                DATE_OUT
            ),
            35.55D
        )).toList();

    MockResponse.builder()
        .withMethod(GET)
        .withURL(BASE_URL + format(GET_BACKLOG_PLANNED_URL, LOGISTIC_CENTER_ID))
        .withStatusCode(OK.value())
        .withResponseHeader(HEADER_NAME, APPLICATION_JSON.toString())
        .withResponseBody(
            getResourceAsString("client/backlog_planned.json"))
        .build();

    //WHEN
    final List<BacklogPlannedResponse> responses = planningModelApiClient.getBacklogPlanned(request);
    //THEN
    assertFalse(responses.isEmpty());
    assertAll(
        "Assert that the response is correct.",
        () -> assertEquals(expectedResponse.size(), responses.size()),
        () -> assertTrue(expectedResponse.containsAll(responses)),
        () -> assertTrue(responses.containsAll(expectedResponse))
    );
  }

  @Test
  @DisplayName("Test that obtains the total backlog projection.")
  void testGetBacklogPlannedException() {
    //GIVEN
    final BacklogPlannedRequest request = new BacklogPlannedRequest(
        LOGISTIC_CENTER_ID,
        FBM_WMS_OUTBOUND,
        Set.of(),
        DATE_FROM,
        DATE_TO,
        Optional.empty(),
        Optional.empty(),
        Set.of()
    );

    MockResponse.builder()
        .withMethod(GET)
        .withURL(BASE_URL + format(GET_BACKLOG_PLANNED_URL, LOGISTIC_CENTER_ID))
        .withStatusCode(INTERNAL_SERVER_ERROR.value())
        .withResponseHeader(HEADER_NAME, APPLICATION_JSON.toString())
        .shouldFail();

    //WHEN - THEN
    assertThrows(ClientException.class, () -> planningModelApiClient.getBacklogPlanned(request));
  }

  @Test
  @DisplayName("Test that obtains the total backlog projection with an exception.")
  void testGetTotalBacklogProjection() {
    //GIVEN
    final String jsonResponseTotalBacklogProjection = getResourceAsString(
        "client/response_get_total_backlog_projection.json"
    );

    MockResponse.builder()
        .withMethod(POST)
        .withURL(BASE_URL.concat(format(GET_BACKLOG_PROJECTION_TOTAL_URL, ARTW01)))
        .withStatusCode(OK.value())
        .withResponseHeader(HEADER_NAME, APPLICATION_JSON.toString())
        .withResponseBody(jsonResponseTotalBacklogProjection)
        .build();

    final TotalBacklogProjectionRequest request = mockTotalBacklogProjectionRequest();

    //WHEN
    List<TotalBacklogProjectionResponse> response = planningModelApiClient.getTotalBacklogProjection(LOGISTIC_CENTER_ID, request);
    //THEN
    assertNotNull(response);
    assertEquals(Instant.parse("2023-03-17T14:00:00Z"), response.get(0).getDate());
    assertEquals(Instant.parse("2023-03-17T15:00:00Z"), response.get(0).getSla().get(0).getDateOut());
    assertEquals(50, response.get(0).getSla().get(0).getQuantity());
    assertEquals(1, response.get(0).getSla().get(0).getProcessPath().size());
    assertEquals(TOT_MONO, response.get(0).getSla().get(0).getProcessPath().get(0).getName());
    assertEquals(50, response.get(0).getSla().get(0).getProcessPath().get(0).getQuantity());

  }

  @Test
  @DisplayName("Test that obtains the total backlog projection with an exception.")
  void testGetTotalBacklogProjectionException() {
    //GIVEN
    final String jsonResponseTotalBacklogProjection = getResourceAsString(
        "client/response_get_total_backlog_projection.json"
    );

    final String expectedMessage = "[http_method: POST] Error calling api.";

    MockResponse.builder()
        .withMethod(POST)
        .withURL(BASE_URL.concat(format(GET_BACKLOG_PROJECTION_TOTAL_URL, ARTW01)))
        .withStatusCode(OK.value())
        .withResponseHeader(HEADER_NAME, APPLICATION_JSON.toString())
        .withResponseBody(jsonResponseTotalBacklogProjection)
        .shouldFail();

    final TotalBacklogProjectionRequest request = mockTotalBacklogProjectionRequest();

    //WHEN
    final ClientException response = assertThrows(ClientException.class, () ->
        planningModelApiClient.getTotalBacklogProjection(LOGISTIC_CENTER_ID, request)
    );
    //THEN
    assertTrue(response.getMessage().contains(expectedMessage));
  }

  private BacklogProjectionRequest mockBacklogProjectionRequest() {

    final QuantityByDateOut processQuantity =
        new QuantityByDateOut(DATE_TO, PICKING_TOTAL);
    final ProcessPathByDateOut processPathByDateOut =
        new ProcessPathByDateOut(TOT_MONO, Set.of(processQuantity));
    final Process process =
        new Process(OutboundProcessName.PICKING, Set.of(processPathByDateOut));
    final Backlog backlog = new Backlog(Set.of(process));

    final QuantityByDateInOut quantityByDateInOut =
        new QuantityByDateInOut(DATE_FROM, DATE_TO, PICKING_TOTAL);
    final ProcessPathByDateInOut processPathByDateInOut =
        new ProcessPathByDateInOut(TOT_MONO, Set.of(quantityByDateInOut));
    final PlannedUnit plannedUnit = new PlannedUnit(Set.of(processPathByDateInOut));

    final QuantityByProcessName throughputQuantity =
        new QuantityByProcessName(OutboundProcessName.PICKING, PICKING_TOTAL);
    final Throughput throughput = new Throughput(DATE_FROM, Set.of(throughputQuantity));

    return new BacklogProjectionRequest(
        backlog,
        plannedUnit,
        Set.of(throughput),
        DATE_FROM,
        DATE_TO,
        FBM_WMS_OUTBOUND
    );
  }

  private TotalBacklogProjectionRequest mockTotalBacklogProjectionRequest() {

    final TotalBacklogProjectionRequest.Backlog.ProcessPathByDateOut.QuantityByDateOut processQuantity =
        new TotalBacklogProjectionRequest.Backlog.ProcessPathByDateOut.QuantityByDateOut(DATE_TO, PICKING_TOTAL);
    final TotalBacklogProjectionRequest.Backlog.ProcessPathByDateOut processPathByDateOut =
        new TotalBacklogProjectionRequest.Backlog.ProcessPathByDateOut(TOT_MONO, Set.of(processQuantity));
    final TotalBacklogProjectionRequest.Backlog backlog = new TotalBacklogProjectionRequest.Backlog(Set.of(processPathByDateOut));

    final TotalBacklogProjectionRequest.PlannedUnit.ProcessPathByDateInOut.QuantityByDateInOut quantityByDateInOut =
        new TotalBacklogProjectionRequest.PlannedUnit.ProcessPathByDateInOut.QuantityByDateInOut(DATE_FROM, DATE_TO, PICKING_TOTAL);
    final TotalBacklogProjectionRequest.PlannedUnit.ProcessPathByDateInOut processPathByDateInOut =
        new TotalBacklogProjectionRequest.PlannedUnit.ProcessPathByDateInOut(TOT_MONO, Set.of(quantityByDateInOut));
    final TotalBacklogProjectionRequest.PlannedUnit plannedUnit =
        new TotalBacklogProjectionRequest.PlannedUnit(Set.of(processPathByDateInOut));

    final TotalBacklogProjectionRequest.Throughput throughput = new TotalBacklogProjectionRequest.Throughput(DATE_FROM, PICKING_TOTAL);

    return new TotalBacklogProjectionRequest(
        DATE_FROM,
        DATE_TO,
        backlog,
        plannedUnit,
        Set.of(throughput)
    );
  }

  private void forecastMetadataEqualTo(final Metadata output,
                                       final String key,
                                       final String value) {
    assertEquals(key, output.key());
    assertEquals(value, output.value());
  }
}
