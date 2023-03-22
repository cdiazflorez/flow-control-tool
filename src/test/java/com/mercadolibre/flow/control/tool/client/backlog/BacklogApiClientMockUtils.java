package com.mercadolibre.flow.control.tool.client.backlog;

import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoGrouper.AREA;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoGrouper.PATH;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoGrouper.STEP;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.GROUPED;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.GROUPING;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.PACKED;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.PACKING;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.PACKING_WALL;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.PENDING;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.PICKED;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.PICKING;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.SORTED;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.TO_DISPATCH;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.TO_GROUP;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.TO_OUT;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.TO_PACK;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.TO_PICK;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.TO_SORT;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoWorkflow.FBM_WMS_OUTBOUND;
import static com.mercadolibre.flow.control.tool.util.TestUtils.LOGISTIC_CENTER_ID;
import static com.mercadolibre.flow.control.tool.util.TestUtils.VIEW_DATE_INSTANT;

import com.mercadolibre.flow.control.tool.client.backlog.dto.LastPhotoRequest;
import com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep;
import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Class containing the common methods and values used in BacklogApiClient tests.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BacklogApiClientMockUtils {

  public static final String BACKLOG_PHOTO_LAST_URL = "/fbm/flow/backlogs/logistic_centers/%s/photos/last";

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
        VIEW_DATE_INSTANT
    );
  }

  /**
   * Mock the full list BacklogPhotoSteps that can be used on BacklogPhotosLastRequest.
   *
   * @return full list BacklogPhotoSteps.
   */
  public static Set<PhotoStep> mockListOfBacklogPhotoSteps() {
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
