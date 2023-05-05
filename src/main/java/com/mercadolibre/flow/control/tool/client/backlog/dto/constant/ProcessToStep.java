package com.mercadolibre.flow.control.tool.client.backlog.dto.constant;

import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.DOCUMENTED;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.GROUPED;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.GROUPING;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.PACKED;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.PENDING;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.PICKED;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.SORTED;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.TO_DISPATCH;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.TO_DOCUMENT;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.TO_GROUP;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.TO_OUT;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.TO_PACK;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.TO_PICK;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.TO_ROUTE;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.TO_SORT;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public enum ProcessToStep {
  WAVING(List.of(PENDING, TO_ROUTE)),
  PICKING(List.of(TO_PICK, PhotoStep.PICKING)),
  BATCH_SORTER(List.of(TO_SORT, PICKED)),
  WALL_IN(List.of(SORTED, TO_GROUP, GROUPING, GROUPED)),
  PACKING(List.of(TO_PACK, PICKED)),
  PACKING_WALL(List.of(TO_PACK)),
  HU_ASSEMBLY(List.of(PhotoStep.PACKING, PACKED, TO_DOCUMENT, DOCUMENTED)),
  SHIPPING(List.of(TO_DISPATCH, TO_OUT));

  private static final Map<ProcessToStep, List<PhotoStep>>
      STEPS_BY_PROCESS = Arrays.stream(values())
      .collect(
          Collectors.toMap(Function.identity(), ProcessToStep::getBacklogPhotoSteps)
      );

  @Getter
  final List<PhotoStep> backlogPhotoSteps;

  ProcessToStep(List<PhotoStep> backlogPhotoSteps) {
    this.backlogPhotoSteps = backlogPhotoSteps;
  }

  public static ProcessToStep from(final String value) {
    return valueOf(value.toUpperCase(Locale.getDefault()));
  }

  @Override
  public String toString() {
    return name().toLowerCase(Locale.getDefault());
  }
}
