package com.mercadolibre.flow.control.tool.feature.backlog.monitor;

import static java.time.temporal.ChronoUnit.HOURS;

import com.mercadolibre.flow.control.tool.client.backlog.dto.constant.ProcessPath;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.BacklogMonitor;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.ProcessPathMonitor;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.ProcessesMonitor;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.SlasMonitor;
import com.mercadolibre.flow.control.tool.feature.editor.ProcessNameEditor;
import com.mercadolibre.flow.control.tool.feature.editor.ProcessPathEditor;
import com.mercadolibre.flow.control.tool.feature.editor.WorkflowEditor;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
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
public class Controller {

  private static final String TOT_MONO = "tot_mono";
  private static final String NON_TOT_MONO = "non_tot_mono";

  @Trace
  @GetMapping("/historical")
  public ResponseEntity<List<BacklogMonitor>> getBacklogHistorical(
      @PathVariable final String logisticCenterId,
      @RequestParam final Workflow workflow,
      @RequestParam final Set<ProcessName> processes,
      @RequestParam(required = false) final Set<Instant> slas,
      @RequestParam(name = "process_paths", required = false) final Set<ProcessPath> processPaths,
      @RequestParam(name = "date_from") final Instant dateFrom,
      @RequestParam(name = "date_to") final Instant dateTo,
      @RequestParam(name = "view_date") final Instant viewDate
  ) {

    final Instant date = Instant.parse("2023-04-01T08:00:00Z");

    final BacklogMonitor monitorHistoricalResponse =
        new BacklogMonitor(
            date,
            List.of(
                new ProcessesMonitor(
                    "picking",
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

    return ResponseEntity.ok(List.of(monitorHistoricalResponse));
  }

  @Trace
  @GetMapping("/projections")
  public ResponseEntity<List<BacklogMonitor>> getBacklogProjections(
      @PathVariable final String logisticCenterId,
      @RequestParam final Workflow workflow,
      @RequestParam final Set<ProcessName> processes,
      @RequestParam(required = false) final Set<Instant> slas,
      @RequestParam(name = "process_paths", required = false) final Set<ProcessPath> processPaths,
      @RequestParam(name = "date_from") final Instant dateFrom,
      @RequestParam(name = "date_to") final Instant dateTo,
      @RequestParam(name = "view_date") final Instant viewDate
  ) {

    final Instant date = Instant.parse("2023-04-02T08:00:00Z");

    final BacklogMonitor monitorProjectionsResponse =
        new BacklogMonitor(
            date,
            List.of(
                new ProcessesMonitor(
                    "wall_in",
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

    return ResponseEntity.ok(List.of(monitorProjectionsResponse));
  }

  @Trace
  @GetMapping("/average")
  public ResponseEntity<List<BacklogMonitor>> getBacklogAverage(
      @PathVariable final String logisticCenterId,
      @RequestParam final Workflow workflow,
      @RequestParam final Set<ProcessName> processes,
      @RequestParam(required = false) final Set<Instant> slas,
      @RequestParam(name = "process_paths", required = false) final Set<ProcessPath> processPaths,
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
                    "packing",
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

  @InitBinder
  public void initBinder(final PropertyEditorRegistry dataBinder) {
    dataBinder.registerCustomEditor(Workflow.class, new WorkflowEditor());
    dataBinder.registerCustomEditor(ProcessName.class, new ProcessNameEditor());
    dataBinder.registerCustomEditor(ProcessPath.class, new ProcessPathEditor());
  }
}
