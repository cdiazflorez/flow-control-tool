package com.mercadolibre.flow.control.tool.client.backlog.dto;

import static com.mercadolibre.flow.control.tool.client.backlog.BacklogApiClientMockUtils.mockBacklogPhotosRequest;
import static com.mercadolibre.flow.control.tool.client.backlog.BacklogApiClientMockUtils.mockListOfBacklogPhotoSteps;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoGrouper.AREA;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoGrouper.PATH;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoGrouper.STEP;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoWorkflow.FBM_WMS_OUTBOUND;
import static com.mercadolibre.flow.control.tool.util.TestUtils.DATE_FROM;
import static com.mercadolibre.flow.control.tool.util.TestUtils.DATE_TO;
import static com.mercadolibre.flow.control.tool.util.TestUtils.LOGISTIC_CENTER_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class PhotoRequestTest {

  @Test
  void testBacklogPhotosRequestValues() {

    // GIVEN
    final PhotoRequest photoRequest = mockBacklogPhotosRequest();
    final Set<PhotoStep> expectedBacklogPhotoSteps = mockListOfBacklogPhotoSteps();

    // THEN
    assertEquals(LOGISTIC_CENTER_ID, photoRequest.logisticCenterId());
    assertEquals(Set.of(FBM_WMS_OUTBOUND), photoRequest.workflows());
    assertEquals(Set.of(STEP, AREA, PATH), photoRequest.groupBy());
    assertEquals(DATE_FROM, photoRequest.dateFrom());
    assertEquals(DATE_TO, photoRequest.dateTo());
    assertEquals(expectedBacklogPhotoSteps, photoRequest.steps());
  }
}
