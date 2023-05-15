package com.mercadolibre.flow.control.tool.feature.staffing;

import static java.time.temporal.ChronoUnit.HOURS;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import com.mercadolibre.flow.control.tool.feature.staffing.constant.StaffingMetricType;
import com.mercadolibre.flow.control.tool.feature.staffing.domain.MetricData;
import com.mercadolibre.flow.control.tool.feature.staffing.domain.StaffingOperation;
import com.mercadolibre.flow.control.tool.feature.staffing.domain.StaffingOperationValues;
import com.mercadolibre.flow.control.tool.feature.staffing.domain.StaffingPlannedData;
import com.mercadolibre.flow.control.tool.feature.staffing.operation.StaffingOperationStrategy;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StaffingPlanUseCase {

  private final StaffingMetricGateway staffingMetricGateway;

  private final StaffingPlanGateway staffingPlanGateway;

  public StaffingOperation getStaffing(final String logisticCenter,
                                       final Workflow workflow,
                                       final Instant dateFrom,
                                       final Instant dateTo,
                                       final Instant viewDate) {

    final Instant viewDateTruncatedByHour = viewDate.truncatedTo(HOURS);
    final Instant dateFromTruncatedByHour = dateFrom.truncatedTo(HOURS);
    final Instant dateToTruncatedByHour = dateTo.truncatedTo(HOURS);
    final List<MetricData> metricsData = new ArrayList<>(Collections.emptyList());

    final Map<StaffingMetricType, List<StaffingPlannedData>> staffingPlan = staffingPlanGateway.getCurrentStaffing(workflow,
                                                                                                                   logisticCenter,
                                                                                                                   dateFromTruncatedByHour,
                                                                                                                   dateToTruncatedByHour);

    if (!dateFrom.isAfter(viewDateTruncatedByHour)) {
      metricsData.addAll(
          staffingMetricGateway.getMetrics(logisticCenter,
                                           workflow,
                                           dateFromTruncatedByHour,
                                           dateToTruncatedByHour.isAfter(viewDateTruncatedByHour)
                                       ? viewDateTruncatedByHour : dateToTruncatedByHour
          )
      );
    }


    //TODO: El campo lastModifiedDate se obtendra de un nuevo servicio que consulta la última fecha del ajuste o
    // reforcast realizado
    return new StaffingOperation(
        null,
        staffingPlan.entrySet().stream()
            .collect(
                toMap(
                    Map.Entry::getKey,
                    v -> getStaffingOperationByProcessName(v.getKey(), v.getValue(), metricsData)
                )
            )
    );
  }

  private Map<ProcessName, StaffingOperationValues> getStaffingOperationByProcessName(
      final StaffingMetricType staffingMetricType,
      final List<StaffingPlannedData> staffingPlannedData,
      final List<MetricData> metricsData) {

    final var strategy = staffingMetricType.getStaffingOperationStrategy();
    final var groupedMetrics = metricsData.stream().collect(groupingBy(MetricData::processName));
    final var groupedPlannedData = staffingPlannedData.stream().collect(groupingBy(StaffingPlannedData::processName));

    return buildMapOfStaffingOperationValuesByProcessName(groupedMetrics, groupedPlannedData, strategy);

  }

  private Map<ProcessName, StaffingOperationValues> buildMapOfStaffingOperationValuesByProcessName(
      final Map<ProcessName, List<MetricData>> metricsDataByProcessName,
      final Map<ProcessName, List<StaffingPlannedData>> staffingPlannedByProcessName,
      final StaffingOperationStrategy staffingOperationStrategy) {
    return staffingPlannedByProcessName.keySet()
        .stream()
        .collect(
            toMap(
                Function.identity(),
                process -> staffingOperationStrategy.getStaffingOperation(
                    staffingPlannedByProcessName.get(process),
                    metricsDataByProcessName.getOrDefault(process, List.of())
                )
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
     * @return a Map of {@link StaffingPlannedData} List by {@link StaffingMetricType}
     */
    Map<StaffingMetricType, List<StaffingPlannedData>> getCurrentStaffing(Workflow workflow,
                                                                          String logisticCenter,
                                                                          Instant dateFrom,
                                                                          Instant dateTo);
  }

  /**
   * Interface used to get staffing metrics.
   */
  public interface StaffingMetricGateway {

    /**
     * Implementation used to obtain the metric information.
     *
     * @param logisticCenterId logistic center Id
     * @param workflow         workflow
     * @param dateFrom         date from
     * @param dateTo           date to
     * @return a List of {@link MetricData}
     */
    List<MetricData> getMetrics(String logisticCenterId,
                                Workflow workflow,
                                Instant dateFrom,
                                Instant dateTo);

  }
}
