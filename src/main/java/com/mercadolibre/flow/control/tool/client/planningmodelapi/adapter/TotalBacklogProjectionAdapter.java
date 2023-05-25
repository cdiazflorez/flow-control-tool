package com.mercadolibre.flow.control.tool.client.planningmodelapi.adapter;

import com.mercadolibre.fbm.wms.outbound.commons.rest.exception.ClientException;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.PlanningModelApiClient;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.TotalBacklogProjectionRequest;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.TotalBacklogProjectionRequest.Backlog;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.TotalBacklogProjectionRequest.PlannedUnit;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.TotalBacklogProjectionRequest.ProcessPath;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.TotalBacklogProjectionRequest.Quantity;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.TotalBacklogProjectionRequest.Throughput;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.TotalBacklogProjectionResponse;
import com.mercadolibre.flow.control.tool.exception.TotalProjectionException;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.TotalBacklogProjectionGateway;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.domain.ProjectionTotal;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.domain.SlaQuantity;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class TotalBacklogProjectionAdapter implements TotalBacklogProjectionGateway {

  final PlanningModelApiClient planningModelApiClient;


  public List<ProjectionTotal> getTotalProjection(final String logisticCenterId,
                                                  final Instant dateFrom,
                                                  final Instant dateTo,
                                                  final Map<ProcessPathName, List<SlaQuantity>> backlog,
                                                  final Map<ProcessPathName, List<SlaQuantity>> plannedUnits,
                                                  final Map<Instant, Integer> throughput) {


    final TotalBacklogProjectionRequest request = new TotalBacklogProjectionRequest(
        dateFrom,
        dateTo,
        new Backlog(buildProcessPath(backlog, true)),
        new PlannedUnit(buildProcessPath(plannedUnits, false)),
        buildThroughputInput(throughput)
    );

    try {

      final List<TotalBacklogProjectionResponse> response = planningModelApiClient.getTotalBacklogProjection(logisticCenterId, request);

      return response.stream().map(projection ->
          new ProjectionTotal(
              projection.getDate(),
              projection.getSla().stream()
                  .map(sla ->
                      new ProjectionTotal.SlaProjected(
                          sla.getDateOut(),
                          sla.getQuantity(),
                          sla.getProcessPath() == null
                              ? List.of()
                              : sla.getProcessPath().stream()
                              .map(processPath -> new ProjectionTotal.Path(processPath.getName(), processPath.getQuantity()))
                              .toList()
                      )).toList()
          )).toList();

    } catch (ClientException ce) {
      throw new TotalProjectionException(logisticCenterId, ce, ce.getResponseStatus());
    }

  }

  private List<Throughput> buildThroughputInput(final Map<Instant, Integer> throughput) {
    return throughput.entrySet().stream()
        .map(entry -> new Throughput(entry.getKey(), entry.getValue()))
        .toList();
  }

  private List<ProcessPath> buildProcessPath(final Map<ProcessPathName, List<SlaQuantity>> slaByProcessName, final boolean hasBacklog) {
    return slaByProcessName.entrySet().stream().map(slaByProcessPath ->
        new ProcessPath(
            slaByProcessPath.getKey(),
            slaByProcessPath.getValue().stream()
                .map(slaQuantity -> {
                      final Instant dateIn = hasBacklog ? null : slaQuantity.getDateIn();
                      return new Quantity(
                          dateIn,
                          slaQuantity.getDateOut(),
                          slaQuantity.getQuantity()
                      );
                    }
                )
                .toList()
        )).toList();
  }
}
