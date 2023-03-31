package com.mercadolibre.flow.control.tool.feature.status.usecase.entity.editor;

import static com.mercadolibre.flow.control.tool.feature.entity.Workflow.FBM_WMS_OUTBOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.mercadolibre.flow.control.tool.feature.editor.WorkflowEditor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkflowEditorTest {

  @InjectMocks
  WorkflowEditor workflowEditor;

  @Test
  void testSetAsTextBlank() {

    // GIVEN
    String expectedMessage = "Value should not be blank";

    // WHEN
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      workflowEditor.setAsText("");
    });

    // THEN
    assertEquals(expectedMessage, exception.getMessage());
  }

  @Test
  void testSetAsTextNotFound() {

    // GIVEN
    String expectedMessage = "No enum constant";

    // WHEN
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      workflowEditor.setAsText("test");
    });

    // THEN
    assertTrue(exception.getMessage().contains(expectedMessage));
  }

  @Test
  void testSetAsTextSuccess() {
    // GIVEN
    final WorkflowEditor workflow = mock(WorkflowEditor.class);
    doNothing().when(workflow).setAsText(FBM_WMS_OUTBOUND.getName());

    // WHEN
    workflow.setAsText(FBM_WMS_OUTBOUND.getName());

    // THEN
    verify(workflow, times(1)).setAsText(FBM_WMS_OUTBOUND.getName());
  }
}
