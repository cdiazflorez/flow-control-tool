package com.mercadolibre.flow.control.tool.feature.backlog.monitor;

import com.mercadolibre.flow.control.tool.client.backlog.dto.constant.ProcessPath;
import com.mercadolibre.flow.control.tool.feature.editor.ProcessNameEditor;
import com.mercadolibre.flow.control.tool.feature.editor.ProcessPathEditor;
import com.mercadolibre.flow.control.tool.feature.editor.WorkflowEditor;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import com.newrelic.api.agent.Trace;
import java.time.Instant;
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
  @Trace
  @GetMapping("/historical")
  public ResponseEntity<String> getBacklogHistorical(
      @PathVariable final String logisticCenterId,
      @RequestParam final Workflow workflow,
      @RequestParam final Set<ProcessName> processes,
      @RequestParam final Set<Instant> slas,
      @RequestParam(name = "process_paths") final Set<ProcessPath> processPaths,
      @RequestParam(name = "date_from") final Instant dateFrom,
      @RequestParam(name = "date_to") final Instant dateTo,
      @RequestParam(name = "view_date") final Instant viewDate
  ) {

    return ResponseEntity.ok("Historical");
  }

  @Trace
  @GetMapping("/projections")
  public ResponseEntity<String> getBacklogProjections(
      @PathVariable final String logisticCenterId,
      @RequestParam final Workflow workflow,
      @RequestParam final Set<ProcessName> processes,
      @RequestParam final Set<Instant> slas,
      @RequestParam(name = "process_paths") final Set<ProcessPath> processPaths,
      @RequestParam(name = "date_from") final Instant dateFrom,
      @RequestParam(name = "date_to") final Instant dateTo,
      @RequestParam(name = "view_date") final Instant viewDate
  ) {

    return ResponseEntity.ok("Projections");
  }

  @Trace
  @GetMapping("/average")
  public ResponseEntity<String> getBacklogAverage(
      @PathVariable final String logisticCenterId,
      @RequestParam final Workflow workflow,
      @RequestParam final Set<ProcessName> processes,
      @RequestParam final Set<Instant> slas,
      @RequestParam(name = "process_paths") final Set<ProcessPath> processPaths,
      @RequestParam(name = "date_from") final Instant dateFrom,
      @RequestParam(name = "date_to") final Instant dateTo,
      @RequestParam(name = "view_date") final Instant viewDate
  ) {

    return ResponseEntity.ok("Average");
  }

  @InitBinder
  public void initBinder(final PropertyEditorRegistry dataBinder) {
    dataBinder.registerCustomEditor(Workflow.class, new WorkflowEditor());
    dataBinder.registerCustomEditor(ProcessName.class, new ProcessNameEditor());
    dataBinder.registerCustomEditor(ProcessPath.class, new ProcessPathEditor());
  }
}
