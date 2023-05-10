package com.mercadolibre.flow.control.tool.client.backlog.adapter;

import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoGrouper.PATH;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoGrouper.STEP;

import com.mercadolibre.flow.control.tool.client.backlog.dto.PhotoResponse;
import com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep;
import com.mercadolibre.flow.control.tool.client.backlog.dto.constant.ProcessToStep;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Util {
  private Util() {
  }

  public static Set<PhotoStep> toSteps(final Set<ProcessName> processNames) {
    return processNames.stream()
        .map(process -> ProcessToStep.from(process.getName()).getBacklogPhotoSteps())
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());
  }

  public static Stream<PhotoResponse.Group> filterExistingProcessPathAndSteps(final List<PhotoResponse.Group> groups) {
    return groups.stream()
        .filter(group -> ProcessPathName.of(group.key().get(PATH.getName())).isPresent()
            && PhotoStep.of(group.key().get(STEP.getName())).isPresent());
  }
}
