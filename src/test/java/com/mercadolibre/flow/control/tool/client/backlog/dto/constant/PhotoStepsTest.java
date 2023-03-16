package com.mercadolibre.flow.control.tool.client.backlog.dto.constant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PhotoStepsTest {

  private static final String PENDING = "pending";

  @Test
  void testFromGetName() {
    // GIVEN
    final PhotoSteps expectedBacklogPhotoSteps = PhotoSteps.PENDING;

    // WHEN
    final PhotoSteps backlogPhotoStepsPending = PhotoSteps.from(PENDING);

    // THEN
    assertEquals(expectedBacklogPhotoSteps, backlogPhotoStepsPending);
    assertEquals(PENDING, backlogPhotoStepsPending.getName());
  }
}
