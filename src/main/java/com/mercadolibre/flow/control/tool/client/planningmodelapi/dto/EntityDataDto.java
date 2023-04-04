package com.mercadolibre.flow.control.tool.client.planningmodelapi.dto;

import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.OutboundProcessName;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.PlanningWorkflow;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.ProcessingType;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.Source;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntityDataDto {
  private PlanningWorkflow workflow;
  private Instant date;
  private String processPath;
  private OutboundProcessName processName;
  private ProcessingType type;
  private String metricUnit;
  private Source source;
  private long value;
}
