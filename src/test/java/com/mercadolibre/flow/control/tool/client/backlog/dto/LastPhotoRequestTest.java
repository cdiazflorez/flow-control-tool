package com.mercadolibre.flow.control.tool.client.backlog.dto;

import static com.mercadolibre.flow.control.tool.client.backlog.BacklogApiClientMockUtils.VIEW_DATE;
import static com.mercadolibre.flow.control.tool.client.backlog.BacklogApiClientMockUtils.mockBacklogPhotosLastRequest;
import static com.mercadolibre.flow.control.tool.client.backlog.BacklogApiClientMockUtils.mockListOfBacklogPhotoSteps;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.BacklogPhotoConstants.BacklogPhotoGrouper.AREA;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.BacklogPhotoConstants.BacklogPhotoGrouper.PATH;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.BacklogPhotoConstants.BacklogPhotoGrouper.STEP;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.BacklogPhotoConstants.BacklogPhotoWorkflows.FBM_WMS_OUTBOUND;
import static com.mercadolibre.flow.control.tool.util.TestUtils.LOGISTIC_CENTER_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * BacklogPhotoLastRequest instance test.
 */
class LastPhotoRequestTest {

  @Test
  void testBacklogPhotosLastRequestValues() {

    // GIVEN
    final LastPhotoRequest lastPhotoRequest = mockBacklogPhotosLastRequest();
    final Set<BacklogPhotoConstants.BacklogPhotoSteps> expectedBacklogPhotoSteps = mockListOfBacklogPhotoSteps();

    // THEN
    assertEquals(VIEW_DATE, lastPhotoRequest.photoDateTo());
    assertEquals(LOGISTIC_CENTER_ID, lastPhotoRequest.logisticCenterId());
    assertEquals(Set.of(FBM_WMS_OUTBOUND), lastPhotoRequest.workflows());
    assertEquals(Set.of(STEP, AREA, PATH), lastPhotoRequest.groupBy());
    assertEquals(expectedBacklogPhotoSteps, lastPhotoRequest.steps());
  }
}
