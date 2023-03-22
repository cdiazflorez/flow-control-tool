package com.mercadolibre.flow.control.tool.client.backlog.dto.constant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PhotoGrouperTest {

  private static final String AREA = "area";

  @Test
  void testFromGetName() {
    // GIVEN
    final PhotoGrouper expectedPhotoGrouper = PhotoGrouper.AREA;

    // WHEN
    final PhotoGrouper photoGrouperArea = PhotoGrouper.from(AREA);

    // THEN
    assertEquals(expectedPhotoGrouper, photoGrouperArea);
    assertEquals(AREA, photoGrouperArea.getName());
  }
}
