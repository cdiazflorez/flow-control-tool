package com.mercadolibre.flow.control.tool.feature.deferral;

import static com.mercadolibre.flow.control.tool.util.TestUtils.DATE_FROM;
import static com.mercadolibre.flow.control.tool.util.TestUtils.DATE_TO;
import static com.mercadolibre.flow.control.tool.util.TestUtils.LOGISTIC_CENTER_ID;
import static com.mercadolibre.flow.control.tool.util.TestUtils.getResourceAsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mercadolibre.flow.control.tool.integration.ControllerTest;
import java.time.Instant;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class DeferralControllerTest extends ControllerTest {

  private static final String MAXIMUM_CAPACITY_OPERATION_URL = "/control_tool/logistic_center/%s/staffing/throughput/max_capacity/plan";
  private static final String MAXIMUM_CAPACITY_OPERATION_BAD_URL =
      "/control_tool/logistic_center/%s/staffing/throughput/max_capacity/planned";

  private static final String WORKFLOW_PARAM = "workflow";

  private static final String FBM_WMS_OUTBOUND = "FBM_WMS_OUTBOUND";

  private static final String DATE_FROM_PARAM = "date_from";

  private static final String DATE_TO_PARAM = "date_to";


  @Autowired
  private MockMvc mvc;

  @Test
  void testGetMaximumCapacityResponseOk() throws Exception {

    //WHEN
    final var result = mvc.perform(
        get(String.format(MAXIMUM_CAPACITY_OPERATION_URL, LOGISTIC_CENTER_ID))
            .param(WORKFLOW_PARAM, FBM_WMS_OUTBOUND)
            .param(DATE_FROM_PARAM, DATE_FROM.toString())
            .param(DATE_TO_PARAM, DATE_TO.toString()));

    //THEN
    result.andExpect(status().isOk())
        .andExpect(content().json(getResourceAsString("staffing/controller_response_get_max_cap.json")));
  }

  @ParameterizedTest
  @MethodSource("urlAndStatusProvider")
  void testGetMaximumCapacity(final String url, final int expectedStatus, final Instant dateFrom, final Instant dateTo) throws Exception {
    // WHEN
    final var result = mvc.perform(
        get(url)
            .param(WORKFLOW_PARAM, FBM_WMS_OUTBOUND)
            .param(DATE_FROM_PARAM, dateFrom.toString())
            .param(DATE_TO_PARAM, dateTo.toString()));

    // THEN
    result.andExpect(status().is(expectedStatus));
  }

  private static Stream<Arguments> urlAndStatusProvider() {
    return Stream.of(
        Arguments.of(String.format(MAXIMUM_CAPACITY_OPERATION_URL, LOGISTIC_CENTER_ID), 200, DATE_FROM, DATE_TO),
        Arguments.of(String.format(MAXIMUM_CAPACITY_OPERATION_URL, LOGISTIC_CENTER_ID), 400, DATE_TO, DATE_FROM),
        Arguments.of(String.format(MAXIMUM_CAPACITY_OPERATION_BAD_URL, LOGISTIC_CENTER_ID), 404, DATE_FROM, DATE_TO)
    );
  }
}
