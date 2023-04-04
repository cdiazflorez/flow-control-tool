package com.mercadolibre.flow.control.tool.client.staffingapi.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Locale;

public enum StaffingProcessName {
    PICKING,
    PACKING,
    PACKING_WALL,
    WALL_IN,
    BATCH_SORTER,
    HU_ASSEMBLY,
    SALES_DISPATCH;

    @JsonValue
    public String getName() {
        return name().toLowerCase(Locale.getDefault());
    }
}
