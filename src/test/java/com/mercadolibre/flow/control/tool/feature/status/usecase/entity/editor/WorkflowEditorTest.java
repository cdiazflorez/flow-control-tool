package com.mercadolibre.flow.control.tool.feature.status.usecase.entity.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
