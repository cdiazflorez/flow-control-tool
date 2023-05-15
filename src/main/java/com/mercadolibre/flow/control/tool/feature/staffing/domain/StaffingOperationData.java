package com.mercadolibre.flow.control.tool.feature.staffing.domain;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class StaffingOperationData {
  private Instant date;
  private Long planned;
  private Long plannedSystemic;
  private Boolean plannedEdited;
  private Boolean plannedSystemicEdited;
  private Long plannedNonSystemic;
  private Boolean plannedNonSystemicEdited;
  private Long presentSystemic;
  private Long presentNonSystemic;
  private Long deviationSystemic;
  private Long deviationNonSystemic;
  private Long real;
  private Long deviation;

}
