package com.mercadolibre.flow.control.tool.feature.status;

import static com.mercadolibre.flow.control.tool.feature.status.usecase.constant.Processes.BATCH_SORTER;
import static com.mercadolibre.flow.control.tool.feature.status.usecase.constant.Processes.HU_ASSEMBLY;
import static com.mercadolibre.flow.control.tool.feature.status.usecase.constant.Processes.PACKING;
import static com.mercadolibre.flow.control.tool.feature.status.usecase.constant.Processes.PACKING_WALL;
import static com.mercadolibre.flow.control.tool.feature.status.usecase.constant.Processes.PICKING;
import static com.mercadolibre.flow.control.tool.feature.status.usecase.constant.Processes.SHIPPED;
import static com.mercadolibre.flow.control.tool.feature.status.usecase.constant.Processes.WALL_IN;
import static com.mercadolibre.flow.control.tool.feature.status.usecase.constant.Processes.WAVING;

import com.mercadolibre.flow.control.tool.feature.status.usecase.constant.Processes;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StatusTestUtils {

  public static Map<Processes, Integer> mockBacklogTotalsByProcess() {
    return Arrays.stream(Processes.values())
        .collect(Collectors.toMap(Function.identity(), value -> 10)
        );
  }

  public static Set<Processes> mockAllProcessesSet() {
    return Set.of(
        WAVING,
        PICKING,
        BATCH_SORTER,
        WALL_IN,
        PACKING,
        PACKING_WALL,
        HU_ASSEMBLY,
        SHIPPED
    );
  }

  public static Set<Processes> mockTwoProcessesSet() {
    return Set.of(
        WAVING,
        PICKING
    );
  }

}
