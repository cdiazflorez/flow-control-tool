package com.mercadolibre.flow.control.tool.client.config;

import com.mercadolibre.flow.control.tool.client.config.RestClientConfig.BacklogApiClientProperties;
import com.mercadolibre.flow.control.tool.client.config.RestClientConfig.PlanningModelApiClientProperties;
import com.mercadolibre.restclient.MeliRestClient;
import com.mercadolibre.restclient.mock.RequestMockHolder;
import java.io.IOException;

/**
 * Class containing several methods that can be used across any client testing.
 */
public class RestClientTestUtils {

  protected static final String BASE_URL = "https://fbm-flow-stage.melioffice.com";

  /**
   * Get Rest Client Base for client testing. Each new client test, must be configured here
   *
   * @return RestClientConfig using known values and Client Properties
   */
  protected MeliRestClient getRestClientTest() throws IOException {

    final BacklogApiClientProperties backlogApiClientProperties = new BacklogApiClientProperties();
    backlogApiClientProperties.setBaseUrl(BASE_URL);

    final PlanningModelApiClientProperties planningModelApiClientProperties =
        new PlanningModelApiClientProperties();
    planningModelApiClientProperties.setBaseUrl(BASE_URL);

    return new RestClientConfig(
        backlogApiClientProperties,
        planningModelApiClientProperties
    ).restClient();
  }

  public void cleanMocks() {
    RequestMockHolder.clear();
  }
}
