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
import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.OutboundProcessName;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.PlanningWorkflow;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.BacklogPlannedRequest;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.BacklogPlannedResponse;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.BacklogProjectionRequest;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.BacklogProjectionResponse;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.EntityDataDto;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.EntityRequestDto;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.Metadata;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.TotalBacklogProjectionRequest;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.TotalBacklogProjectionResponse;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import com.mercadolibre.json.type.TypeReference;
import com.mercadolibre.restclient.MeliRestClient;
import com.mercadolibre.restclient.exception.ParseException;
import com.newrelic.api.agent.Trace;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PlanningModelApiClient extends HttpClient {

  private static final String GET_THROUGHPUT_BY_PP_PROCESS_DATE_URL = "/logistic_center/%s/plan/staffing/throughput";

  private static final String GET_BACKLOG_PROJECTION_URL = "/logistic_center/%s/projections/backlog";

  private static final String GET_FORECAST_METADATA_URL = "/planning/model/workflows/%s/metadata";

  private static final String GET_ALL_STAFFING_DATA_URL = "/planning/model/workflows/%s/entities/search";

  private static final String GET_BACKLOG_PLANNED_URL = "/logistic_center/%s/plan/units";

  private static final String GET_BACKLOG_PROJECTION_TOTAL_URL = GET_BACKLOG_PROJECTION_URL + "/total";

  private static final String WAREHOUSE_ID = "warehouse_id";

  private static final String DATE_TIME = "date_time";

  private static final String WORKFLOW = "workflow";

  private static final String DATE_FROM = "date_from";

  private static final String DATE_TO = "date_to";

  private static final String PROCESS_PATHS = "process_paths";

  private static final String PROCESSES = "processes";

  private final ObjectMapper objectMapper;

  protected PlanningModelApiClient(final MeliRestClient restClient, final ObjectMapper objectMapper) {
    super(restClient, PLANNING_MODEL_API.name());
    this.objectMapper = objectMapper;
  }

  @Trace
  public List<BacklogProjectionResponse> getBacklogProjection(
      final String logisticCenterId,
      final BacklogProjectionRequest backlogProjectionRequest
  ) {
    final HttpRequest request = HttpRequest.builder()
        .url(format(GET_BACKLOG_PROJECTION_URL, logisticCenterId))
        .POST(requestSupplier(backlogProjectionRequest))
        .acceptedHttpStatuses(Set.of(OK))
        .build();

    return send(
        request,
        response -> response.getData(new TypeReference<>() {
        }));
  }

  @Trace
  public List<TotalBacklogProjectionResponse> getTotalBacklogProjection(
      final String logisticCenterId,
      final TotalBacklogProjectionRequest totalBacklogProjectionRequest
  ) {
    final HttpRequest request = HttpRequest.builder()
        .url(format(GET_BACKLOG_PROJECTION_TOTAL_URL, logisticCenterId))
        .POST(requestSupplier(totalBacklogProjectionRequest))
        .acceptedHttpStatuses(Set.of(OK))
        .build();

    return send(
        request,
        response -> response.getData(new TypeReference<>() {
        }));
  }

  @Trace
  public Map<ProcessPathName, Map<OutboundProcessName, Map<Instant, Throughput>>> getThroughputByPPAndProcessAndDate(
      final Workflow workflow,
      final String logisticCenterId,
      final Instant dateFrom,
      final Instant dateTo,
      final Set<ProcessName> process,
      final Set<ProcessPathName> processPathNames
  ) {
    final HttpRequest request = HttpRequest.builder()
        .url(format(GET_THROUGHPUT_BY_PP_PROCESS_DATE_URL, logisticCenterId))
        .GET()
        .queryParams(createQueryParamsToGetThroughputByPPAndProcessAndDate(
            workflow,
            dateFrom,
            dateTo,
            process,
            processPathNames
        ))
        .acceptedHttpStatuses(Set.of(OK))
        .build();

    return send(
        request,
        response -> response.getData(new TypeReference<>() {
        }));

  }

  @Trace
  public List<Metadata> getForecastMetadata(final PlanningWorkflow planningWorkflow,
                                            final String logisticCenterId,
                                            final ZonedDateTime viewDate) {
    final HttpRequest request = HttpRequest.builder()
        .url(format(GET_FORECAST_METADATA_URL, planningWorkflow))
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
        .url(format(GET_ALL_STAFFING_DATA_URL, entityRequestDto.workflow()))
        .POST(requestSupplier(entityRequestDto))
        .acceptedHttpStatuses(Set.of(OK))
        .build();
    return send(
        request,
        response -> response.getData(new TypeReference<>() {
        })
    );
  }

  @Trace
  public List<BacklogPlannedResponse> getBacklogPlanned(final BacklogPlannedRequest backlogPlannedRequest) {
    final HttpRequest request = HttpRequest.builder()
        .url(format(GET_BACKLOG_PLANNED_URL, backlogPlannedRequest.logisticCenter()))
        .GET()
        .queryParams(backlogPlannedRequest.getQueryParams())
        .acceptedHttpStatuses(Set.of(OK))
        .build();

    return send(
        request,
        response -> response.getData(new TypeReference<>() {
        }));
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
    params.put(DATE_TIME, viewDate.format(ISO_OFFSET_DATE_TIME));
    return params;
  }

  private Map<String, String> createQueryParamsToGetThroughputByPPAndProcessAndDate(
      final Workflow workflow,
      final Instant dateFrom,
      final Instant dateTo,
      final Set<ProcessName> process,
      final Set<ProcessPathName> processPathNames
  ) {
    final Map<String, String> params = new ConcurrentHashMap<>();
    params.put(WORKFLOW, workflow.getName());
    params.put(DATE_TIME, dateFrom.toString());
    params.put(DATE_FROM, dateFrom.toString());
    params.put(DATE_TO, dateTo.toString());
    params.put(
        PROCESSES,
        process.stream()
            .map(ProcessName::getName)
            .collect(Collectors.toSet())
            .toString()
    );
    if (processPathNames != null) {
      params.put(
          PROCESS_PATHS,
          processPathNames.stream()
              .map(ProcessPathName::getName)
              .collect(Collectors.toSet())
              .toString());
    }
    return params;
  }

  public record Throughput(
      int quantity
  ) {
  }
}
