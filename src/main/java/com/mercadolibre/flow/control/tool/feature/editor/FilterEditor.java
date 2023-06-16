package com.mercadolibre.flow.control.tool.feature.editor;

import static org.apache.commons.lang3.StringUtils.isBlank;

import com.mercadolibre.flow.control.tool.exception.FilterNotSupportedException;
import com.mercadolibre.flow.control.tool.feature.forecastdeviation.constant.Filter;
import java.beans.PropertyEditorSupport;

public class FilterEditor extends PropertyEditorSupport {

  @Override
  public void setAsText(final String text) {
    if (isBlank(text)) {
      throw new IllegalArgumentException("Value should not be blank");
    }

    final Filter filter = Filter.from(text).orElseThrow(() -> new FilterNotSupportedException(text));
    setValue(filter);
  }
}
