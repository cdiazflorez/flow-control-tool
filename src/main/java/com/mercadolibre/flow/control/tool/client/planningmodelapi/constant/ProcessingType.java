package com.mercadolibre.flow.control.tool.client.planningmodelapi.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Locale;

public enum ProcessingType {
    EFFECTIVE_WORKERS,
    EFFECTIVE_WORKERS_NS;

    @JsonValue
    public String getName() {
        return name().toLowerCase(Locale.getDefault());
    }
}