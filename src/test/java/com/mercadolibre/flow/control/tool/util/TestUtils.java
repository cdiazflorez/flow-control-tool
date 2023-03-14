package com.mercadolibre.flow.control.tool.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.io.IOUtils;

/**
 * Class containing the common methods and values used across the tests.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestUtils {
  public static final String LOGISTIC_CENTER_ID = "ARTW01";

  /**
   * Load as String a resource located in the given resourceName.
   *
   * @param resourceName location and/or name of resource
   * @return String with the file content
   * @throws IllegalStateException when file cannot be found
   */
  public static String getResourceAsString(final String resourceName) {
    final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    try (InputStream resource = classLoader.getResourceAsStream(resourceName)) {
      assert resource != null;
      return IOUtils.toString(resource, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }
}
