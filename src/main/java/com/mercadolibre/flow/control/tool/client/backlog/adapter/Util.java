package com.mercadolibre.flow.control.tool.client.backlog.adapter;

import com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep;
import com.mercadolibre.flow.control.tool.client.backlog.dto.constant.ProcessToStep;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public final class Util {
  private Util() {
  }

  public static Set<PhotoStep> toSteps(final Set<ProcessName> processNames) {
    return processNames.stream()
        .map(process -> ProcessToStep.from(process.getName()).getBacklogPhotoSteps())
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());
  }
}
