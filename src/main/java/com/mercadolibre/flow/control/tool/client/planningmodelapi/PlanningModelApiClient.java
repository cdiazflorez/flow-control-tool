package com.mercadolibre.flow.control.tool.client.planningmodelapi;

import static com.mercadolibre.flow.control.tool.client.config.RestPool.PLANNING_MODEL_API;
import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static org.springframework.http.HttpStatus.OK;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercadolibre.fbm.wms.outbound.commons.rest.HttpClient;
import com.mercadolibre.fbm.wms.outbound.commons.rest.HttpRequest;
import com.mercadolibre.fbm.wms.outbound.commons.rest.RequestBodyHandler;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.EntityType;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.PlanningWorkflow;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.EntityDataDto;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.EntityRequestDto;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.Metadata;
import com.mercadolibre.json.type.TypeReference;
import com.mercadolibre.restclient.MeliRestClient;
import com.mercadolibre.restclient.exception.ParseException;
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

  private static final String GET_ALL_STAFFING_DATA = "/planning/model/workflows/%s/entities/search";

  private static final String WAREHOUSE_ID = "warehouse_id";

  private static final String VIEW_DATE = "date_time";

  private final ObjectMapper objectMapper;

  protected PlanningModelApiClient(final MeliRestClient restClient, final ObjectMapper objectMapper) {
    super(restClient, PLANNING_MODEL_API.name());
    this.objectMapper = objectMapper;
  }

  @Trace
  public List<Metadata> getForecastMetadata(final PlanningWorkflow planningWorkflow,
                                            final String logisticCenterId,
                                            final ZonedDateTime viewDate) {
    final HttpRequest request = HttpRequest.builder()
        .url(format(GET_FORECAST_METADATA, planningWorkflow))
        .GET()
        .queryParams(createForecastMetadataParams(logisticCenterId, viewDate))
        .acceptedHttpStatuses(Set.of(OK))
        .build();

    return send(
        request,
        response -> response.getData(new TypeReference<>() {
        }));
  }

  @Trace
  public Map<EntityType, List<EntityDataDto>> searchEntities(final EntityRequestDto entityRequestDto) {
    final HttpRequest request = HttpRequest.builder()
        .url(format(GET_ALL_STAFFING_DATA, entityRequestDto.workflow()))
        .POST(requestSupplier(entityRequestDto))
        .acceptedHttpStatuses(Set.of(OK))
        .build();
    return send(
        request,
        response -> response.getData(new TypeReference<>() {
        })
    );
  }

  private <T> RequestBodyHandler requestSupplier(final T requestBody) {
    return () -> {
      try {
        return objectMapper.writeValueAsBytes(requestBody);
      } catch (JsonProcessingException e) {
        throw new ParseException(e);
      }
    };
  }

  private Map<String, String> createForecastMetadataParams(final String logisticCenterId,
                                                           final ZonedDateTime viewDate) {
    final Map<String, String> params = new ConcurrentHashMap<>();
    params.put(WAREHOUSE_ID, logisticCenterId);
    params.put(VIEW_DATE, viewDate.format(ISO_OFFSET_DATE_TIME));
    return params;
  }
}
