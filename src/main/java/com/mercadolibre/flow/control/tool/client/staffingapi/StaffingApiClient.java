package com.mercadolibre.flow.control.tool.client.staffingapi;

import static com.mercadolibre.flow.control.tool.client.config.RestPool.STAFFING_API;
import static java.lang.String.format;
import static org.springframework.http.HttpStatus.OK;

import com.mercadolibre.fbm.wms.outbound.commons.rest.HttpClient;
import com.mercadolibre.fbm.wms.outbound.commons.rest.HttpRequest;
import com.mercadolibre.flow.control.tool.client.staffingapi.constant.StaffingWorkflow;
import com.mercadolibre.flow.control.tool.client.staffingapi.dto.MetricHistoryDto;
import com.mercadolibre.json.type.TypeReference;
import com.mercadolibre.restclient.MeliRestClient;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class StaffingApiClient extends HttpClient {

  private static final String GET_METRIC_HISTORY = "/logistic_centers/%s/metrics/history";

  private static final String WORKFLOW = "workflow";

  private static final String DATE_FROM = "date_from";

  private static final String DATE_TO = "date_to";

  protected StaffingApiClient(MeliRestClient client) {
    super(client, STAFFING_API.name());
  }

  public List<MetricHistoryDto> getMetricsHistory(final String logisticCenterId,
                                                  final StaffingWorkflow staffingWorkflow,
                                                  final Instant dateFrom,
                                                  final Instant dateTo) {
    final HttpRequest request = HttpRequest.builder()
        .url(format(GET_METRIC_HISTORY, logisticCenterId))
        .GET()
        .queryParams(createMetricHistoryParams(staffingWorkflow, dateFrom, dateTo))
        .acceptedHttpStatuses(Set.of(OK))
        .build();

    return send(request, response -> response.getData(new TypeReference<>() {
    }));
  }

  private Map<String, String> createMetricHistoryParams(final StaffingWorkflow staffingWorkflow,
                                                        final Instant dateFrom,
                                                        final Instant dateTo) {
    return Map.of(
        WORKFLOW, staffingWorkflow.getName(),
        DATE_FROM, dateFrom.toString(),
        DATE_TO, dateTo.toString()
    );
  }

}
