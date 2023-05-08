package com.mercadolibre.flow.control.tool.feature.status.usecase.entity.editor;

import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.ProcessPath.NON_TOT_MONO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.mercadolibre.flow.control.tool.feature.editor.ProcessPathEditor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProcessPathNameEditorTest {

  @InjectMocks
  private ProcessPathEditor processPathEditor;

  @Test
  void testSetAsTextBlank() {
    // GIVEN
    String expectedMessage = "Value should not be blank";

    // WHEN
    Exception exception = assertThrows(IllegalArgumentException.class, () -> processPathEditor.setAsText(""));

    // THEN
    assertEquals(expectedMessage, exception.getMessage());
  }

  @Test
  void testSetAsTextNotFound() {
    // GIVEN
    String expectedMessage = "instead it should be one of";

    // WHEN
    Exception exception = assertThrows(IllegalArgumentException.class, () -> processPathEditor.setAsText("test"));

    // THEN
    assertTrue(exception.getMessage().contains(expectedMessage));
  }

  @Test
  void testSetAsTextSuccess() {
    // GIVEN
    final ProcessPathEditor pathEditor = mock(ProcessPathEditor.class);
    doNothing().when(pathEditor).setAsText(NON_TOT_MONO.getName());

    // WHEN
    pathEditor.setAsText(NON_TOT_MONO.getName());

    // THEN
    verify(pathEditor, times(1)).setAsText(NON_TOT_MONO.getName());
  }
}
