package com.mercadolibre.flow.control.tool.feature.status.controller;

import com.mercadolibre.flow.control.tool.feature.status.usecase.BacklogStatusUseCase;
import com.mercadolibre.flow.control.tool.feature.status.usecase.constant.Processes;
import com.mercadolibre.flow.control.tool.feature.status.usecase.constant.ValueType;
import com.mercadolibre.flow.control.tool.feature.status.usecase.constant.Workflow;
import com.mercadolibre.flow.control.tool.feature.status.usecase.constant.editor.ProcessesEditor;
import com.mercadolibre.flow.control.tool.feature.status.usecase.constant.editor.ValueTypeEditor;
import com.mercadolibre.flow.control.tool.feature.status.usecase.constant.editor.WorkflowEditor;
import com.newrelic.api.agent.Trace;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.beans.PropertyEditorRegistry;
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

  private BacklogStatusUseCase backlogStatusUseCase;

  @Trace
  @GetMapping("/status")
  public Map<String, Integer> getBacklogStatus(
      @PathVariable final String logisticCenterId,
      @RequestParam final Workflow workflow,
      @RequestParam final ValueType type,
      @RequestParam final Set<Processes> processes,
      @RequestParam(name = "view_date") final Instant viewDate
  ) {

    final var backlogTotalsByProcess = backlogStatusUseCase.getBacklogTotalsByProcess(
        logisticCenterId,
        workflow,
        type,
        processes,
        viewDate
    );

    return backlogTotalsByProcess.entrySet().stream()
        .collect(Collectors.toMap(entry -> entry.getKey().getName(), Map.Entry::getValue));
  }

  @InitBinder
  public void initBinder(final PropertyEditorRegistry dataBinder) {
    dataBinder.registerCustomEditor(Workflow.class, new WorkflowEditor());
    dataBinder.registerCustomEditor(Processes.class, new ProcessesEditor());
    dataBinder.registerCustomEditor(ValueType.class, new ValueTypeEditor());
  }
}
