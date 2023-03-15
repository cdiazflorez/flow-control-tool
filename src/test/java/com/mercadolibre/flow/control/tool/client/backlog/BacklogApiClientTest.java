package com.mercadolibre.flow.control.tool.client.backlog;

import static com.mercadolibre.flow.control.tool.client.backlog.BacklogApiClientMockUtils.BACKLOG_PHOTO_LAST_URL;
import static com.mercadolibre.flow.control.tool.client.backlog.BacklogApiClientMockUtils.VIEW_DATE;
import static com.mercadolibre.flow.control.tool.client.backlog.BacklogApiClientMockUtils.mockBacklogPhotosLastRequest;
import static com.mercadolibre.flow.control.tool.util.TestUtils.LOGISTIC_CENTER_ID;
import static com.mercadolibre.flow.control.tool.util.TestUtils.getResourceAsString;
import static com.mercadolibre.restclient.http.ContentType.APPLICATION_JSON;
import static com.mercadolibre.restclient.http.ContentType.HEADER_NAME;
import static com.mercadolibre.restclient.http.HttpMethod.GET;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.OK;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mercadolibre.fbm.wms.outbound.commons.rest.exception.ClientException;
import com.mercadolibre.flow.control.tool.client.backlog.dto.LastPhotoRequest;
import com.mercadolibre.flow.control.tool.client.backlog.dto.PhotoResponse;
import com.mercadolibre.flow.control.tool.client.config.RestClientTestUtils;
import com.mercadolibre.restclient.MockResponse;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Class containing the BacklogApiClient tests.
 */
class BacklogApiClientTest extends RestClientTestUtils {

  private BacklogApiClient backlogApiClient;

  @BeforeEach
  void setUp() throws IOException {
    backlogApiClient = new BacklogApiClient(getRestClientTest());
  }

  @AfterEach
  void cleanUp() {
    super.cleanMocks();
  }

  /**
   * Test the getBacklogPhotosLast method in BacklogApiClient.
   * It uses a known JSON response file to mock the Client response.
   */
  @Test
  void testGetBacklogPhotosLast() throws IOException {

    // GIVEN
    final String jsonResponseBacklogPhotosLast = getResourceAsString(
        "client/response_get_backlog_api_photos_last.json"
    );
    final ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    final PhotoResponse expectedPhotoResponse = objectMapper.readValue(
        jsonResponseBacklogPhotosLast,
        PhotoResponse.class
    );

    MockResponse.builder()
        .withMethod(GET)
        .withURL(format(BASE_URL + BACKLOG_PHOTO_LAST_URL, LOGISTIC_CENTER_ID))
        .withStatusCode(OK.value())
        .withResponseHeader(HEADER_NAME, APPLICATION_JSON.toString())
        .withResponseBody(jsonResponseBacklogPhotosLast)
        .build();

    // WHEN
    final PhotoResponse photoResponse = backlogApiClient.getLastPhoto(mockBacklogPhotosLastRequest());

    // THEN
    assertEquals(expectedPhotoResponse, photoResponse);
    assertEquals(VIEW_DATE, photoResponse.takenOn());
  }

  /**
   * Test the getBacklogPhotosLast method in BacklogApiClient when it fails.
   */
  @Test
  void testGetBacklogPhotosLastException() throws IOException {

    // GIVEN
    final String jsonResponseBacklogPhotosLast = getResourceAsString(
        "client/response_get_backlog_api_photos_last.json"
    );
    String expectedMessage = "[http_method: GET] Error calling api.";
    LastPhotoRequest lastPhotoRequest = mockBacklogPhotosLastRequest();
    MockResponse.builder()
        .withMethod(GET)
        .withURL(format(BASE_URL + BACKLOG_PHOTO_LAST_URL, LOGISTIC_CENTER_ID))
        .withStatusCode(OK.value())
        .withResponseHeader(HEADER_NAME, APPLICATION_JSON.toString())
        .withResponseBody(jsonResponseBacklogPhotosLast)
        .shouldFail();

    // WHEN
    final ClientException response = assertThrows(ClientException.class, () ->
        backlogApiClient.getLastPhoto(lastPhotoRequest));

    // THEN
    assertTrue(response.getMessage().contains(expectedMessage));
  }

}