package com.mercadolibre.flow.control.tool.client.staffingapi;

import static com.mercadolibre.flow.control.tool.client.staffingapi.constant.StaffingProcessName.PICKING;
import static com.mercadolibre.flow.control.tool.client.staffingapi.constant.StaffingWorkflow.FBM_WMS_OUTBOUND;
import static com.mercadolibre.flow.control.tool.util.TestUtils.DATE_FROM;
import static com.mercadolibre.flow.control.tool.util.TestUtils.DATE_TO;
import static com.mercadolibre.flow.control.tool.util.TestUtils.LOGISTIC_CENTER_ID;
import static java.time.temporal.ChronoUnit.HOURS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.mercadolibre.flow.control.tool.client.staffingapi.adapter.MetricsAdapter;
import com.mercadolibre.flow.control.tool.client.staffingapi.dto.MetricDto;
import com.mercadolibre.flow.control.tool.client.staffingapi.dto.MetricHistoryDto;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import com.mercadolibre.flow.control.tool.feature.staffing.domain.MetricData;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MetricAdapterTest {

  private static final long PRODUCTIVITY_VALUE = 100L;

  private static final long THROUGHPUT_VALUE = 1000L;

  @Mock
  private StaffingApiClient client;

  @InjectMocks
  private MetricsAdapter metricsAdapter;

  @Test
  @DisplayName("Gets a historical metrics from staffing client")
  void testGetMetricsOk() {
    //GIVEN
    final List<MetricData> expectedMetricsData = IntStream.rangeClosed(0, 6)
        .mapToObj(iterator -> new MetricData(
            ProcessName.PICKING,
            DATE_FROM.plus(iterator, HOURS),
            PRODUCTIVITY_VALUE,
            THROUGHPUT_VALUE)
        ).toList();

    final List<MetricDto> metricsDto = IntStream.rangeClosed(0, 6)
            .mapToObj(iterator -> new MetricDto(
                DATE_FROM.plus(iterator, HOURS),
                PRODUCTIVITY_VALUE,
                THROUGHPUT_VALUE)
            ).toList();


    when(client.getMetricsHistory(LOGISTIC_CENTER_ID, FBM_WMS_OUTBOUND, DATE_FROM, DATE_TO)).thenReturn(
        List.of(
            new MetricHistoryDto(FBM_WMS_OUTBOUND, PICKING, metricsDto)
        )
    );
    //THEN
    final List<MetricData> metricsData = metricsAdapter.getMetrics(LOGISTIC_CENTER_ID, Workflow.FBM_WMS_OUTBOUND, DATE_FROM, DATE_TO);
    //WHEN
    assertEquals(expectedMetricsData, metricsData);
  }

}
