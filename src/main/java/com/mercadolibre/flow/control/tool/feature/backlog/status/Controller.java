package com.mercadolibre.flow.control.tool.feature.backlog.status;

import com.mercadolibre.flow.control.tool.feature.editor.ProcessNameEditor;
import com.mercadolibre.flow.control.tool.feature.editor.ValueTypeEditor;
import com.mercadolibre.flow.control.tool.feature.editor.WorkflowEditor;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.ValueType;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import com.newrelic.api.agent.Trace;
import java.time.Instant;
import java.util.Set;
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
public class Controller {

  private BacklogStatusUseCase backlogStatusUseCase;

  @Trace
  @GetMapping("/status")
  public BacklogStatus getBacklogStatus(
      @PathVariable final String logisticCenterId,
      @RequestParam final Workflow workflow,
      @RequestParam final ValueType type,
      @RequestParam final Set<ProcessName> processes,
      @RequestParam(name = "view_date") final Instant viewDate
  ) {

    return backlogStatusUseCase.getBacklogStatus(
        logisticCenterId,
        workflow,
        type,
        processes,
        viewDate
    );
  }

  @InitBinder
  public void initBinder(final PropertyEditorRegistry dataBinder) {
    dataBinder.registerCustomEditor(Workflow.class, new WorkflowEditor());
    dataBinder.registerCustomEditor(ProcessName.class, new ProcessNameEditor());
    dataBinder.registerCustomEditor(ValueType.class, new ValueTypeEditor());
  }
}
