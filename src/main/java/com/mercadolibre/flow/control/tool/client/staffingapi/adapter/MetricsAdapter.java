package com.mercadolibre.flow.control.tool.client.staffingapi.adapter;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.mercadolibre.fbm.wms.outbound.commons.rest.exception.ClientException;
import com.mercadolibre.flow.control.tool.client.staffingapi.StaffingApiClient;
import com.mercadolibre.flow.control.tool.client.staffingapi.constant.StaffingWorkflow;
import com.mercadolibre.flow.control.tool.exception.RealMetricsNotFoundException;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import com.mercadolibre.flow.control.tool.feature.staffing.domain.MetricData;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MetricsAdapter {

  private final StaffingApiClient staffingApiClient;

  public List<MetricData> getMetrics(final String logisticCenterId,
                                     final Workflow workflow,
                                     final Instant dateFrom,
                                     final Instant dateTo) {

    try {

      return staffingApiClient.getMetricsHistory(logisticCenterId, StaffingWorkflow.from(workflow.name()), dateFrom, dateTo).stream()
          .flatMap(metricHistoryDto -> {
            final var process = metricHistoryDto.getProcess();
            return metricHistoryDto.getMetrics().stream().map(
                metrics -> new MetricData(
                    process.translateProcessName(),
                    metrics.getDate(),
                    metrics.getEffProductivity(),
                    metrics.getThroughput()
                )
            );
          }).toList();

    } catch (ClientException ce) {
      if (ce.getResponseStatus() == NOT_FOUND.value()) {
        throw new RealMetricsNotFoundException(logisticCenterId, workflow.getName(), ce);
      } else {
        throw ce;
      }
    }
  }
}
