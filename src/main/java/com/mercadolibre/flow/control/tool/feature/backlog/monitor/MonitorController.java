package com.mercadolibre.flow.control.tool.feature.backlog.monitor;

import static com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName.NON_TOT_MONO;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName.TOT_MONO;
import static com.mercadolibre.flow.control.tool.util.DateUtils.isDifferenceBetweenDateBiggestThan;
import static com.mercadolibre.flow.control.tool.util.DateUtils.validateDateRange;
import static java.time.temporal.ChronoUnit.HOURS;

import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.BacklogLimit;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.BacklogMonitor;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.ProcessLimit;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.ProcessPathMonitor;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.ProcessesMonitor;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.SlasMonitor;
import com.mercadolibre.flow.control.tool.feature.editor.ProcessNameEditor;
import com.mercadolibre.flow.control.tool.feature.editor.ProcessPathEditor;
import com.mercadolibre.flow.control.tool.feature.editor.WorkflowEditor;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import com.newrelic.api.agent.Trace;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController("MonitorController")
@AllArgsConstructor
@RequestMapping("/control_tool/logistic_center/{logisticCenterId}/backlog")
public class MonitorController {

  private static final int MAX_HOURS = 30;

  private GetHistoricalBacklogUseCase getHistoricalBacklogUseCase;

  private BacklogProjectedUseCase backlogProjectedUseCase;

  private static Instant processDateTo(final Instant dateFrom, final Instant dateTo) {
    if (isDifferenceBetweenDateBiggestThan(dateFrom, dateTo, MAX_HOURS)) {
      return dateFrom.plus(MAX_HOURS, HOURS);
    } else {
      return dateTo;
    }
  }

  @Trace
  @GetMapping("/historical")
  public ResponseEntity<List<BacklogMonitor>> getBacklogHistorical(
      @PathVariable final String logisticCenterId,
      @RequestParam final Workflow workflow,
      @RequestParam final Set<ProcessName> processes,
      @RequestParam(required = false) final Set<Instant> slas,
      @RequestParam(name = "process_paths", required = false) final Set<ProcessPathName> processPaths,
      @RequestParam(name = "date_from") final Instant dateFrom,
      @RequestParam(name = "date_to") final Instant dateTo,
      @RequestParam(name = "view_date") final Instant viewDate
  ) {
    validateDateRange(dateFrom, dateTo);

    final Instant dateToProcessed = processDateTo(dateFrom, dateTo);

    final List<BacklogMonitor> historicalBacklog = getHistoricalBacklogUseCase.backlogHistoricalMonitor(
        workflow, logisticCenterId, processes, dateFrom, dateToProcessed, viewDate);

    return ResponseEntity.ok(historicalBacklog);

  }

  @Trace
  @GetMapping("/projections")
  public ResponseEntity<List<BacklogMonitor>> getBacklogProjections(
      @PathVariable final String logisticCenterId,
      @RequestParam final Workflow workflow,
      @RequestParam final Set<ProcessName> processes,
      @RequestParam(required = false) final Set<Instant> slas,
      @RequestParam(name = "process_paths", required = false) final Set<ProcessPathName> processPaths,
      @RequestParam(name = "date_from") final Instant dateFrom,
      @RequestParam(name = "date_to") final Instant dateTo,
      @RequestParam(name = "view_date") final Instant viewDate
  ) {

    validateDateRange(dateFrom, dateTo);

    final var response = backlogProjectedUseCase.getBacklogProjected(dateFrom, dateTo, logisticCenterId, workflow, processes, viewDate);

    return ResponseEntity.ok(response);
  }

  @Trace
  @GetMapping("/average")
  public ResponseEntity<List<BacklogMonitor>> getBacklogAverage(
      @PathVariable final String logisticCenterId,
      @RequestParam final Workflow workflow,
      @RequestParam final Set<ProcessName> processes,
      @RequestParam(required = false) final Set<Instant> slas,
      @RequestParam(name = "process_paths", required = false) final Set<ProcessPathName> processPaths,
      @RequestParam(name = "date_from") final Instant dateFrom,
      @RequestParam(name = "date_to") final Instant dateTo,
      @RequestParam(name = "view_date") final Instant viewDate
  ) {

    final Instant date = Instant.parse("2023-04-03T08:00:00Z");

    final BacklogMonitor monitorAverageResponse =
        new BacklogMonitor(
            date,
            List.of(
                new ProcessesMonitor(
                    ProcessName.PACKING,
                    20,
                    List.of(
                        new SlasMonitor(
                            date,
                            0,
                            List.of(
                                new ProcessPathMonitor(TOT_MONO, 0),
                                new ProcessPathMonitor(NON_TOT_MONO, 0)
                            )
                        ),
                        new SlasMonitor(
                            date.plus(1, HOURS),
                            5,
                            List.of(
                                new ProcessPathMonitor(TOT_MONO, 2),
                                new ProcessPathMonitor(NON_TOT_MONO, 3)
                            )
                        )
                    )
                )
            )
        );

    return ResponseEntity.ok(List.of(monitorAverageResponse));
  }

  /**
   * Retrieves a list of backlog limits based on the provided logistic center ID,
   * workflow, and process filters, as well as date filters.
   *
   * @param logisticCenterId the ID of the logistic center to retrieve the backlog limits for
   * @param workflow         the workflow to filter by
   * @param processes        the set of process names to filter by
   * @param dateFrom         the start date to filter by (inclusive)
   * @param dateTo           the end date to filter by (inclusive)
   * @return a ResponseEntity containing the list of matching backlog limits
   */
  @Trace
  @GetMapping("/limits")
  public ResponseEntity<List<BacklogLimit>> getBacklogLimits(
      @PathVariable final String logisticCenterId,
      @RequestParam final Workflow workflow,
      @RequestParam final Set<ProcessName> processes,
      @RequestParam("date_from") final Instant dateFrom,
      @RequestParam("date_to") final Instant dateTo
  ) {


    final List<BacklogLimit> backlogLimits = List.of(
        new BacklogLimit(
            Instant.parse("2023-03-21T08:00:00Z"),
            List.of(
                new ProcessLimit(
                    ProcessName.PICKING,
                    40L,
                    100L
                ),
                new ProcessLimit(
                    ProcessName.PACKING,
                    50L,
                    100L
                )
            )
        )
    );
    return ResponseEntity.ok(backlogLimits);
  }

  @InitBinder
  public void initBinder(final PropertyEditorRegistry dataBinder) {
    dataBinder.registerCustomEditor(Workflow.class, new WorkflowEditor());
    dataBinder.registerCustomEditor(ProcessName.class, new ProcessNameEditor());
    dataBinder.registerCustomEditor(ProcessPathName.class, new ProcessPathEditor());
  }
}
