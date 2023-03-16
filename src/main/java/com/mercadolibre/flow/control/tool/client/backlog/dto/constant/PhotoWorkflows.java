package com.mercadolibre.flow.control.tool.client.backlog.dto.constant;

import com.mercadolibre.flow.control.tool.feature.status.usecase.constant.Workflow;
import java.util.Locale;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Possible values for workflow param in BacklogApi photos/.
 */
@Getter
@RequiredArgsConstructor
public enum PhotoWorkflows {

  FBM_WMS_OUTBOUND("outbound-orders");

  public final String alias;

  public static PhotoWorkflows from(final String value) {
    return valueOf(value.toUpperCase(Locale.getDefault()));
  }

  public static PhotoWorkflows from(final Workflow workflow) {
    return valueOf(workflow.name());
  }

  public String getName() {
    return name().toLowerCase(Locale.getDefault());
  }
}

