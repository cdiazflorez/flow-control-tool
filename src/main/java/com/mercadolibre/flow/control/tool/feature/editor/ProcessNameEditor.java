package com.mercadolibre.flow.control.tool.feature.editor;

import static org.apache.commons.lang3.StringUtils.isBlank;

import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import java.beans.PropertyEditorSupport;

public class ProcessNameEditor extends PropertyEditorSupport {

  @Override
  public void setAsText(final String text) {
    if (isBlank(text)) {
      throw new IllegalArgumentException("Value should not be blank");
    }

    final ProcessName processes = ProcessName.from(text);
    setValue(processes);
  }
}
