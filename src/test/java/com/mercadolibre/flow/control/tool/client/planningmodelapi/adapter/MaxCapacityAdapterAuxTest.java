package com.mercadolibre.flow.control.tool.client.planningmodelapi.adapter;

import static java.util.Collections.emptyMap;

import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MaxCapacityAdapterAuxTest {

  @InjectMocks
  private MaxCapacityAdapterAux maxCapacityAdapterAux;

  @Test
  void testBacklogLimitAdapter() {
    final Map<Instant, Long> maxCapacityForHour = maxCapacityAdapterAux.getMaxCapacityForHour(
        "ARTW01",
        Instant.parse("2023-06-15T08:00:00Z"),
        Instant.parse("2023-06-15T14:00:00Z")
    );

    Assertions.assertEquals(maxCapacityForHour, emptyMap());
  }
}
