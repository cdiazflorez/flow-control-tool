package com.mercadolibre.flow.control.tool.client.backlog.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

/**
 * Class with all the constants/enums, used for BacklogsApi consumption.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BacklogPhotoConstants {

  /**
   * Needed query params used into GET request for BacklogApi photos/.
   */
  public enum BacklogPhotoQueryParams {

    LOGISTIC_CENTER_ID,
    WORKFLOWS,
    GROUP_BY,
    PHOTO_DATE_TO,
    STEPS
  }

  /**
   * Possible values for "group_by" param for BacklogApi photos/.
   */
  public enum BacklogPhotoGrouper {

    AREA,
    STEP,
    PATH
  }

  /**
   * Possible values for workflow param in BacklogApi photos/.
   */
  @Getter
  @RequiredArgsConstructor
  public enum BacklogPhotoWorkflows {

    FBM_WMS_OUTBOUND("outbound-orders");

    private final String backlogPhotoWorkflow;
  }

  /**
   * Possible values for steps param in BacklogApi photos/.
   */
  public enum BacklogPhotoSteps {

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
    TO_OUT
  }
}
