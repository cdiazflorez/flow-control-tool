package com.mercadolibre.flow.control.tool.feature.deferral;

import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.mercadolibre.flow.control.tool.feature.deferral.dto.MaximumCapacityDataDto;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MaxCapacityUseCaseTest {

  private static final Instant DATE = Instant.parse("2023-06-15T08:00:00Z");

  @Mock
  private GetMaxCapacityGateway getMaxCapacityGateway;

  @InjectMocks
  private MaxCapacityUseCase maxCapacityUseCase;

  private static Map<Instant, Long> expectedDataGateway() {
    return Map.of(
        DATE, 100L,
        DATE.plus(1, ChronoUnit.HOURS), 200L,
        DATE.plus(2, ChronoUnit.HOURS), 300L
    );
  }

  private static List<MaximumCapacityDataDto> expectedDataUseCase() {
    return List.of(
        new MaximumCapacityDataDto(DATE, 100L),
        new MaximumCapacityDataDto(DATE.plus(1, ChronoUnit.HOURS), 200L),
        new MaximumCapacityDataDto(DATE.plus(2, ChronoUnit.HOURS), 300L)
    );
  }

  private static Stream<Arguments> provideMaxCapacityData() {
    return Stream.of(
        Arguments.of(
            expectedDataGateway(),
            expectedDataUseCase()
        ),
        Arguments.of(
            emptyMap(),
            List.of()
        )
    );
  }

  @ParameterizedTest
  @MethodSource("provideMaxCapacityData")
  void testGetMaxCapacity(
      final Map<Instant, Long> responseGateway,
      final List<MaximumCapacityDataDto> responseUseCase
  ) {
    // GIVEN
    final String logisticCenterId = "ARTW01";
    final Instant dateFrom = Instant.parse("2023-06-15T08:00:00Z");
    final Instant dateTo = Instant.parse("2023-06-15T10:00:00Z");

    when(getMaxCapacityGateway.getMaxCapacityForHour(logisticCenterId, dateFrom, dateTo)).thenReturn(responseGateway);

    // WHEN
    final List<MaximumCapacityDataDto> maxCapacity = maxCapacityUseCase.getMaxCapacity(logisticCenterId, dateFrom, dateTo);

    //THEN
    assertAll(
        () -> assertEquals(responseUseCase.size(), maxCapacity.size()),
        () -> assertTrue(responseUseCase.containsAll(maxCapacity)),
        () -> assertTrue(maxCapacity.containsAll(responseUseCase))
    );
  }
}
