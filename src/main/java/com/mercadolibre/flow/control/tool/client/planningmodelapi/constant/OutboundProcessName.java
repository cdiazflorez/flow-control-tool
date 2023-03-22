package com.mercadolibre.flow.control.tool.client.planningmodelapi.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Locale;

public enum OutboundProcessName {
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
