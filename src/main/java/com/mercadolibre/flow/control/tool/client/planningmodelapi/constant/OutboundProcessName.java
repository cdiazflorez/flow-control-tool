package com.mercadolibre.flow.control.tool.client.planningmodelapi.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import java.util.Arrays;
import java.util.Locale;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum OutboundProcessName {
  WAVING(ProcessName.WAVING),
  PICKING(ProcessName.PICKING),
  PACKING(ProcessName.PACKING),
  PACKING_WALL(ProcessName.PACKING_WALL),
  WALL_IN(ProcessName.WALL_IN),
  BATCH_SORTER(ProcessName.BATCH_SORTER),
  HU_ASSEMBLY(ProcessName.HU_ASSEMBLY),
  SALES_DISPATCH(ProcessName.SHIPPING);

  private final ProcessName processName;

  public ProcessName translateProcessName() {
    return this.processName;
  }

  @JsonValue
  public String getName() {
    return name().toLowerCase(Locale.getDefault());
  }

  public static OutboundProcessName fromProcessName(ProcessName processName) {
    return Arrays.stream(OutboundProcessName.values())
        .filter(outboundProcessName -> outboundProcessName.processName == processName)
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException(
            "No matching value was found in enum OutboundProcessName for the value of enum ProcessName: " + processName
        ));
  }
}
