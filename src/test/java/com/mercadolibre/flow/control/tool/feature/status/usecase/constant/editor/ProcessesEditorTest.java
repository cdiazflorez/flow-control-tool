package com.mercadolibre.flow.control.tool.feature.status.usecase.constant.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProcessesEditorTest {

  @InjectMocks
  ProcessesEditor processesEditor;

  @Test
  void testSetAsTextBlank() {

    // GIVEN
    String expectedMessage = "Value should not be blank";

    // WHEN
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      processesEditor.setAsText("");
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
      processesEditor.setAsText("test");
    });

    // THEN
    assertTrue(exception.getMessage().contains(expectedMessage));
  }

}
