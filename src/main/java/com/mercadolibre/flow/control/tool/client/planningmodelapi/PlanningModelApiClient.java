package com.mercadolibre.flow.control.tool.client.planningmodelapi;

import static com.mercadolibre.flow.control.tool.client.config.RestPool.PLANNING_MODEL_API;
import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static org.springframework.http.HttpStatus.OK;

import com.mercadolibre.fbm.wms.outbound.commons.rest.HttpClient;
import com.mercadolibre.fbm.wms.outbound.commons.rest.HttpRequest;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.Metadata;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import com.mercadolibre.json.type.TypeReference;
import com.mercadolibre.restclient.MeliRestClient;
import com.newrelic.api.agent.Trace;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PlanningModelApiClient extends HttpClient {

  private static final String GET_FORECAST_METADATA = "/planning/model/workflows/%s/metadata";
  private static final String WAREHOUSE_ID = "warehouse_id";
  private static final String VIEW_DATE = "date_time";

  protected PlanningModelApiClient(final MeliRestClient restClient) {
    super(restClient, PLANNING_MODEL_API.name());
  }

  @Trace
  public List<Metadata> getForecastMetadata(final Workflow workflow,
                                            final String logisticCenterId,
                                            final ZonedDateTime viewDate) {
    final HttpRequest request = HttpRequest.builder()
        .url(format(GET_FORECAST_METADATA, workflow))
        .GET()
        .queryParams(createForecastMetadataParams(logisticCenterId, viewDate))
        .acceptedHttpStatuses(Set.of(OK))
        .build();

    return send(
        request,
        response -> response.getData(new TypeReference<>() {
        }));
  }

  private Map<String, String> createForecastMetadataParams(final String logisticCenterId,
                                                           final ZonedDateTime viewDate) {
    final Map<String, String> params = new ConcurrentHashMap<>();
    params.put(WAREHOUSE_ID, logisticCenterId);
    params.put(VIEW_DATE, viewDate.format(ISO_OFFSET_DATE_TIME));
    return params;
  }
}
