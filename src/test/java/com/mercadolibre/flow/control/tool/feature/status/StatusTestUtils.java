package com.mercadolibre.flow.control.tool.feature.status;

import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.BATCH_SORTER;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.HU_ASSEMBLY;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.PACKING;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.PACKING_WALL;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.PICKING;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.SHIPPED;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.WALL_IN;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.WAVING;

import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StatusTestUtils {

  public static Map<ProcessName, Integer> mockBacklogTotalsByProcess() {
    return Arrays.stream(ProcessName.values())
        .collect(Collectors.toMap(Function.identity(), value -> 10)
        );
  }

  public static Set<ProcessName> mockAllProcessesSet() {
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

  public static Set<ProcessName> mockTwoProcessesSet() {
    return Set.of(
        WAVING,
        PICKING
    );
  }

}
