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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoGrouper;
import com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * BacklogPhotoLastRequest instance test.
 */
class LastPhotoRequestTest {

  private static final Instant DATE_FROM = Instant.parse("2023-09-05T10:00:00Z");

  private static final Instant DATE_TO = Instant.parse("2023-09-05T15:00:00Z");

  private static final List<String> OPTIONAL_QUERY_PARAMETERS = List.of("date_in_from", "date_in_to", "date_out_from", "date_out_to");

  private static Stream<Arguments> photoLastArgumentsTest() {
    return Stream.of(
        Arguments.of(PhotoGrouper.DATE_IN, DATE_FROM, DATE_TO, DATE_FROM, DATE_TO),
        Arguments.of(PhotoGrouper.DATE_OUT, DATE_FROM, DATE_TO, DATE_FROM, null),
        Arguments.of(PhotoGrouper.DATE_OUT, DATE_FROM, DATE_TO, null, DATE_TO),
        Arguments.of(PhotoGrouper.DATE_OUT, DATE_FROM, null, DATE_FROM, DATE_TO),
        Arguments.of(PhotoGrouper.DATE_OUT, null, DATE_TO, DATE_FROM, DATE_TO),
        Arguments.of(PhotoGrouper.DATE_OUT, null, null, null, null)
    );
  }

  @Test
  void testBacklogPhotosLastRequestValues() {

    // GIVEN
    final LastPhotoRequest lastPhotoRequest = mockBacklogPhotosLastRequest();
    final Set<PhotoStep> expectedBacklogPhotoSteps = mockListOfBacklogPhotoSteps();

    // THEN
    assertEquals(VIEW_DATE_INSTANT, lastPhotoRequest.getPhotoDateTo());
    assertEquals(LOGISTIC_CENTER_ID, lastPhotoRequest.getLogisticCenterId());
    assertEquals(Set.of(FBM_WMS_OUTBOUND), lastPhotoRequest.getWorkflows());
    assertEquals(Set.of(STEP, AREA, PATH), lastPhotoRequest.getGroupBy());
    assertEquals(expectedBacklogPhotoSteps, lastPhotoRequest.getSteps());
  }

  @ParameterizedTest
  @MethodSource("photoLastArgumentsTest")
  void photoLastRequestToRealSalesTest(final PhotoGrouper groupBy,
                                       final Instant dateInFrom,
                                       final Instant dateInTo,
                                       final Instant dateOutFrom,
                                       final Instant dateOutTo) {
    // GIVE
    final LastPhotoRequest lastPhotoRequest = new LastPhotoRequest(
        LOGISTIC_CENTER_ID,
        Set.of(FBM_WMS_OUTBOUND),
        Set.of(groupBy),
        Set.of(),
        VIEW_DATE_INSTANT,
        dateInFrom,
        dateInTo,
        dateOutFrom,
        dateOutTo
    );

    // THEN
    assertTrue(lastPhotoRequest.getSteps().isEmpty());
    assertEquals(Set.of(groupBy), lastPhotoRequest.getGroupBy());

    if (dateInFrom != null && dateInTo != null && dateOutFrom != null && dateOutTo != null) {
      OPTIONAL_QUERY_PARAMETERS.forEach(key -> assertTrue(lastPhotoRequest.getQueryParams().containsKey(key)));
    }

    if (dateInFrom == null && dateInTo == null && dateOutFrom == null && dateOutTo == null) {
      OPTIONAL_QUERY_PARAMETERS.forEach(key -> assertFalse(lastPhotoRequest.getQueryParams().containsKey(key)));
    }

    if (dateInFrom == null) {
      assertFalse(lastPhotoRequest.getQueryParams().containsKey(OPTIONAL_QUERY_PARAMETERS.get(0)));
      assertFalse(lastPhotoRequest.getQueryParams().containsKey(OPTIONAL_QUERY_PARAMETERS.get(1)));
    }

    if (dateInTo == null) {
      assertFalse(lastPhotoRequest.getQueryParams().containsKey(OPTIONAL_QUERY_PARAMETERS.get(0)));
      assertFalse(lastPhotoRequest.getQueryParams().containsKey(OPTIONAL_QUERY_PARAMETERS.get(1)));
    }

    if (dateOutFrom == null) {
      assertFalse(lastPhotoRequest.getQueryParams().containsKey(OPTIONAL_QUERY_PARAMETERS.get(2)));
      assertFalse(lastPhotoRequest.getQueryParams().containsKey(OPTIONAL_QUERY_PARAMETERS.get(3)));
    }

    if (dateOutTo == null) {
      assertFalse(lastPhotoRequest.getQueryParams().containsKey(OPTIONAL_QUERY_PARAMETERS.get(2)));
      assertFalse(lastPhotoRequest.getQueryParams().containsKey(OPTIONAL_QUERY_PARAMETERS.get(3)));
    }

  }
}
