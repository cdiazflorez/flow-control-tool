package com.mercadolibre.flow.control.tool.client.backlog;

import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.DOCUMENTED;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.GROUPED;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.GROUPING;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.PACKED;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.PENDING;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.PICKING;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.SORTED;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.TO_DISPATCH;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.TO_DOCUMENT;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.TO_GROUP;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.TO_OUT;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.TO_PACK;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.TO_PICK;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.TO_ROUTE;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.TO_SORT;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoWorkflow.FBM_WMS_OUTBOUND;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.BATCH_SORTER;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.HU_ASSEMBLY;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.PACKING;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.PACKING_WALL;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.SHIPPING;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.WALL_IN;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.WAVING;
import static com.mercadolibre.flow.control.tool.util.TestUtils.getResourceAsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mercadolibre.flow.control.tool.client.backlog.adapter.BacklogByProcessAdapter;
import com.mercadolibre.flow.control.tool.client.backlog.dto.LastPhotoRequest;
import com.mercadolibre.flow.control.tool.client.backlog.dto.PhotoResponse;
import com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoGrouper;
import com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BacklogAdapterTest {
  private static final Instant PHOTO_DATE = Instant.parse("2023-03-16T10:00:00Z");
  private static final String LOGISTIC_CENTER_ID = "ARTW01";
  private static final String TOT_MULTI_BATCH = "TOT_MULTI_BATCH";
  private static final String STEP = "step";
  private static final String PATH = "path";
  private static final String PICKED = "picked";
  private static final String NON_TOT_MULTI_BATCH = "NON_TOT_MULTI_BATCH";
  @InjectMocks
  BacklogByProcessAdapter backlogAdapter;

  @Mock
  BacklogApiClient backlogApiClient;

  @Test
  void backlogsByStepDummyCase() {
    final PhotoResponse mockBacklogsAPI = new PhotoResponse(
        PHOTO_DATE,
        List.of(
            new PhotoResponse.Group(Map.of(PATH, TOT_MULTI_BATCH, STEP, PICKED), 500),
            new PhotoResponse.Group(Map.of(PATH, TOT_MULTI_BATCH, STEP, "to_pack"), 1000),
            new PhotoResponse.Group(Map.of(PATH, TOT_MULTI_BATCH, STEP, "pending"), 700)
        ));

    final LastPhotoRequest request = new LastPhotoRequest(
        LOGISTIC_CENTER_ID,
        Set.of(FBM_WMS_OUTBOUND),
        Set.of(PhotoGrouper.STEP, PhotoGrouper.PATH),
        Set.of(PENDING, TO_ROUTE, TO_SORT, PhotoStep.PICKED, TO_PACK),
        PHOTO_DATE
    );

    when(backlogApiClient.getLastPhoto(request)).thenReturn(mockBacklogsAPI);

    final var response =
        backlogAdapter.getBacklogTotalsByProcess(LOGISTIC_CENTER_ID, Workflow.FBM_WMS_OUTBOUND, Set.of(WAVING, BATCH_SORTER, PACKING_WALL),
            PHOTO_DATE);

    final Map<ProcessName, Integer> expected = Map.of(WAVING, 700, BATCH_SORTER, 500, PACKING_WALL, 1000);
    assertEquals(expected.get(WAVING), response.get(WAVING));
    assertEquals(expected.get(PACKING_WALL), response.get(PACKING_WALL));
    assertEquals(expected.get(BATCH_SORTER), response.get(BATCH_SORTER));

  }

  @Test
  void backlogsByStepHappyCase() {
    final PhotoResponse mockBacklogsAPI = new PhotoResponse(
        PHOTO_DATE,
        List.of(
            new PhotoResponse.Group(Map.of(PATH, TOT_MULTI_BATCH, STEP, "packed"), 40000),
            new PhotoResponse.Group(Map.of(PATH, NON_TOT_MULTI_BATCH, STEP, "picked"), 443),
            new PhotoResponse.Group(Map.of(PATH, "NON_TOT_MONO", STEP, "to_sort"), 2000),
            new PhotoResponse.Group(Map.of(PATH, TOT_MULTI_BATCH, STEP, "packing"), 10000),
            new PhotoResponse.Group(Map.of(PATH, "TOT_MONO", STEP, "picked"), 302),
            new PhotoResponse.Group(Map.of(PATH, "TOT_MULTI_ORDER", STEP, "to_pack"), 700),
            new PhotoResponse.Group(Map.of(PATH, TOT_MULTI_BATCH, STEP, "to_route"), 553),
            new PhotoResponse.Group(Map.of(PATH, TOT_MULTI_BATCH, STEP, "sorted"), 1000),
            new PhotoResponse.Group(Map.of(PATH, TOT_MULTI_BATCH, STEP, "pending"), 700),
            new PhotoResponse.Group(Map.of(PATH, TOT_MULTI_BATCH, STEP, "to_document"), 500),
            new PhotoResponse.Group(Map.of(PATH, TOT_MULTI_BATCH, STEP, "documented"), 500),
            new PhotoResponse.Group(Map.of(PATH, TOT_MULTI_BATCH, STEP, "grouping"), 10)
        ));

    final LastPhotoRequest request = new LastPhotoRequest(
        LOGISTIC_CENTER_ID,
        Set.of(FBM_WMS_OUTBOUND),
        Set.of(PhotoGrouper.STEP, PhotoGrouper.PATH),
        Set.of(PENDING, TO_PICK, PICKING, PhotoStep.PICKED, TO_GROUP, GROUPING, GROUPED, TO_ROUTE, TO_SORT, SORTED,
            TO_PACK, PhotoStep.PACKING, PACKED, TO_DOCUMENT, DOCUMENTED, TO_DISPATCH, TO_OUT),
        PHOTO_DATE
    );

    when(backlogApiClient.getLastPhoto(request)).thenReturn(mockBacklogsAPI);

    final var response =
        backlogAdapter.getBacklogTotalsByProcess(
            LOGISTIC_CENTER_ID,
            Workflow.FBM_WMS_OUTBOUND,
            Set.of(WAVING, ProcessName.PICKING, BATCH_SORTER, WALL_IN, PACKING, PACKING_WALL, HU_ASSEMBLY, SHIPPING),
            PHOTO_DATE);

    final Map<ProcessName, Integer> expected = Map.of(
        WAVING, 1253,
        BATCH_SORTER, 2443,
        WALL_IN, 1010,
        PACKING, 1002,
        HU_ASSEMBLY, 51000
    );
    // compare the returned response with what should have been returned.
    assertEquals(expected.get(WAVING), response.get(WAVING));
    assertEquals(expected.get(ProcessName.PICKING), response.get(ProcessName.PICKING));
    assertEquals(expected.get(BATCH_SORTER), response.get(BATCH_SORTER));
    assertEquals(expected.get(WALL_IN), response.get(WALL_IN));
    assertEquals(expected.get(PACKING), response.get(PACKING));
    assertEquals(expected.get(PACKING_WALL), response.get(PACKING_WALL));
    assertEquals(expected.get(HU_ASSEMBLY), response.get(HU_ASSEMBLY));
    assertEquals(expected.get(SHIPPING), response.get(SHIPPING));

  }

  @Test
  void backlogByProcessWhenProcessPathNotExist() {
    final PhotoResponse mockBacklogsAPI = new PhotoResponse(
        PHOTO_DATE,
        List.of(
            new PhotoResponse.Group(Map.of(PATH, TOT_MULTI_BATCH, STEP, "TO_ROUTE"), 500),
            new PhotoResponse.Group(Map.of(PATH, "TOT_MULTI_BATCH_DOS", STEP, PICKED), 500)
        ));

    final LastPhotoRequest request = new LastPhotoRequest(
        LOGISTIC_CENTER_ID,
        Set.of(FBM_WMS_OUTBOUND),
        Set.of(PhotoGrouper.PATH, PhotoGrouper.STEP),
        Set.of(PENDING, TO_ROUTE),
        PHOTO_DATE
    );

    when(backlogApiClient.getLastPhoto(request)).thenReturn(mockBacklogsAPI);

    final var response = backlogAdapter.getBacklogTotalsByProcess(
        LOGISTIC_CENTER_ID,
        Workflow.FBM_WMS_OUTBOUND,
        Set.of(WAVING),
        PHOTO_DATE
    );

    final Map<ProcessName, Integer> expected = Map.of(WAVING, 500);
    assertEquals(expected.keySet().size(), response.keySet().size());
    assertEquals(expected.get(WAVING), response.get(WAVING));
  }

  @Test
  void backlogByProcessWhenBacklogsIsNull() {
    final LastPhotoRequest request = new LastPhotoRequest(
        LOGISTIC_CENTER_ID,
        Set.of(FBM_WMS_OUTBOUND),
        Set.of(PhotoGrouper.PATH, PhotoGrouper.STEP),
        Set.of(PENDING, TO_ROUTE),
        PHOTO_DATE
    );
    when(backlogApiClient.getLastPhoto(request)).thenReturn(null);
    final var response = backlogAdapter.getBacklogTotalsByProcess(
        LOGISTIC_CENTER_ID,
        Workflow.FBM_WMS_OUTBOUND,
        Set.of(WAVING),
        PHOTO_DATE
    );
    assertEquals(Map.of(), response);
  }

  @Test
  void exhaustiveTest() throws JsonProcessingException {

    final LastPhotoRequest request = new LastPhotoRequest(
        LOGISTIC_CENTER_ID,
        Set.of(FBM_WMS_OUTBOUND),
        Set.of(PhotoGrouper.STEP, PhotoGrouper.PATH),
        Set.of(
            TO_PICK,
            TO_PACK,
            SORTED,
            GROUPING,
            TO_ROUTE,
            PhotoStep.PACKING,
            TO_DISPATCH,
            TO_GROUP,
            PACKED,
            TO_SORT,
            PENDING,
            PhotoStep.PICKED,
            TO_OUT,
            PICKING,
            TO_DOCUMENT,
            DOCUMENTED,
            GROUPED
        ),
        Instant.parse("2023-03-15T10:00:00Z")
    );

    final String jsonResponseBacklogPhotosLast = getResourceAsString(
        "client/response_get_backlog_api_photos_last.json"
    );
    final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    final PhotoResponse expectedBacklogPhotoResponse = objectMapper.readValue(
        jsonResponseBacklogPhotosLast,
        PhotoResponse.class
    );
    when(backlogApiClient.getLastPhoto(request)).thenReturn(expectedBacklogPhotoResponse);

    final Map<ProcessName, Integer> response = backlogAdapter.getBacklogTotalsByProcess(
        LOGISTIC_CENTER_ID,
        Workflow.FBM_WMS_OUTBOUND,
        Set.of(
            WAVING,
            ProcessName.PICKING,
            BATCH_SORTER,
            WALL_IN,
            PACKING,
            PACKING_WALL,
            HU_ASSEMBLY,
            SHIPPING
        ),
        Instant.parse("2023-03-15T10:00:00Z")
    );

    final Map<ProcessName, Integer> expected = Map.of(
        WAVING, 32628,
        ProcessName.PICKING, 3509,
        BATCH_SORTER, 1306,
        WALL_IN, 5027,
        PACKING, 2846,
        PACKING_WALL, 579,
        HU_ASSEMBLY, 26,
        SHIPPING, 20740);

    // compare the returned response with what should have been returned.
    assertEquals(expected.get(WAVING), response.get(WAVING));
    assertEquals(expected.get(ProcessName.PICKING), response.get(ProcessName.PICKING));
    assertEquals(expected.get(BATCH_SORTER), response.get(BATCH_SORTER));
    assertEquals(expected.get(WALL_IN), response.get(WALL_IN));
    assertEquals(expected.get(PACKING), response.get(PACKING));
    assertEquals(expected.get(PACKING_WALL), response.get(PACKING_WALL));
    assertEquals(expected.get(HU_ASSEMBLY), response.get(HU_ASSEMBLY));
    assertEquals(expected.get(SHIPPING), response.get(SHIPPING));
  }

}
