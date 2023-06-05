package com.mercadolibre.flow.control.tool.feature.editor;

import static com.mercadolibre.flow.control.tool.feature.forecastdeviation.constant.Filter.DATE_IN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.mercadolibre.flow.control.tool.exception.FilterNotSupportedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FilterEditorTest {

  @InjectMocks
  private FilterEditor filterEditor;

  @Test
  void testSetAsTextBlank() {

    // GIVEN
    String expectedMessage = "Value should not be blank";

    // WHEN
    Exception exception = assertThrows(IllegalArgumentException.class, () -> filterEditor.setAsText(""));

    // THEN
    assertEquals(expectedMessage, exception.getMessage());
  }

  @Test
  void testSetAsTextNotFound() {
    // GIVEN
    String expectedMessage = "Filter: test not supported";

    // WHEN
    Exception exception = assertThrows(FilterNotSupportedException.class, () -> filterEditor.setAsText("test"));

    // THEN
    assertTrue(exception.getMessage().contains(expectedMessage));
  }

  @Test
  void testSetAsTextSuccess() {
    // GIVEN
    final FilterEditor filter = mock(FilterEditor.class);
    doNothing().when(filter).setAsText(DATE_IN.getName());

    // WHEN
    filter.setAsText(DATE_IN.getName());

    // THEN
    verify(filter, times(1)).setAsText(DATE_IN.getName());
  }

}
