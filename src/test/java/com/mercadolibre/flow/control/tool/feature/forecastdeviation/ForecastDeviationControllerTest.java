package com.mercadolibre.flow.control.tool.feature.forecastdeviation;

import static com.mercadolibre.flow.control.tool.util.TestUtils.FBM_WMS_OUTBOUND;
import static com.mercadolibre.flow.control.tool.util.TestUtils.LOGISTIC_CENTER_ID;
import static com.mercadolibre.flow.control.tool.util.TestUtils.getResourceAsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ForecastDeviationController.class)
class ForecastDeviationControllerTest {

  private static final String FORECAST_DEVIATION_URL = "/control_tool/logistic_center/%s/forecast_deviation";


  private static final String WORKFLOW = "workflow";

  private static final String VIEW_DATE = "view_date";

  private static final String DATE_FROM = "date_from";

  private static final String DATE_TO = "date_to";

  private static final String FILTER_BY = "filter_by";

  private static final String FILTER_DATE_IN = "date_in";

  private static final String VIEW_DATE_STRING = "2023-03-23T10:00:00Z";

  private static final String DATE_FROM_STRING = "2023-03-23T07:00:00Z";

  private static final String DATE_TO_STRING = "2023-03-24T08:00:00Z";


  @Autowired
  private MockMvc mvc;

  @Test
  void forecastDeviationTest() throws Exception {

    // WHEN
    final var result = mvc.perform(
        get(String.format(FORECAST_DEVIATION_URL, LOGISTIC_CENTER_ID))
            .param(WORKFLOW, FBM_WMS_OUTBOUND)
            .param(DATE_FROM, DATE_FROM_STRING)
            .param(DATE_TO, DATE_TO_STRING)
            .param(VIEW_DATE, VIEW_DATE_STRING)
            .param(FILTER_BY, FILTER_DATE_IN)
    );

    // THEN
    result.andExpect(status().isOk())
        .andExpect(
            content()
                .json(getResourceAsString("forecastdeviation/controller_response_get_forecast_deviation.json"))
        );


  }

  @Test
  void forecastDeviationErrorTest() throws Exception {

    // WHEN
    final var result = mvc.perform(
        get(String.format(FORECAST_DEVIATION_URL, LOGISTIC_CENTER_ID))
            .param(WORKFLOW, "FBM_WM_OUTBOUND")
            .param(DATE_FROM, DATE_FROM_STRING)
            .param(DATE_TO, DATE_TO_STRING)
            .param(VIEW_DATE, VIEW_DATE_STRING)
            .param(FILTER_BY, FILTER_DATE_IN)
    );

    // THEN
    result.andExpect(status().isBadRequest());
  }

  @Test
  void forecastDeviationFilterNotSupportedExceptionTest() throws Exception {
    // GIVEN
    final String expectedMessage = new JSONObject()
        .put("error", "bad_request")
        .put("message",
            "bad request /control_tool/logistic_center/ARTW01/forecast_deviation. "
                + "Allowed values are: [DATE_IN, DATE_OUT]")
        .put("status", 400)
        .toString();

    // WHEN
    final var result = mvc.perform(
        get(String.format(FORECAST_DEVIATION_URL, LOGISTIC_CENTER_ID))
            .param(WORKFLOW, FBM_WMS_OUTBOUND)
            .param(DATE_FROM, DATE_FROM_STRING)
            .param(DATE_TO, DATE_TO_STRING)
            .param(VIEW_DATE, VIEW_DATE_STRING)
            .param(FILTER_BY, "error")
    );

    // THEN
    result.andExpect(status().isBadRequest()).andExpect(content().json(expectedMessage));
  }

}
