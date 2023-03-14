package com.mercadolibre.flow.control.tool.client.backlog;

import static com.mercadolibre.flow.control.tool.client.backlog.dto.BacklogPhotoConstants.BacklogPhotoGrouper.AREA;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.BacklogPhotoConstants.BacklogPhotoGrouper.PATH;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.BacklogPhotoConstants.BacklogPhotoGrouper.STEP;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.BacklogPhotoConstants.BacklogPhotoSteps.GROUPED;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.BacklogPhotoConstants.BacklogPhotoSteps.GROUPING;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.BacklogPhotoConstants.BacklogPhotoSteps.PACKED;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.BacklogPhotoConstants.BacklogPhotoSteps.PACKING;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.BacklogPhotoConstants.BacklogPhotoSteps.PACKING_WALL;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.BacklogPhotoConstants.BacklogPhotoSteps.PENDING;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.BacklogPhotoConstants.BacklogPhotoSteps.PICKED;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.BacklogPhotoConstants.BacklogPhotoSteps.PICKING;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.BacklogPhotoConstants.BacklogPhotoSteps.SORTED;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.BacklogPhotoConstants.BacklogPhotoSteps.TO_DISPATCH;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.BacklogPhotoConstants.BacklogPhotoSteps.TO_GROUP;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.BacklogPhotoConstants.BacklogPhotoSteps.TO_OUT;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.BacklogPhotoConstants.BacklogPhotoSteps.TO_PACK;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.BacklogPhotoConstants.BacklogPhotoSteps.TO_PICK;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.BacklogPhotoConstants.BacklogPhotoSteps.TO_SORT;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.BacklogPhotoConstants.BacklogPhotoWorkflows.FBM_WMS_OUTBOUND;
import static com.mercadolibre.flow.control.tool.util.TestUtils.LOGISTIC_CENTER_ID;

import com.mercadolibre.flow.control.tool.client.backlog.dto.BacklogPhotoConstants.BacklogPhotoSteps;
import com.mercadolibre.flow.control.tool.client.backlog.dto.LastPhotoRequest;
import java.time.Instant;
import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Class containing the common methods and values used in BacklogApiClient tests.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BacklogApiClientMockUtils {

  public static final String BACKLOG_PHOTO_LAST_URL = "/fbm/flow/backlogs/logistic_centers/%s/photos/last";

  public static final Instant VIEW_DATE = Instant.parse("2023-03-06T10:00:00Z");

  /**
   * Mock BacklogPhotosLastRequest using some common values in utils.
   *
   * @return an example of BacklogPhotosLastRequest using common logistic_center in TestUtils.
   */
  public static LastPhotoRequest mockBacklogPhotosLastRequest() {
    return new LastPhotoRequest(
        LOGISTIC_CENTER_ID,
        Set.of(FBM_WMS_OUTBOUND),
        Set.of(STEP, AREA, PATH),
        mockListOfBacklogPhotoSteps(),
        VIEW_DATE
    );
  }

  /**
   * Mock the full list BacklogPhotoSteps that can be used on BacklogPhotosLastRequest.
   *
   * @return full list BacklogPhotoSteps.
   */
  public static Set<BacklogPhotoSteps> mockListOfBacklogPhotoSteps() {
    return Set.of(
        PENDING,
        TO_PICK,
        PICKING,
        PICKED,
        TO_GROUP,
        GROUPING,
        GROUPED,
        TO_SORT,
        SORTED,
        TO_PACK,
        PACKING,
        PACKING_WALL,
        PACKED,
        TO_DISPATCH,
        TO_OUT);
  }
}
