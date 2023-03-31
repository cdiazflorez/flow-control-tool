package com.mercadolibre.flow.control.tool.util;

import static java.time.temporal.ChronoUnit.HOURS;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercadolibre.json.JsonUtils;
import com.mercadolibre.json_jackson.JsonJackson;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.io.IOUtils;

/**
 * Class containing the common methods and values used across the tests.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestUtils {
  public static final String LOGISTIC_CENTER_ID = "ARTW01";

  public static final String FBM_WMS_OUTBOUND = "fbm_wms_outbound";

  public static final Instant VIEW_DATE_INSTANT = Instant.parse("2023-03-06T10:00:00Z");

  public static final Instant DATE_FROM = Instant.parse("2023-03-24T12:00:00Z");

  public static final Instant DATE_TO = DATE_FROM.plus(6, HOURS);

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

  /**
   * build an object instance to use in tests.
   * @return and {@link ObjectMapper} instance
   */
  public static ObjectMapper objectMapper() {
    return ((JsonJackson) JsonUtils.INSTANCE.getEngine())
            .getMapper();
  }
}
