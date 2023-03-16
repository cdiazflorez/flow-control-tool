package com.mercadolibre.flow.control.tool.client.planningmodelapi;

import static com.mercadolibre.flow.control.tool.client.config.RestPool.PLANNING_MODEL_API;
import static com.mercadolibre.flow.control.tool.client.planningmodelapi.PlanningModelApiUtils.GET_FORECAST_METADATA;
import static com.mercadolibre.flow.control.tool.client.planningmodelapi.PlanningModelApiUtils.createForecastMetadataParams;
import static java.lang.String.format;
import static org.springframework.http.HttpStatus.OK;

import com.mercadolibre.fbm.wms.outbound.commons.rest.HttpClient;
import com.mercadolibre.fbm.wms.outbound.commons.rest.HttpRequest;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.Metadata;
import com.mercadolibre.json.type.TypeReference;
import com.mercadolibre.restclient.MeliRestClient;
import com.newrelic.api.agent.Trace;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PlanningModelApiClient extends HttpClient {

  protected PlanningModelApiClient(final MeliRestClient restClient) {
    super(restClient, PLANNING_MODEL_API.name());
  }

  @Trace
  public List<Metadata> getForecastMetadata(final Workflow workflow,
                                            final String warehouseId,
                                            final ZonedDateTime dateFrom,
                                            final ZonedDateTime dateTo) {
    final HttpRequest request = HttpRequest.builder()
        .url(format(GET_FORECAST_METADATA, workflow))
        .GET()
        .queryParams(createForecastMetadataParams(warehouseId, dateFrom, dateTo))
        .acceptedHttpStatuses(Set.of(OK))
        .build();

    return send(
        request,
        response -> response.getData(new TypeReference<>() {
        }));
  }
}
