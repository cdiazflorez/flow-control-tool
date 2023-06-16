package com.mercadolibre.flow.control.tool.feature.backlog.monitor;

import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.OutboundProcessName;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.ProcessingType;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.BacklogLimit;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.ProcessLimit;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BacklogLimitsUseCase {

  private final GetBacklogLimitGateway backlogLimitGateway;

  /**
   * Executes and sorts the retrieval of backlog limits for a logistic center, workflow, processes, and date range.
   *
   * @param logisticCenterId the ID of the logistic center
   * @param workflow         the workflow
   * @param processes        the set of process names
   * @param dateFrom         the starting date of the range
   * @param dateTo           the ending date of the range
   * @return a list of backlog limits, sorted by date in ascending order
   */
  public List<BacklogLimit> execute(
      final String logisticCenterId,
      final Workflow workflow,
      final Set<ProcessName> processes,
      final Instant dateFrom,
      final Instant dateTo
  ) {

    final List<BacklogLimit> backlogLimits = mapToBacklogLimitsEntity(
        backlogLimitGateway.getBacklogLimitsEntityDataMap(
            logisticCenterId,
            workflow,
            processes,
            dateFrom,
            dateTo
        )
    );
    return filterAndSortBacklogLimits(backlogLimits);
  }

  /**
   * Maps the grouped entity data by date, process, and type to a list of backlog limits.
   *
   * @param groupedData the grouped entity data by date, process, and type
   * @return a list of backlog limits based on the grouped entity data
   */
  private List<BacklogLimit> mapToBacklogLimitsEntity(
      Map<Instant, Map<OutboundProcessName, Map<ProcessingType, Long>>> groupedData
  ) {
    return groupedData.entrySet().stream()
        .map(entity -> new BacklogLimit(
            entity.getKey(),
            entity.getValue().entrySet().stream()
                .map(process -> new ProcessLimit(
                    process.getKey().translateProcessName(),
                    process.getValue().get(ProcessingType.BACKLOG_LOWER_LIMIT),
                    process.getValue().get(ProcessingType.BACKLOG_UPPER_LIMIT)
                ))
                .toList()
        ))
        .toList();
  }

  /**
   * Filters the provided list of BacklogLimits by removing processes with lower or upper values equal to -1.
   * Returns a new list of BacklogLimits with the filtered processes, sorted by date.
   *
   * @param backlogLimits The list of BacklogLimits to be filtered.
   * @return A new list of BacklogLimits with the filtered processes.
   */
  private List<BacklogLimit> filterAndSortBacklogLimits(final List<BacklogLimit> backlogLimits) {
    return backlogLimits.stream()
        .map(this::filterBacklogLimit)
        .sorted(Comparator.comparing(BacklogLimit::date))
        .toList();
  }

  /**
   * Filters and updates the provided BacklogLimit by removing processes with both lower and upper values equal to -1,
   * and updating the lower value to null when lower is -1 and upper is not -1.
   * Returns the filtered and updated BacklogLimit.
   *
   * @param backlogLimit The BacklogLimit to be filtered and updated.
   * @return The filtered and updated BacklogLimit.
   */
  private BacklogLimit filterBacklogLimit(BacklogLimit backlogLimit) {
    List<ProcessLimit> filteredProcesses = backlogLimit.processes().stream()
        .filter(processLimit -> !(processLimit.lower() == -1 && processLimit.upper() == -1))
        .map(this::updateProcessLimit)
        .sorted(Comparator.comparing(ProcessLimit::name))
        .toList();

    return new BacklogLimit(backlogLimit.date(), filteredProcesses);
  }

  /**
   * Updates the provided ProcessLimit by setting the lower value to null when lower is -1 and upper is not -1.
   * Returns the updated ProcessLimit.
   *
   * @param processLimit The ProcessLimit to be updated.
   * @return The updated ProcessLimit.
   */
  private ProcessLimit updateProcessLimit(ProcessLimit processLimit) {
    if (processLimit.lower() == -1) {
      return new ProcessLimit(processLimit.name(), null, processLimit.upper());
    }
    return processLimit;
  }

  /**
   * This interface defines a gateway for retrieving backlog limits for a logistic center, workflow, and list of processes
   * between the specified base dates.
   * The implementation of this interface should return a list of {@link BacklogLimit} objects representing the limits for each process
   * based on the provided parameters.
   */
  public interface GetBacklogLimitGateway {

    /**
     * The implementation should return the backlog limits given by upper and lower.
     *
     * @param logisticCenterId logistic center id.
     * @param workflow         outbound
     * @param processes        list of processes to be requested.
     * @param dateFrom         base date to backlog.
     * @param dateTo           base date to backlog.
     * @return a map containing the grouped entity data by date, process, and type.
     */
    Map<Instant, Map<OutboundProcessName, Map<ProcessingType, Long>>> getBacklogLimitsEntityDataMap(
        String logisticCenterId,
        Workflow workflow,
        Set<ProcessName> processes,
        Instant dateFrom,
        Instant dateTo
    );
  }
}
