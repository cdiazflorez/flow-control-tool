package com.mercadolibre.flow.control.tool.client.backlog.adapter;

import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoGrouper.PATH;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoGrouper.STEP;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.PENDING;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.TO_PACK;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.TO_ROUTE;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.TO_SORT;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoWorkflow.FBM_WMS_OUTBOUND;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.BATCH_SORTER;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.PACKING_WALL;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.WAVING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.mercadolibre.flow.control.tool.client.backlog.BacklogApiClient;
import com.mercadolibre.flow.control.tool.client.backlog.dto.PhotoRequest;
import com.mercadolibre.flow.control.tool.client.backlog.dto.PhotoResponse;
import com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoGrouper;
import com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BacklogHistoricalAdapterTest {
  private static final Instant PHOTO_DATE = Instant.parse("2023-03-16T10:00:00Z");
  private static final String TOT_MULTI_BATCH = "TOT_MULTI_BATCH";
  private static final String NON_TOT_MULTI_BATCH = "NON_TOT_MULTI_BATCH";
  private static final String LOGISTIC_CENTER_ID = "ARTW01";
  private static final String PICKED = "picked";
  private static final String DATE_OUT = "date_out";
  private static final String DATE_OUT_1 = "2023-03-16T14:00:00Z";
  private static final String DATE_OUT_2 = "2023-03-16T18:00:00Z";
  private static final Instant DATE_TO = Instant.parse("2023-03-16T19:00:00Z");

  @InjectMocks
  BacklogHistoricalAdapter backlogHistoricalAdapter;

  @Mock
  BacklogApiClient backlogApiClient;

  @Test
  void backlogByDateProcessDateOutAndProcessPathNormalCase() {
    final PhotoResponse mockBacklogsAPI = new PhotoResponse(
        PHOTO_DATE,
        List.of(
            new PhotoResponse.Group(Map.of(PATH.getName(), TOT_MULTI_BATCH, STEP.getName(), PICKED, DATE_OUT, DATE_OUT_1), 500),
            new PhotoResponse.Group(Map.of(PATH.getName(), TOT_MULTI_BATCH, STEP.getName(), "to_pack", DATE_OUT, DATE_OUT_1), 1000),
            new PhotoResponse.Group(Map.of(PATH.getName(), TOT_MULTI_BATCH, STEP.getName(), "pending", DATE_OUT, DATE_OUT_1), 700),
            new PhotoResponse.Group(Map.of(PATH.getName(), TOT_MULTI_BATCH, STEP.getName(), PICKED, DATE_OUT, DATE_OUT_2), 500),
            new PhotoResponse.Group(Map.of(PATH.getName(), TOT_MULTI_BATCH, STEP.getName(), "to_pack", DATE_OUT, DATE_OUT_2), 1000),
            new PhotoResponse.Group(Map.of(PATH.getName(), TOT_MULTI_BATCH, STEP.getName(), "pending", DATE_OUT, DATE_OUT_2), 700),
            new PhotoResponse.Group(Map.of(PATH.getName(), NON_TOT_MULTI_BATCH, STEP.getName(), PICKED, DATE_OUT, DATE_OUT_1), 500),
            new PhotoResponse.Group(Map.of(PATH.getName(), NON_TOT_MULTI_BATCH, STEP.getName(), PICKED, DATE_OUT, DATE_OUT_1), 500)

        ));

    final PhotoRequest request = new PhotoRequest(
        LOGISTIC_CENTER_ID,
        Set.of(FBM_WMS_OUTBOUND),
        Set.of(STEP, PATH, PhotoGrouper.DATE_OUT),
        Set.of(PENDING, TO_ROUTE, TO_SORT, PhotoStep.PICKED, TO_PACK),
        PHOTO_DATE,
        DATE_TO
    );

    when(backlogApiClient.getPhotos(request)).thenReturn(Collections.singletonList(mockBacklogsAPI));

    final var response =
        backlogHistoricalAdapter.getBacklogByDateProcessAndPP(
            Workflow.FBM_WMS_OUTBOUND,
            LOGISTIC_CENTER_ID,
            Set.of(WAVING, BATCH_SORTER, PACKING_WALL),
            PHOTO_DATE,
            DATE_TO);

    final Map<Instant, Map<ProcessName, Map<Instant, Map<ProcessPathName, Integer>>>> expected =
        Map.of(
            PHOTO_DATE, Map.of(
                WAVING, Map.of(
                    Instant.parse(DATE_OUT_1),
                    Map.of(ProcessPathName.TOT_MULTI_BATCH, 700),
                    Instant.parse(DATE_OUT_2),
                    Map.of(ProcessPathName.TOT_MULTI_BATCH, 700)),
                BATCH_SORTER, Map.of(
                    Instant.parse(DATE_OUT_1),
                    Map.of(
                        ProcessPathName.TOT_MULTI_BATCH, 500,
                        ProcessPathName.NON_TOT_MULTI_BATCH, 1000),
                    Instant.parse(DATE_OUT_2),
                    Map.of(ProcessPathName.TOT_MULTI_BATCH, 500)),
                PACKING_WALL, Map.of(
                    Instant.parse(DATE_OUT_1),
                    Map.of(ProcessPathName.TOT_MULTI_BATCH, 1000),
                    Instant.parse(DATE_OUT_2),
                    Map.of(ProcessPathName.TOT_MULTI_BATCH, 1000))
            )
        );
    assertEquals(expected.get(PHOTO_DATE).get(WAVING).get(Instant.parse(DATE_OUT_1)).get(ProcessPathName.TOT_MULTI_BATCH),
        response.get(PHOTO_DATE).get(WAVING).get(Instant.parse(DATE_OUT_1)).get(ProcessPathName.TOT_MULTI_BATCH));
    assertEquals(expected.get(PHOTO_DATE).get(PACKING_WALL).get(Instant.parse(DATE_OUT_1)).get(ProcessPathName.TOT_MULTI_BATCH),
        response.get(PHOTO_DATE).get(PACKING_WALL).get(Instant.parse(DATE_OUT_1)).get(ProcessPathName.TOT_MULTI_BATCH));
    assertEquals(expected.get(PHOTO_DATE).get(BATCH_SORTER).get(Instant.parse(DATE_OUT_1)).get(ProcessPathName.TOT_MULTI_BATCH),
        response.get(PHOTO_DATE).get(BATCH_SORTER).get(Instant.parse(DATE_OUT_1)).get(ProcessPathName.TOT_MULTI_BATCH));
    assertEquals(expected.get(PHOTO_DATE).get(BATCH_SORTER).get(Instant.parse(DATE_OUT_1)).get(ProcessPathName.NON_TOT_MULTI_BATCH),
        response.get(PHOTO_DATE).get(BATCH_SORTER).get(Instant.parse(DATE_OUT_1)).get(ProcessPathName.NON_TOT_MULTI_BATCH));
    assertEquals(expected.get(PHOTO_DATE).get(WAVING).get(Instant.parse(DATE_OUT_2)).get(ProcessPathName.TOT_MULTI_BATCH),
        response.get(PHOTO_DATE).get(WAVING).get(Instant.parse(DATE_OUT_2)).get(ProcessPathName.TOT_MULTI_BATCH));
    assertEquals(expected.get(PHOTO_DATE).get(PACKING_WALL).get(Instant.parse(DATE_OUT_2)).get(ProcessPathName.TOT_MULTI_BATCH),
        response.get(PHOTO_DATE).get(PACKING_WALL).get(Instant.parse(DATE_OUT_2)).get(ProcessPathName.TOT_MULTI_BATCH));
    assertEquals(expected.get(PHOTO_DATE).get(BATCH_SORTER).get(Instant.parse(DATE_OUT_2)).get(ProcessPathName.TOT_MULTI_BATCH),
        response.get(PHOTO_DATE).get(BATCH_SORTER).get(Instant.parse(DATE_OUT_2)).get(ProcessPathName.TOT_MULTI_BATCH));
  }

  @Test
  void backlogByDateProcessDateOutAndProcessPathPWhenGetsNullResponse() {
    final PhotoRequest request = new PhotoRequest(
        LOGISTIC_CENTER_ID,
        Set.of(FBM_WMS_OUTBOUND),
        Set.of(STEP, PATH, PhotoGrouper.DATE_OUT),
        Set.of(PENDING, TO_ROUTE, TO_SORT, PhotoStep.PICKED, TO_PACK),
        PHOTO_DATE,
        DATE_TO
    );

    when(backlogApiClient.getPhotos(request)).thenReturn(null);

    final var response =
        backlogHistoricalAdapter.getBacklogByDateProcessAndPP(
            Workflow.FBM_WMS_OUTBOUND,
            LOGISTIC_CENTER_ID,
            Set.of(WAVING, BATCH_SORTER, PACKING_WALL),
            PHOTO_DATE,
            DATE_TO);
    assertEquals(Map.of(), response);
  }

  @Test
  void backlogByDateProcessDateOutAndProcessPathWhenGetsEmptyResponse() {
    final PhotoRequest request = new PhotoRequest(
        LOGISTIC_CENTER_ID,
        Set.of(FBM_WMS_OUTBOUND),
        Set.of(STEP, PATH, PhotoGrouper.DATE_OUT),
        Set.of(PENDING, TO_ROUTE, TO_SORT, PhotoStep.PICKED, TO_PACK),
        PHOTO_DATE,
        DATE_TO
    );

    when(backlogApiClient.getPhotos(request)).thenReturn(Collections.singletonList(new PhotoResponse(PHOTO_DATE, List.of())));

    final var response =
        backlogHistoricalAdapter.getBacklogByDateProcessAndPP(
            Workflow.FBM_WMS_OUTBOUND,
            LOGISTIC_CENTER_ID,
            Set.of(WAVING, BATCH_SORTER, PACKING_WALL),
            PHOTO_DATE,
            DATE_TO);
    assertEquals(Map.of(PHOTO_DATE, Map.of()), response);
  }
}
