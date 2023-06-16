package com.mercadolibre.flow.control.tool.feature.backlog;

import static com.mercadolibre.flow.control.tool.util.DateUtils.validateDateRange;

import com.mercadolibre.flow.control.tool.exception.InvalidateDateDurationRangeException;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.TotalBacklogMonitor;
import com.mercadolibre.flow.control.tool.feature.editor.ProcessNameEditor;
import com.mercadolibre.flow.control.tool.feature.editor.ProcessPathEditor;
import com.mercadolibre.flow.control.tool.feature.editor.ValueTypeEditor;
import com.mercadolibre.flow.control.tool.feature.editor.WorkflowEditor;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName;
import com.mercadolibre.flow.control.tool.feature.entity.ValueType;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import com.newrelic.api.agent.Trace;
import java.time.Duration;
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

@RestController
@AllArgsConstructor
@RequestMapping("/control_tool/logistic_center/{logisticCenterId}/backlog")
public class BacklogController {
  private BacklogProjectedTotalUseCase backlogProjectedTotalUseCase;

  @Trace
  @GetMapping("/projections/total")
  public ResponseEntity<List<TotalBacklogMonitor>> getTotalBacklogProjections(
      @PathVariable final String logisticCenterId,
      @RequestParam final Workflow workflow,
      @RequestParam(name = "backlog_processes") final Set<ProcessName> processes,
      @RequestParam(name = "throughput_processes") final Set<ProcessName> throughputProcesses,
      @RequestParam(name = "value_type") final ValueType valueType,
      @RequestParam(name = "date_from") final Instant dateFrom,
      @RequestParam(name = "date_to") final Instant dateTo,
      @RequestParam(name = "view_date") final Instant viewDate
  ) {

    validateDateRange(dateFrom, dateTo);
    validateIntervalDate(dateFrom, dateTo);

    final List<TotalBacklogMonitor> totalBacklogResponse = backlogProjectedTotalUseCase.getTotalProjection(logisticCenterId,
        workflow,
        processes,
        throughputProcesses,
        valueType,
        dateFrom,
        dateTo,
        viewDate);

    return ResponseEntity.ok(totalBacklogResponse);
  }

  @InitBinder
  public void initBinder(final PropertyEditorRegistry dataBinder) {
    dataBinder.registerCustomEditor(Workflow.class, new WorkflowEditor());
    dataBinder.registerCustomEditor(ProcessName.class, new ProcessNameEditor());
    dataBinder.registerCustomEditor(ProcessPathName.class, new ProcessPathEditor());
    dataBinder.registerCustomEditor(ValueType.class, new ValueTypeEditor());
  }

  private void validateIntervalDate(final Instant dateFrom, Instant dateTo) {
    final Duration duration = Duration.between(dateFrom, dateTo);
    final int oneHour = 1;

    if (duration.toHours() < oneHour) {
      throw new InvalidateDateDurationRangeException(dateFrom, dateTo, duration.toHours());
    }
  }

}
