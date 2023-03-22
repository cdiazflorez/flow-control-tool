package com.mercadolibre.flow.control.tool.client.backlog.dto.constant;


import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoSteps.GROUPED;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoSteps.GROUPING;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoSteps.PACKED;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoSteps.PENDING;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoSteps.PICKED;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoSteps.SORTED;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoSteps.TO_DISPATCH;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoSteps.TO_GROUP;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoSteps.TO_OUT;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoSteps.TO_PACK;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoSteps.TO_PICK;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoSteps.TO_ROUTE;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoSteps.TO_SORT;

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
  PICKING(List.of(TO_PICK, PhotoSteps.PICKING)),
  BATCH_SORTER(List.of(TO_SORT, PICKED)),
  WALL_IN(List.of(SORTED, TO_GROUP, GROUPING, GROUPED)),
  PACKING(List.of(TO_PACK, PICKED)),
  PACKING_WALL(List.of(TO_PACK)),
  HU_ASSEMBLY(List.of(PhotoSteps.PACKING, PACKED)),
  SHIPPED(List.of(TO_DISPATCH, TO_OUT));

  private static final Map<ProcessToStep, List<PhotoSteps>>
      STEPS_BY_PROCESS = Arrays.stream(values())
      .collect(
          Collectors.toMap(Function.identity(), ProcessToStep::getBacklogPhotoSteps)
      );

  @Getter
  final List<PhotoSteps> backlogPhotoSteps;

  ProcessToStep(List<PhotoSteps> backlogPhotoSteps) {
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
