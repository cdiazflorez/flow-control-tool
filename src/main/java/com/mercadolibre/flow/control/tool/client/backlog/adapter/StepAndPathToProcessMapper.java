package com.mercadolibre.flow.control.tool.client.backlog.adapter;

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
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.BATCH_SORTER;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.HU_ASSEMBLY;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.PACKING;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.PACKING_WALL;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.PICKING;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.SHIPPED;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.WALL_IN;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.WAVING;

import com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep;
import com.mercadolibre.flow.control.tool.client.backlog.dto.constant.ProcessPath;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class StepAndPathToProcessMapper {

  private StepAndPathToProcessMapper() {
  }

  private static Map<PhotoStep, Map<ProcessPath, ProcessName>> stepAndPathToProcess() {
    return
        Stream.of(
            new AbstractMap.SimpleEntry<>(PENDING, ProcessPath.allPaths().stream()
                .collect(Collectors.toMap(a -> a, value -> WAVING))),
            new AbstractMap.SimpleEntry<>(TO_ROUTE, ProcessPath.allPaths().stream()
                .collect(Collectors.toMap(a -> a, value -> WAVING))),
            new AbstractMap.SimpleEntry<>(TO_PICK, ProcessPath.allPaths().stream()
                .collect(Collectors.toMap(a -> a, value -> PICKING))),
            new AbstractMap.SimpleEntry<>(PhotoStep.PICKING, ProcessPath.allPaths().stream(

            ).collect(Collectors.toMap(a -> a, value -> PICKING))),
            new AbstractMap.SimpleEntry<>(PICKED,
                Stream.concat(ProcessPath.multiBatchPaths().stream()
                            .collect(Collectors.toMap(a -> a, v -> BATCH_SORTER)).entrySet().stream(),
                        ProcessPath.pathsMinusMultiBatch().stream().collect(Collectors.toMap(a -> a, v -> PACKING)).entrySet().stream())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))),
            new AbstractMap.SimpleEntry<>(TO_SORT, ProcessPath.allPaths().stream()
                .collect(Collectors.toMap(a -> a, value -> BATCH_SORTER))),
            new AbstractMap.SimpleEntry<>(SORTED, ProcessPath.allPaths().stream()
                .collect(Collectors.toMap(a -> a, value -> WALL_IN))),
            new AbstractMap.SimpleEntry<>(TO_GROUP, ProcessPath.allPaths().stream()
                .collect(Collectors.toMap(a -> a, value -> WALL_IN))),
            new AbstractMap.SimpleEntry<>(GROUPED, ProcessPath.allPaths().stream()
                .collect(Collectors.toMap(a -> a, value -> WALL_IN))),
            new AbstractMap.SimpleEntry<>(GROUPING, ProcessPath.allPaths().stream()
                .collect(Collectors.toMap(a -> a, value -> WALL_IN))),
            new AbstractMap.SimpleEntry<>(TO_PACK,
                Stream.concat(ProcessPath.pathsMinusMultiBatch().stream().collect(
                    Collectors.toMap(a -> a, v -> PACKING)).entrySet().stream(),
                        ProcessPath.multiBatchPaths().stream().collect(Collectors.toMap(a -> a, v -> PACKING_WALL)).entrySet().stream())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))),
            new AbstractMap.SimpleEntry<>(PACKED, ProcessPath.allPaths().stream()
                .collect(Collectors.toMap(a -> a, value -> HU_ASSEMBLY))),
            new AbstractMap.SimpleEntry<>(PhotoStep.PACKING, ProcessPath.allPaths().stream()
                .collect(Collectors.toMap(a -> a, value -> HU_ASSEMBLY))),
            new AbstractMap.SimpleEntry<>(TO_DOCUMENT, ProcessPath.allPaths().stream()
                .collect(Collectors.toMap(a -> a, value -> HU_ASSEMBLY))),
            new AbstractMap.SimpleEntry<>(DOCUMENTED, ProcessPath.allPaths().stream()
                .collect(Collectors.toMap(a -> a, value -> HU_ASSEMBLY))),
            new AbstractMap.SimpleEntry<>(TO_DISPATCH, ProcessPath.allPaths().stream()
                .collect(Collectors.toMap(a -> a, value -> SHIPPED))),
            new AbstractMap.SimpleEntry<>(TO_OUT, ProcessPath.allPaths().stream()
                .collect(Collectors.toMap(a -> a, value -> SHIPPED)))
        ).collect(Collectors.toMap(
            AbstractMap.SimpleEntry::getKey,
            AbstractMap.SimpleEntry::getValue
        ));
  }

  static Optional<ProcessName> pathAndStepToProcessName(final ProcessPath path, final PhotoStep steps) {
    return Optional.ofNullable(stepAndPathToProcess().get(steps)).map(processPathToProcessName -> processPathToProcessName.get(path));
  }
}
