package com.mercadolibre.flow.control.tool.client.backlog.adapter;

import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.PACKING;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.PICKING;
import static com.mercadolibre.flow.control.tool.feature.entity.Workflow.FBM_WMS_OUTBOUND;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.mercadolibre.flow.control.tool.client.backlog.BacklogApiClient;
import com.mercadolibre.flow.control.tool.client.backlog.dto.LastPhotoRequest;
import com.mercadolibre.flow.control.tool.client.backlog.dto.PhotoResponse;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BacklogProjectedAdapterTest {

  private static final String LOGISTIC_CENTER_ID = "ARBA01";
  private static final String KEY_STEP = "step";
  private static final String KEY_PATH = "path";
  private static final String KEY_DATE_OUT = "date_out";
  private static final String TO_PICK = "to_pick";
  private static final String TO_PACK = "to_pack";
  private static final String TOT_MONO = "tot_mono";
  private static final String NON_TOT_MONO = "non_tot_mono";
  private static final Instant TAKEN_ON = Instant.parse("2023-09-05T00:00:00Z");
  private static final String SLA_1 = "2023-09-05T10:00:00Z";
  private static final String SLA_2 = "2023-09-06T10:00:00Z";
  private static final PhotoResponse PHOTO_RESPONSE = new PhotoResponse(
      TAKEN_ON,
      List.of(
          new PhotoResponse.Group(
              Map.of(KEY_STEP, TO_PICK, KEY_PATH, TOT_MONO, KEY_DATE_OUT, SLA_1),
              100
          ),
          new PhotoResponse.Group(
              Map.of(KEY_STEP, TO_PICK, KEY_PATH, TOT_MONO, KEY_DATE_OUT, SLA_2),
              210
          ),
          new PhotoResponse.Group(
              Map.of(KEY_STEP, TO_PICK, KEY_PATH, NON_TOT_MONO, KEY_DATE_OUT, SLA_1),
              150
          ),
          new PhotoResponse.Group(
              Map.of(KEY_STEP, TO_PICK, KEY_PATH, NON_TOT_MONO, KEY_DATE_OUT, SLA_2),
              300
          ),
          new PhotoResponse.Group(
              Map.of(KEY_STEP, TO_PACK, KEY_PATH, TOT_MONO, KEY_DATE_OUT, SLA_1),
              50
          ),
          new PhotoResponse.Group(
              Map.of(KEY_STEP, TO_PACK, KEY_PATH, TOT_MONO, KEY_DATE_OUT, SLA_2),
              30
          ),
          new PhotoResponse.Group(
              Map.of(KEY_STEP, TO_PACK, KEY_PATH, NON_TOT_MONO, KEY_DATE_OUT, SLA_1),
              11
          ),
          new PhotoResponse.Group(
              Map.of(KEY_STEP, TO_PACK, KEY_PATH, NON_TOT_MONO, KEY_DATE_OUT, SLA_2),
              444
          )
      )
  );

  private static final Map<ProcessName, Map<ProcessPathName, Map<Instant, Integer>>> EXPECTED = Map.of(
      PICKING, Map.of(
          ProcessPathName.TOT_MONO,
          Map.of(Instant.parse(SLA_1), 100, Instant.parse(SLA_2), 210),
          ProcessPathName.NON_TOT_MONO,
          Map.of(Instant.parse(SLA_1), 150, Instant.parse(SLA_2), 300)
      ),
      PACKING, Map.of(
          ProcessPathName.TOT_MONO,
          Map.of(Instant.parse(SLA_1), 50, Instant.parse(SLA_2), 30),
          ProcessPathName.NON_TOT_MONO,
          Map.of(Instant.parse(SLA_1), 11, Instant.parse(SLA_2), 444)
      )
  );
  @InjectMocks
  private BacklogProjectedAdapter backlogProjectedAdapter;
  @Mock
  private BacklogApiClient backlogApiClient;

  private static Stream<Arguments> parameterTest() {
    return Stream.of(Arguments.of(PHOTO_RESPONSE, EXPECTED), Arguments.of(null, emptyMap()));
  }

  @ParameterizedTest
  @MethodSource("parameterTest")
  void backlogProjectedAdapterTest(
      final PhotoResponse photoResponse,
      final Map<ProcessName, Map<ProcessPathName, Map<Instant, Integer>>> expected) {

    when(backlogApiClient.getLastPhoto(any(LastPhotoRequest.class))
    ).thenReturn(photoResponse);

    final var response = backlogProjectedAdapter.getBacklogTotalsByProcessAndPPandSla(
        LOGISTIC_CENTER_ID, FBM_WMS_OUTBOUND, Set.of(PICKING, PACKING), TAKEN_ON);

    assertEquals(expected.size(), response.size());
    assertEquals(expected, response);
  }
}
