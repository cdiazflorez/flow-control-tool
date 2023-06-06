package com.mercadolibre.flow.control.tool.client.planningmodelapi.dto;

import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.PlannedGrouper;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.PlanningWorkflow;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BacklogPlannedRequest {

  private static final String DELIMITER = ",";

  private String logisticCenter;
  private PlanningWorkflow planningWorkflow;
  private Set<ProcessPathName> processPathNames;
  private Instant dateInFrom;
  private Instant dateInTo;
  private Instant dateOutFrom;
  private Instant dateOutTo;
  private Set<PlannedGrouper> groupBy;

  public Map<String, String> getQueryParams() {
    final Map<String, String> queryParams = new ConcurrentHashMap<>();
    queryParams.put("logistic_center", logisticCenter);
    queryParams.put("workflow", planningWorkflow.getName());
    queryParams.put("date_in_from", dateInFrom.toString());
    queryParams.put("date_in_to", dateInTo.toString());
    queryParams.put("view_date", dateInTo.toString());
    queryParams.put("group_by", String.join(DELIMITER, groupBy.stream().map(PlannedGrouper::getName).toList()));

    if (dateOutFrom != null && dateOutTo != null) {
      queryParams.put("date_out_from", dateOutFrom.toString());
      queryParams.put("date_out_to", dateOutTo.toString());
    }

    if (!processPathNames.isEmpty()) {
      queryParams.put("process_paths", String.join(DELIMITER, processPathNames.stream().map(ProcessPathName::getName).toList()));
    }

    return queryParams;
  }

}
