package com.mercadolibre.flow.control.tool.client.staffingapi.dto;

import com.mercadolibre.flow.control.tool.client.staffingapi.constant.StaffingProcessName;
import com.mercadolibre.flow.control.tool.client.staffingapi.constant.StaffingWorkflow;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetricHistoryDto {

  private StaffingProcessName process;

  private List<MetricDto> metrics;

}
