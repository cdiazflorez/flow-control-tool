package com.mercadolibre.flow.control.tool.feature.editor;

import static org.apache.commons.lang3.StringUtils.isBlank;

import com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName;
import java.beans.PropertyEditorSupport;
import java.util.Arrays;

public class ProcessPathEditor extends PropertyEditorSupport {

  public static final String MESSAGE_PATTERN = "Value %s is invalid, "
      + "instead it should be one of %s";

  @Override
  public void setAsText(final String text) {
    if (isBlank(text)) {
      throw new IllegalArgumentException("Value should not be blank");
    }

    final ProcessPathName processPathName = ProcessPathName.of(text)
        .orElseThrow(
            () -> new IllegalArgumentException(
                String.format(
                    MESSAGE_PATTERN, text, Arrays.toString(ProcessPathName.values())
                )
            )
        );

    setValue(processPathName);
  }
}
