package com.mercadolibre.flow.control.tool.feature.forecastdeviation;

import static com.mercadolibre.flow.control.tool.feature.entity.Workflow.FBM_WMS_OUTBOUND;
import static com.mercadolibre.flow.control.tool.feature.forecastdeviation.constant.Filter.DATE_IN;
import static com.mercadolibre.flow.control.tool.feature.forecastdeviation.constant.Filter.DATE_OUT;
import static com.mercadolibre.flow.control.tool.util.TestUtils.LOGISTIC_CENTER_ID;
import static java.lang.Math.round;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import com.mercadolibre.flow.control.tool.feature.forecastdeviation.constant.Filter;
import com.mercadolibre.flow.control.tool.feature.forecastdeviation.domain.ForecastDeviationData;
import com.mercadolibre.flow.control.tool.feature.forecastdeviation.domain.ForecastDeviationQuantity;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ForecastDeviationUseCaseTest {

  private static final Instant DATE_ONE = Instant.parse("2023-06-02T12:00:00Z");

  private static final Instant DATE_TWO = Instant.parse("2023-06-02T13:00:00Z");

  private static final Instant DATE_THREE = Instant.parse("2023-06-02T14:00:00Z");

  private static final double SALES_DISTRIBUTION_ONE = 5.0;

  private static final double SALES_DISTRIBUTION_TWO = 0.0;

  private static final double SALES_DISTRIBUTION_THREE = 15.0;

  private static final long REAL_SALES_ONE = 10L;

  private static final long REAL_SALES_TWO = 15L;

  private static final long REAL_SALES_THREE = 5L;

  private static final long DEVIATION_ONE = REAL_SALES_ONE - round(SALES_DISTRIBUTION_ONE);

  private static final long DEVIATION_TWO = REAL_SALES_TWO - round(SALES_DISTRIBUTION_TWO);

  private static final long DEVIATION_THREE = REAL_SALES_THREE - round(SALES_DISTRIBUTION_THREE);

  private static final double DEVIATION_PERCENTAGE_ONE = DEVIATION_ONE / SALES_DISTRIBUTION_ONE;

  private static final double DEVIATION_PERCENTAGE_TWO = 0;

  private static final double DEVIATION_PERCENTAGE_THREE = DEVIATION_THREE / SALES_DISTRIBUTION_THREE;

  private static final long TOTAL_PLANNED_ONE = round(SALES_DISTRIBUTION_ONE + SALES_DISTRIBUTION_TWO + SALES_DISTRIBUTION_THREE);

  private static final long TOTAL_PLANNED_TWO = round(SALES_DISTRIBUTION_ONE + SALES_DISTRIBUTION_TWO);

  private static final long TOTAL_REAL_ONE = REAL_SALES_ONE + REAL_SALES_TWO + REAL_SALES_THREE;

  private static final long TOTAL_REAL_TWO = REAL_SALES_ONE + REAL_SALES_TWO;

  private static final long TOTAL_DEVIATION_ONE = TOTAL_REAL_ONE - TOTAL_PLANNED_ONE;

  private static final long TOTAL_DEVIATION_TWO = TOTAL_REAL_TWO - TOTAL_PLANNED_TWO;

  private static final double TOTAL_DEVIATION_PERCENTAGE_ONE = (double) TOTAL_DEVIATION_ONE / (double) TOTAL_PLANNED_ONE;

  private static final double TOTAL_DEVIATION_PERCENTAGE_TWO = (double) TOTAL_DEVIATION_TWO / (double) TOTAL_PLANNED_TWO;


  @Mock
  private ForecastDeviationUseCase.SalesDistributionPlanGateway salesDistributionPlanGateway;

  @Mock
  private ForecastDeviationUseCase.RealSalesGateway realSalesGateway;

  @InjectMocks
  private ForecastDeviationUseCase forecastDeviationUseCase;

  @ParameterizedTest
  @MethodSource("provideArgumentsAndExpectedToForecastDeviation")
  @DisplayName("Forecast deviation is ok")
  void testForecastDeviationOk(final Instant viewDate,
                               final Filter filter,
                               final Map<Instant, Double> mockSalesDistribution,
                               final Map<Instant, Long> mockRealSales,
                               final ForecastDeviationQuantity expectedForecastQuantityTotal,
                               final Map<Instant, ForecastDeviationQuantity> expectedForecastQuantityDetails) {
    //GIVEN
    when(salesDistributionPlanGateway.getSalesDistributionPlanned(anyString(),
                                                                  any(Workflow.class),
                                                                  any(Filter.class),
                                                                  any(Instant.class),
                                                                  any(Instant.class),
                                                                  any(Instant.class),
                                                                  any(Instant.class))).thenReturn(mockSalesDistribution);

    when(realSalesGateway.getRealSales(anyString(),
                                       any(Workflow.class),
                                       any(Filter.class),
                                       any(Instant.class),
                                       any(Instant.class),
                                       any(Instant.class),
                                       any(Instant.class),
                                       any(Instant.class))).thenReturn(mockRealSales);

    //WHEN
    final ForecastDeviationData forecastDeviationData = forecastDeviationUseCase.getForecastDeviation(LOGISTIC_CENTER_ID,
                                                                                                      FBM_WMS_OUTBOUND,
                                                                                                      DATE_ONE,
                                                                                                      DATE_THREE,
                                                                                                      viewDate,
                                                                                                      filter);
    //THEN
    final ForecastDeviationQuantity total = forecastDeviationData.totalForecastDeviationQuantity();
    final Map<Instant, ForecastDeviationQuantity> forecastDeviationQuantityByDate = forecastDeviationData
        .forecastDeviationQuantityByDate();

    assertEquals(expectedForecastQuantityTotal.getPlanned(),
                 total.getPlanned(),
                 "Total planned is not equal");
    assertEquals(expectedForecastQuantityTotal.getReal(),
                 total.getReal(),
                 "Total real is not equal");
    assertEquals(expectedForecastQuantityTotal.getDeviation(),
                 total.getDeviation(),
                 "Total deviation is not equal");
    assertEquals(expectedForecastQuantityTotal.getDeviationPercentage(),
                 total.getDeviationPercentage(),
                 "Total deviation percentage is not equal");

    assertEquals(expectedForecastQuantityDetails.size(), forecastDeviationQuantityByDate.size());
    forecastDeviationQuantityByDate.forEach((date, forecastDeviationQuantity) -> {
      assertEquals(expectedForecastQuantityDetails.get(date).getPlanned(),
                   forecastDeviationQuantity.getPlanned(),
                   "Planned is not equal");
      assertEquals(expectedForecastQuantityDetails.get(date).getReal(),
                   forecastDeviationQuantity.getReal(),
                   "Real is not equal");
      assertEquals(expectedForecastQuantityDetails.get(date).getDeviation(),
                   forecastDeviationQuantity.getDeviation(),
                   "Deviation is not equal");
      assertEquals(expectedForecastQuantityDetails.get(date).getDeviationPercentage(),
                   forecastDeviationQuantity.getDeviationPercentage(),
                   "Deviation percentage is not equal");
    });

  }

  private static ForecastDeviationData expectedAllForecastDeviation() {
    final ForecastDeviationQuantity totalForecastDeviationQuantity = ForecastDeviationQuantity.builder()
        .planned(TOTAL_PLANNED_ONE)
        .real(TOTAL_REAL_ONE)
        .deviation(TOTAL_DEVIATION_ONE)
        .deviationPercentage(TOTAL_DEVIATION_PERCENTAGE_ONE)
        .build();
    final Map<Instant, ForecastDeviationQuantity> forecastDeviationQuantityByDate = Map.of(
        DATE_ONE, ForecastDeviationQuantity.builder()
            .planned(round(SALES_DISTRIBUTION_ONE))
            .real(REAL_SALES_ONE)
            .deviation(DEVIATION_ONE)
            .deviationPercentage(DEVIATION_PERCENTAGE_ONE)
            .build(),
        DATE_TWO, ForecastDeviationQuantity.builder()
            .planned(round(SALES_DISTRIBUTION_TWO))
            .real(REAL_SALES_TWO)
            .deviation(DEVIATION_TWO)
            .deviationPercentage(DEVIATION_PERCENTAGE_TWO)
            .build(),
        DATE_THREE, ForecastDeviationQuantity.builder()
            .planned(round(SALES_DISTRIBUTION_THREE))
            .real(REAL_SALES_THREE)
            .deviation(DEVIATION_THREE)
            .deviationPercentage(DEVIATION_PERCENTAGE_THREE)
            .build()
    );
    return new ForecastDeviationData(
        totalForecastDeviationQuantity,
        forecastDeviationQuantityByDate
    );
  }

  private static ForecastDeviationData expectedPartialRealForecastDeviation() {

    final ForecastDeviationQuantity totalForecastDeviationQuantity = ForecastDeviationQuantity.builder()
        .planned(TOTAL_PLANNED_TWO)
        .real(TOTAL_REAL_TWO)
        .deviation(TOTAL_DEVIATION_TWO)
        .deviationPercentage(TOTAL_DEVIATION_PERCENTAGE_TWO)
        .build();
    final Map<Instant, ForecastDeviationQuantity> forecastDeviationQuantityByDate = Map.of(
        DATE_ONE, ForecastDeviationQuantity.builder()
            .planned(round(SALES_DISTRIBUTION_ONE))
            .real(REAL_SALES_ONE)
            .deviation(DEVIATION_ONE)
            .deviationPercentage(DEVIATION_PERCENTAGE_ONE)
            .build(),
        DATE_TWO, ForecastDeviationQuantity.builder()
            .planned(round(SALES_DISTRIBUTION_TWO))
            .real(REAL_SALES_TWO)
            .deviation(DEVIATION_TWO)
            .deviationPercentage(DEVIATION_PERCENTAGE_TWO)
            .build(),
        DATE_THREE, ForecastDeviationQuantity.builder()
            .planned(round(SALES_DISTRIBUTION_THREE))
            .build()
    );
    return new ForecastDeviationData(
        totalForecastDeviationQuantity,
        forecastDeviationQuantityByDate
    );
  }

  private static ForecastDeviationData expectedOnlyPlannedForecastDeviation() {
    final ForecastDeviationQuantity totalForecastDeviationQuantity = ForecastDeviationQuantity.builder()
        .planned(TOTAL_PLANNED_ONE)
        .build();
    final Map<Instant, ForecastDeviationQuantity> forecastDeviationQuantityByDate = Map.of(
        DATE_ONE, ForecastDeviationQuantity.builder()
            .planned(round(SALES_DISTRIBUTION_ONE))
            .build(),
        DATE_TWO, ForecastDeviationQuantity.builder()
            .planned(round(SALES_DISTRIBUTION_TWO))
            .build(),
        DATE_THREE, ForecastDeviationQuantity.builder()
            .planned(round(SALES_DISTRIBUTION_THREE))
            .build()
    );
    return new ForecastDeviationData(
        totalForecastDeviationQuantity,
        forecastDeviationQuantityByDate
    );
  }

  private static Map<Instant, Double> mockSalesDistribution() {
    return Map.of(
        DATE_ONE, SALES_DISTRIBUTION_ONE,
        DATE_TWO, SALES_DISTRIBUTION_TWO,
        DATE_THREE, SALES_DISTRIBUTION_THREE
    );
  }

  private static Map<Instant, Long> mockRealSales() {
    return Map.of(
        DATE_ONE, REAL_SALES_ONE,
        DATE_TWO, REAL_SALES_TWO,
        DATE_THREE, REAL_SALES_THREE
    );
  }

  private static Stream<Arguments> provideArgumentsAndExpectedToForecastDeviation() {
    return Stream.of(
        Arguments.of(DATE_THREE,
                     DATE_IN,
                     mockSalesDistribution(),
                     mockRealSales(),
                     expectedAllForecastDeviation().totalForecastDeviationQuantity(),
                     expectedAllForecastDeviation().forecastDeviationQuantityByDate()
        ),
        Arguments.of(DATE_TWO,
                     DATE_IN,
                     mockSalesDistribution(),
                     mockRealSales(),
                     expectedPartialRealForecastDeviation().totalForecastDeviationQuantity(),
                     expectedPartialRealForecastDeviation().forecastDeviationQuantityByDate()
        ),
        Arguments.of(DATE_ONE.minus(1, DAYS),
                     DATE_IN,
                     mockSalesDistribution(),
                     mockRealSales(),
                     expectedOnlyPlannedForecastDeviation().totalForecastDeviationQuantity(),
                     expectedOnlyPlannedForecastDeviation().forecastDeviationQuantityByDate()
        ),
        Arguments.of(DATE_THREE,
                     DATE_OUT,
                     mockSalesDistribution(),
                     mockRealSales(),
                     expectedAllForecastDeviation().totalForecastDeviationQuantity(),
                     expectedAllForecastDeviation().forecastDeviationQuantityByDate()
        ),
        Arguments.of(DATE_TWO,
                     DATE_OUT,
                     mockSalesDistribution(),
                     mockRealSales(),
                     expectedPartialRealForecastDeviation().totalForecastDeviationQuantity(),
                     expectedPartialRealForecastDeviation().forecastDeviationQuantityByDate()
        ),
        Arguments.of(DATE_ONE.minus(1, DAYS),
                     DATE_OUT,
                     mockSalesDistribution(),
                     mockRealSales(),
                     expectedOnlyPlannedForecastDeviation().totalForecastDeviationQuantity(),
                     expectedOnlyPlannedForecastDeviation().forecastDeviationQuantityByDate()
        )
    );
  }

}
