package com.mercadolibre.flow.control.tool.client.planningmodelapi.dto;

import static com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.PlannedGrouper.DATE_IN;
import static com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.PlanningWorkflow.FBM_WMS_OUTBOUND;
import static com.mercadolibre.flow.control.tool.util.TestUtils.LOGISTIC_CENTER_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class BacklogPlannedRequestTest {

  private static final Instant DATE_FROM = Instant.parse("2023-09-05T10:00:00Z");

  private static final Instant DATE_TO = Instant.parse("2023-09-05T15:00:00Z");

  private static final List<String> OPTIONAL_QUERY_PARAMETERS = List.of("date_out_from", "date_out_to");

  private static Stream<Arguments> plannedArgumentsTest() {
    return Stream.of(
        Arguments.of(DATE_FROM, DATE_TO),
        Arguments.of(DATE_FROM, null),
        Arguments.of(null, DATE_TO),
        Arguments.of(null, null)
    );
  }

  @ParameterizedTest
  @MethodSource("plannedArgumentsTest")
  void testBacklogPlannedRequestValues(final Instant dateOutFrom, final Instant dateOutTo) {
    final BacklogPlannedRequest backlogPlannedRequest = BacklogPlannedRequest.builder()
        .logisticCenter(LOGISTIC_CENTER_ID)
        .planningWorkflow(FBM_WMS_OUTBOUND)
        .groupBy(Set.of(DATE_IN))
        .processPathNames(Set.of(ProcessPathName.GLOBAL))
        .dateInFrom(DATE_FROM)
        .dateInTo(DATE_TO)
        .dateOutFrom(dateOutFrom)
        .dateOutTo(dateOutTo)
        .build();


    assertEquals(LOGISTIC_CENTER_ID, backlogPlannedRequest.getLogisticCenter());
    assertEquals(FBM_WMS_OUTBOUND, backlogPlannedRequest.getPlanningWorkflow());
    assertEquals(Set.of(DATE_IN), backlogPlannedRequest.getGroupBy());
    assertEquals(Set.of(ProcessPathName.GLOBAL), backlogPlannedRequest.getProcessPathNames());
    assertEquals(DATE_FROM, backlogPlannedRequest.getDateInFrom());
    assertEquals(DATE_TO, backlogPlannedRequest.getDateInTo());


    if (dateOutFrom != null && dateOutTo != null) {
      assertEquals(DATE_FROM, backlogPlannedRequest.getDateOutFrom());
      assertEquals(DATE_TO, backlogPlannedRequest.getDateOutTo());
      OPTIONAL_QUERY_PARAMETERS.forEach(key -> assertTrue(backlogPlannedRequest.getQueryParams().containsKey(key)));
    }

    if (dateOutFrom == null || dateOutTo == null) {
      OPTIONAL_QUERY_PARAMETERS.forEach(key -> assertFalse(backlogPlannedRequest.getQueryParams().containsKey(key)));
    }

  }

}
