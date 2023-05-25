package com.mercadolibre.flow.control.tool.feature.backlog.monitor;

import com.mercadolibre.flow.control.tool.feature.backlog.monitor.domain.ProjectionTotal;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.domain.SlaQuantity;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName;
import java.time.Instant;
import java.util.List;
import java.util.Map;


/* TODO: Remover esta interfaz cuando se integre con el caso de uso para la proyección total*/

/**
 * This interface defines a gateway for retrieving total backlog projection.
 * The implementation of this interface should return a list of {@link ProjectionTotal}
 */
public interface TotalBacklogProjectionGateway {

  List<ProjectionTotal> getTotalProjection(
      String logisticCenterId,
      Instant dateFrom,
      Instant dateTo,
      Map<ProcessPathName, List<SlaQuantity>> backlog,
      Map<ProcessPathName, List<SlaQuantity>> plannedUnits,
      Map<Instant, Integer> throughput
  );
}
