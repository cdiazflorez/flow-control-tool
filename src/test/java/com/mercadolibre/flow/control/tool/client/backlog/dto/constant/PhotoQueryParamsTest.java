package com.mercadolibre.flow.control.tool.client.backlog.dto.constant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PhotoQueryParamsTest {

  private static final String PHOTO_DATE_TO = "photo_date_to";

  @Test
  void testFromGetName() {
    // GIVEN
    final PhotoQueryParam expectedBacklogPhotoQueryParams = PhotoQueryParam.PHOTO_DATE_TO;

    // WHEN
    final PhotoQueryParam backlogPhotoQueryParamsDate = PhotoQueryParam.from(PHOTO_DATE_TO);

    // THEN
    assertEquals(expectedBacklogPhotoQueryParams, backlogPhotoQueryParamsDate);
    assertEquals(PHOTO_DATE_TO, backlogPhotoQueryParamsDate.getName());
  }
}
