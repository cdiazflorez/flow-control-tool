package com.mercadolibre.flow.control.tool.client.backlog.adapter;

import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoGrouper.DATE_IN;
import static com.mercadolibre.flow.control.tool.feature.entity.Workflow.FBM_WMS_OUTBOUND;
import static com.mercadolibre.flow.control.tool.util.TestUtils.objectMapper;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static java.util.Collections.emptyMap;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mercadolibre.fbm.wms.outbound.commons.rest.HttpRequest;
import com.mercadolibre.fbm.wms.outbound.commons.rest.exception.ClientException;
import com.mercadolibre.flow.control.tool.client.backlog.BacklogApiClient;
import com.mercadolibre.flow.control.tool.client.backlog.dto.LastPhotoRequest;
import com.mercadolibre.flow.control.tool.client.backlog.dto.PhotoResponse;
import com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoGrouper;
import com.mercadolibre.flow.control.tool.exception.RealSalesException;
import com.mercadolibre.flow.control.tool.feature.forecastdeviation.constant.Filter;
import com.mercadolibre.restclient.Response;
import com.mercadolibre.restclient.http.Headers;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RealSalesAdapterTest {

  private static final String LOGISTIC_CENTER_ID = "ARBA01";

  private static final Instant DATE_FROM = Instant.parse("2023-09-05T10:00:00Z");
  private static final Instant DATE_TO = Instant.parse("2023-09-05T15:00:00Z");

  @InjectMocks
  private RealSalesAdapter realSalesAdapter;

  @Mock
  private BacklogApiClient backlogApiClient;

  @Captor
  private ArgumentCaptor<LastPhotoRequest> lastPhotoRequestArgumentCaptor;


  private static Stream<Arguments> photoGrouperArgumentsTest() {
    return Stream.of(
        Arguments.of(Filter.DATE_IN, DATE_FROM),
        Arguments.of(Filter.DATE_OUT, DATE_TO)
    );
  }

  @ParameterizedTest
  @MethodSource("photoGrouperArgumentsTest")
  @DisplayName("Test get real sales.")
  void realSalesTest(final Filter filter, final Instant date) {
    // WHEN
    final PhotoGrouper groupBy = PhotoGrouper.from(filter.getName());

    when(backlogApiClient.getLastPhoto(any(LastPhotoRequest.class))).thenReturn(mockLastPhoto(groupBy));

    // THEN
    final Map<Instant, Integer> realSales =
        realSalesAdapter.getRealSales(LOGISTIC_CENTER_ID, FBM_WMS_OUTBOUND, filter, DATE_FROM, DATE_TO, DATE_FROM, DATE_TO, DATE_TO);

    verify(backlogApiClient).getLastPhoto(lastPhotoRequestArgumentCaptor.capture());
    final LastPhotoRequest input = lastPhotoRequestArgumentCaptor.getValue();

    Assertions.assertEquals(3, realSales.entrySet().size());


    Assertions.assertEquals(Set.of(groupBy), input.getGroupBy());
    Assertions.assertTrue(realSales.containsKey(date));
    Assertions.assertTrue(realSales.containsKey(date.plus(2, ChronoUnit.HOURS)));
    Assertions.assertTrue(realSales.containsKey(date.plus(4, ChronoUnit.HOURS)));


  }

  @Test
  void realSalesWithoutLastPhoto() {
    // WHEN
    when(backlogApiClient.getLastPhoto(any(LastPhotoRequest.class))).thenReturn(null);

    // THEN
    final Map<Instant, Integer> realSales =
        realSalesAdapter.getRealSales(LOGISTIC_CENTER_ID, FBM_WMS_OUTBOUND, Filter.DATE_IN, DATE_FROM, DATE_TO, DATE_FROM, DATE_TO,
            DATE_TO);

    Assertions.assertEquals(emptyMap(), realSales);
  }

  @Test
  void realSalesErrorTest() throws JsonProcessingException {
    // GIVE
    final ClientException ce = new ClientException(
        "BACKLOG_API",
        HttpRequest.builder()
            .url("URL")
            .build(),
        new Response(404, new Headers(Map.of()), objectMapper().writeValueAsBytes("real_sales_exception"))
    );

    // WHEN
    when(backlogApiClient.getLastPhoto(any(LastPhotoRequest.class))).thenThrow(ce);

    // THEN
    Assertions.assertThrows(
        RealSalesException.class,
        () -> realSalesAdapter.getRealSales(LOGISTIC_CENTER_ID, FBM_WMS_OUTBOUND, Filter.DATE_IN, DATE_FROM, DATE_TO, DATE_FROM, DATE_TO,
            DATE_TO)
    );

  }

  private PhotoResponse mockLastPhoto(final PhotoGrouper groupBy) {
    return DATE_IN == groupBy
        ? new PhotoResponse(
        DATE_TO,
        List.of(
            new PhotoResponse.Group(Map.of(groupBy.getName(), ISO_INSTANT.format(DATE_FROM)), 10),
            new PhotoResponse.Group(Map.of(groupBy.getName(), ISO_INSTANT.format(DATE_FROM.plus(2, ChronoUnit.HOURS))), 10),
            new PhotoResponse.Group(Map.of(groupBy.getName(), ISO_INSTANT.format(DATE_FROM.plus(4, ChronoUnit.HOURS))), 10)
        )
    ) : new PhotoResponse(
        DATE_TO,
        List.of(
            new PhotoResponse.Group(Map.of(groupBy.getName(), ISO_INSTANT.format(DATE_TO)), 10),
            new PhotoResponse.Group(Map.of(groupBy.getName(), ISO_INSTANT.format(DATE_TO.plus(2, ChronoUnit.HOURS))), 10),
            new PhotoResponse.Group(Map.of(groupBy.getName(), ISO_INSTANT.format(DATE_TO.plus(4, ChronoUnit.HOURS))), 10)
        )
    );
  }

}
