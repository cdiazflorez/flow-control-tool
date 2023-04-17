package com.mercadolibre.flow.control.tool.client.backlog.dto;

import static com.mercadolibre.flow.control.tool.util.TestUtils.getResourceAsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * BacklogPhotoResponse instance test.
 */
class PhotoResponseTest {

  @Test
  void testBacklogPhotosLastResponseValues() throws JsonProcessingException {

    // GIVEN
    final ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    final PhotoResponse photoResponse = objectMapper.readValue(
        getResourceAsString("client/response_get_backlog_api_photos_last.json"),
        PhotoResponse.class
    );
    final int expectedFirstTotal = 415;

    // THEN
    assertNotNull(photoResponse.takenOn());
    assertNotNull(photoResponse.groups());
    assertNotNull(photoResponse.groups().get(0).key());
    assertEquals(expectedFirstTotal, photoResponse.groups().get(0).total());
  }

  @Test
  void testBacklogPhotosResponseValues() throws JsonProcessingException {

    // GIVEN
    final ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    final List<PhotoResponse> photoResponse = objectMapper.readValue(
        getResourceAsString("client/response_get_backlog_api_photos.json"),
        objectMapper.getTypeFactory().constructCollectionType(List.class, PhotoResponse.class)
    );

    final int expectedFirstTotal = 217;

    // THEN
    assertNotNull(photoResponse.get(0).takenOn());
    assertNotNull(photoResponse.get(1).groups());
    assertNotNull(photoResponse.get(0).groups().get(0).key());
    assertEquals(expectedFirstTotal, photoResponse.get(1).groups().get(0).total());
  }
}
