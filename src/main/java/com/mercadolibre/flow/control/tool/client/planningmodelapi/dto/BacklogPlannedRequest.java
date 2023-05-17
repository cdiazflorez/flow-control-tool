package com.mercadolibre.flow.control.tool.client.planningmodelapi.dto;

import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.PlannedGrouper;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.PlanningWorkflow;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public record BacklogPlannedRequest(

    String logisticCenter,

    PlanningWorkflow planningWorkflow,

    List<ProcessPathName> processPathNames,

    Instant dateInFrom,

    Instant dateInTo,

    Instant viewDate,

    List<PlannedGrouper> groupBy

    ) {

  private static final String DELIMITER = ",";

  public Map<String, String> getQueryParams() {
    return Map.of(
        "logistic_center", logisticCenter,
        "workflow", planningWorkflow.getName(),
        "process_paths", String.join(DELIMITER, processPathNames.stream().map(ProcessPathName::getName).toList()),
        "date_in_from", dateInFrom.toString(),
        "date_in_to", dateInTo.toString(),
        "view_date", viewDate.toString(),
        "group_by", String.join(DELIMITER, groupBy.stream().map(PlannedGrouper::getName).toList())
    );
  }
}
