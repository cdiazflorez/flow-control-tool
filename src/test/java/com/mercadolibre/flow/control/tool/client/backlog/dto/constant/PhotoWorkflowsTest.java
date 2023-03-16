package com.mercadolibre.flow.control.tool.client.backlog.dto.constant;

import static com.mercadolibre.flow.control.tool.util.TestUtils.FBM_WMS_OUTBOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mercadolibre.flow.control.tool.feature.status.usecase.constant.Workflow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PhotoWorkflowsTest {

  @Test
  void testFromGetName() {
    // GIVEN
    final PhotoWorkflows expectedBacklogPhotoWorkflows = PhotoWorkflows.FBM_WMS_OUTBOUND;

    // WHEN
    final PhotoWorkflows backlogPhotoWorkflowsOutFromText = PhotoWorkflows.from(FBM_WMS_OUTBOUND);
    final PhotoWorkflows backlogPhotoWorkflowsOutFromWorkflow = PhotoWorkflows.from(Workflow.FBM_WMS_OUTBOUND);

    // THEN
    assertEquals(expectedBacklogPhotoWorkflows, backlogPhotoWorkflowsOutFromText);
    assertEquals(FBM_WMS_OUTBOUND, backlogPhotoWorkflowsOutFromText.getName());
    assertEquals(expectedBacklogPhotoWorkflows, backlogPhotoWorkflowsOutFromWorkflow);
    assertEquals(FBM_WMS_OUTBOUND, backlogPhotoWorkflowsOutFromWorkflow.getName());
  }

}
