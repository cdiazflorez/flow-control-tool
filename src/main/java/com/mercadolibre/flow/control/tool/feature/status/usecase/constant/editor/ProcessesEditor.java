package com.mercadolibre.flow.control.tool.feature.status.usecase.constant.editor;

import static org.apache.commons.lang3.StringUtils.isBlank;

import com.mercadolibre.flow.control.tool.feature.status.usecase.constant.Processes;
import java.beans.PropertyEditorSupport;

public class ProcessesEditor extends PropertyEditorSupport {

  @Override
  public void setAsText(final String text) {
    if (isBlank(text)) {
      throw new IllegalArgumentException("Value should not be blank");
    }

    final Processes processes = Processes.from(text);
    setValue(processes);
  }
}
