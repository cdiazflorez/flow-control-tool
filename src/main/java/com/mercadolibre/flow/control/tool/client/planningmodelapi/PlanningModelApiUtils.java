package com.mercadolibre.flow.control.tool.client.planningmodelapi;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlanningModelApiUtils {
  protected static final String GET_FORECAST_METADATA = "/planning/model/workflows/%s/metadata";
  protected static final String WAREHOUSE_ID = "warehouse_id";
  protected static final String DATE_FROM = "date_from";
  protected static final String DATE_TO = "date_to";

  protected static Map<String, String> createForecastMetadataParams(final String warehouseId,
                                                                    final ZonedDateTime dateFrom,
                                                                    final ZonedDateTime dateTo) {
    final Map<String, String> params = new ConcurrentHashMap<>();
    params.put(WAREHOUSE_ID, warehouseId);
    params.put(DATE_FROM, dateFrom.format(ISO_OFFSET_DATE_TIME));
    params.put(DATE_TO, dateTo.format(ISO_OFFSET_DATE_TIME));
    return params;
  }
}
