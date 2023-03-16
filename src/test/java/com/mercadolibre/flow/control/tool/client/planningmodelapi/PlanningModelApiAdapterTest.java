package com.mercadolibre.flow.control.tool.client.planningmodelapi;

import static com.mercadolibre.flow.control.tool.util.TestUtils.LOGISTIC_CENTER_ID;
import static org.mockito.Mockito.when;

import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.Metadata;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PlanningModelApiAdapterTest {
  @InjectMocks
  private PlanningModelApiAdapter planningModelApiAdapter;
  @Mock
  private PlanningModelApiClient planningModelApiClient;

  @Test
  void testPlanningModelApiAdapter() {
    // GIVEN
    final String date = "2023-03-16T13:47:48.809940Z";
    final ZonedDateTime dateFrom = ZonedDateTime.parse(date);
    final ZonedDateTime dateTo = dateFrom.plusDays(1);
    when(planningModelApiClient
        .getForecastMetadata(Workflow.FBM_WMS_OUTBOUND, LOGISTIC_CENTER_ID, dateFrom, dateTo))
        .thenReturn(mockMetadata());

    // WHEN
    final Instant viewDate = Instant.parse(date);
    final Optional<Double> unitsPerOrderRatio =
        planningModelApiAdapter.getUnitsPerOrderRatio(Workflow.FBM_WMS_OUTBOUND, LOGISTIC_CENTER_ID, viewDate);

    // THEN
    final Double expectedUnitsPerOrderRatio = 3.96;

    unitsPerOrderRatio.ifPresent(ratio -> Assertions.assertEquals(expectedUnitsPerOrderRatio, ratio));
  }

  @Test
  void testPlanningModelApiAdapterError() {
    // GIVEN
    final String date = "2023-03-16T13:47:48.809940Z";
    final ZonedDateTime dateFrom = ZonedDateTime.parse(date);
    final ZonedDateTime dateTo = dateFrom.plusDays(1);
    when(planningModelApiClient
        .getForecastMetadata(Workflow.FBM_WMS_OUTBOUND, LOGISTIC_CENTER_ID, dateFrom, dateTo))
        .thenReturn(mockMetadataError());

    // WHEN
    final Instant viewDate = Instant.parse(date);
    final Optional<Double> unitsPerOrderRatio =
        planningModelApiAdapter.getUnitsPerOrderRatio(Workflow.FBM_WMS_OUTBOUND, LOGISTIC_CENTER_ID, viewDate);

    // THEN
    Assertions.assertEquals(Optional.empty(), unitsPerOrderRatio);
  }

  private List<Metadata> mockMetadata() {
    return List.of(
        new Metadata("week", "11-2023"),
        new Metadata("warehouse_id", LOGISTIC_CENTER_ID),
        new Metadata("version", "2.0"),
        new Metadata("units_per_order_ratio", "3.96"),
        new Metadata("outbound_wall_in_productivity", "100"),
        new Metadata("outbound_picking_productivity", "80"),
        new Metadata("outbound_packing_wall_productivity", "90"),
        new Metadata("outbound_packing_productivity", "89"),
        new Metadata("outbound_batch_sorter_productivity", "93"),
        new Metadata("multi_order_distribution", "26"),
        new Metadata("multi_batch_distribution", "32"),
        new Metadata("mono_order_distribution", "42")
    );
  }

  private List<Metadata> mockMetadataError() {
    return List.of(
        new Metadata("week", "11-2023"),
        new Metadata("warehouse_id", LOGISTIC_CENTER_ID),
        new Metadata("version", "2.0"),
        new Metadata("outbound_wall_in_productivity", "100"),
        new Metadata("outbound_picking_productivity", "80"),
        new Metadata("outbound_packing_wall_productivity", "90"),
        new Metadata("outbound_packing_productivity", "95"),
        new Metadata("outbound_batch_sorter_productivity", "87"),
        new Metadata("multi_order_distribution", "26"),
        new Metadata("multi_batch_distribution", "32"),
        new Metadata("mono_order_distribution", "42")
    );
  }
}
