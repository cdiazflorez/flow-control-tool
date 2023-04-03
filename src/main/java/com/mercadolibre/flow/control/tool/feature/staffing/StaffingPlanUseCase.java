package com.mercadolibre.flow.control.tool.feature.staffing;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import com.mercadolibre.flow.control.tool.feature.staffing.constant.StaffingType;
import com.mercadolibre.flow.control.tool.feature.staffing.domain.StaffingOperation;
import com.mercadolibre.flow.control.tool.feature.staffing.domain.StaffingOperationValues;
import com.mercadolibre.flow.control.tool.feature.staffing.domain.StaffingPlannedData;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StaffingPlanUseCase {

  private final StaffingPlanGateway staffingPlanGateway;

  public StaffingOperation getStaffing(final String logisticCenter,
                                       final Workflow workflow,
                                       final Instant dateFrom,
                                       final Instant dateTo) {

    final Map<StaffingType, List<StaffingPlannedData>> staffingPlan = staffingPlanGateway.getStaffingPlanned(workflow,
                                                                                                             logisticCenter,
                                                                                                             dateFrom,
                                                                                                             dateTo);
    //TODO: El campo lastModifiedDate se obtendra de un nuevo servicio que consulta la última fecha del ajuste o
    // reforcast realizado
    return new StaffingOperation(
        null,
        staffingPlan.entrySet().stream()
            .collect(
                toMap(
                    Map.Entry::getKey,
                    v -> getStaffingOperationByProcessName(v.getKey(), v.getValue())
                )
            )
    );
  }

  private Map<ProcessName, StaffingOperationValues> getStaffingOperationByProcessName(
      final StaffingType staffingType,
      final List<StaffingPlannedData> staffingPlannedData) {

    return staffingPlannedData.stream()
        .collect(
            groupingBy(
                StaffingPlannedData::processName,
                collectingAndThen(toList(), list -> staffingType.getStaffingOperationStrategy().getStaffingOperation(list))
            )
        );
  }

  /**
   * Interface used to obtain the staffing plan.
   */
  public interface StaffingPlanGateway {

    /**
     * Implementation should obtain information from the staffing plan.
     *
     * @param workflow       workflow
     * @param logisticCenter logistic center id
     * @param dateFrom       date from
     * @param dateTo         date to
     * @return a Map of {@link StaffingPlannedData} List by {@link StaffingType}
     */
    Map<StaffingType, List<StaffingPlannedData>> getStaffingPlanned(Workflow workflow,
                                                                    String logisticCenter,
                                                                    Instant dateFrom,
                                                                    Instant dateTo);
  }
}
