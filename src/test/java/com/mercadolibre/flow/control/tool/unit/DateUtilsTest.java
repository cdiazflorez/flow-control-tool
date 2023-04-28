package com.mercadolibre.flow.control.tool.unit;

import static com.mercadolibre.flow.control.tool.util.DateUtils.isDifferenceBetweenDateBiggestThan;
import static com.mercadolibre.flow.control.tool.util.DateUtils.validDates;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DateUtilsTest {

  private static final Instant DATE_FROM = Instant.parse("2023-04-25T08:00:00Z");

  private static final Instant DATE_TO = Instant.parse("2023-04-26T08:00:00Z");

  @Test
  void testValidDatesFalse() {
    Assertions.assertFalse(validDates(DATE_FROM, DATE_TO));
  }

  @Test
  void testValidDatesTrue() {
    Assertions.assertTrue(validDates(DATE_TO, DATE_FROM));
  }

  @Test
  void testDifferenceBetweenTwoInstantDurationForHoursFalse() {
    Assertions.assertFalse(isDifferenceBetweenDateBiggestThan(DATE_FROM, DATE_TO, 30));
  }

  @Test
  void testDifferenceBetweenTwoInstantDurationForHoursTrue() {
    Assertions.assertTrue(
        isDifferenceBetweenDateBiggestThan(DATE_FROM, DATE_FROM.plus(40, ChronoUnit.HOURS), 30)
    );
  }
}
