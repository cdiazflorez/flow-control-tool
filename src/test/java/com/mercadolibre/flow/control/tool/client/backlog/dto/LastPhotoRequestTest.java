package com.mercadolibre.flow.control.tool.client.backlog.dto;

import static com.mercadolibre.flow.control.tool.client.backlog.BacklogApiClientMockUtils.mockBacklogPhotosLastRequest;
import static com.mercadolibre.flow.control.tool.client.backlog.BacklogApiClientMockUtils.mockListOfBacklogPhotoSteps;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoGrouper.AREA;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoGrouper.PATH;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoGrouper.STEP;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoWorkflow.FBM_WMS_OUTBOUND;
import static com.mercadolibre.flow.control.tool.util.TestUtils.LOGISTIC_CENTER_ID;
import static com.mercadolibre.flow.control.tool.util.TestUtils.VIEW_DATE_INSTANT;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep;
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
    final Set<PhotoStep> expectedBacklogPhotoSteps = mockListOfBacklogPhotoSteps();

    // THEN
    assertEquals(VIEW_DATE_INSTANT, lastPhotoRequest.photoDateTo());
    assertEquals(LOGISTIC_CENTER_ID, lastPhotoRequest.logisticCenterId());
    assertEquals(Set.of(FBM_WMS_OUTBOUND), lastPhotoRequest.workflows());
    assertEquals(Set.of(STEP, AREA, PATH), lastPhotoRequest.groupBy());
    assertEquals(expectedBacklogPhotoSteps, lastPhotoRequest.steps());
  }
}
