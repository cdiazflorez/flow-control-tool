package com.mercadolibre.flow.control.tool.feature.forecastdeviation;

import static com.mercadolibre.flow.control.tool.feature.entity.Workflow.FBM_WMS_OUTBOUND;
import static com.mercadolibre.flow.control.tool.feature.forecastdeviation.constant.Filter.DATE_IN;
import static com.mercadolibre.flow.control.tool.feature.forecastdeviation.constant.Filter.DATE_OUT;
import static com.mercadolibre.flow.control.tool.util.TestUtils.LOGISTIC_CENTER_ID;
import static java.lang.String.format;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

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
class GetForecastDeviationUseCaseTest {

  private static final Instant DATE_ONE = Instant.parse("2023-06-02T12:00:00Z");

  private static final Instant DATE_TWO = Instant.parse("2023-06-02T13:00:00Z");

  private static final Instant DATE_THREE = Instant.parse("2023-06-02T14:00:00Z");

  private static final Instant VIEW_DATE_ONE = DATE_THREE.plus(1, DAYS);

  private static final Instant VIEW_DATE_TWO = DATE_TWO.plus(30, MINUTES);

  private static final Instant VIEW_DATE_THREE = DATE_ONE.minus(1, DAYS);

  private static final int DAYS_TO_SEARCH = 7;

  private static final int SALES_DISTRIBUTION_ONE = 5;

  private static final int SALES_DISTRIBUTION_TWO = 10;

  private static final int SALES_DISTRIBUTION_THREE = 15;

  private static final int PARTIAL_SALES_DISTRIBUTION = 5;

  private static final int REAL_SALES_ONE = 10;

  private static final int REAL_SALES_TWO = 15;

  private static final int REAL_SALES_THREE = 5;

  private static final int DEVIATION_ONE = REAL_SALES_ONE - SALES_DISTRIBUTION_ONE;

  private static final int DEVIATION_TWO = REAL_SALES_TWO - SALES_DISTRIBUTION_TWO;

  private static final int DEVIATION_THREE = REAL_SALES_THREE - SALES_DISTRIBUTION_THREE;

  private static final int DEVIATION_WITH_PARTIAL_SALES = REAL_SALES_TWO - PARTIAL_SALES_DISTRIBUTION;

  private static final double DEVIATION_PERCENTAGE_ONE = DEVIATION_ONE / (double) SALES_DISTRIBUTION_ONE;

  private static final double DEVIATION_PERCENTAGE_TWO = DEVIATION_TWO / (double) SALES_DISTRIBUTION_TWO;

  private static final double DEVIATION_PERCENTAGE_THREE = DEVIATION_THREE / (double) SALES_DISTRIBUTION_THREE;

  private static final double DEVIATION_PERCENTAGE_WITH_PARTIAL_SALES = DEVIATION_WITH_PARTIAL_SALES / (double) PARTIAL_SALES_DISTRIBUTION;

  private static final int TOTAL_PLANNED_ONE = SALES_DISTRIBUTION_ONE + SALES_DISTRIBUTION_TWO + SALES_DISTRIBUTION_THREE;

  private static final int TOTAL_PLANNED_TWO = SALES_DISTRIBUTION_ONE + PARTIAL_SALES_DISTRIBUTION;

  private static final int TOTAL_REAL_ONE = REAL_SALES_ONE + REAL_SALES_TWO + REAL_SALES_THREE;

  private static final int TOTAL_REAL_TWO = REAL_SALES_ONE + REAL_SALES_TWO;

  private static final int TOTAL_DEVIATION_ONE = TOTAL_REAL_ONE - TOTAL_PLANNED_ONE;

  private static final int TOTAL_DEVIATION_TWO = TOTAL_REAL_TWO - TOTAL_PLANNED_TWO;

  private static final double TOTAL_DEVIATION_PERCENTAGE_ONE = TOTAL_DEVIATION_ONE / (double) TOTAL_PLANNED_ONE;

  private static final double TOTAL_DEVIATION_PERCENTAGE_TWO = TOTAL_DEVIATION_TWO / (double) TOTAL_PLANNED_TWO;


  @Mock
  private GetForecastDeviationUseCase.SalesDistributionPlanGateway salesDistributionPlanGateway;

  @Mock
  private GetForecastDeviationUseCase.RealSalesGateway realSalesGateway;

  @InjectMocks
  private GetForecastDeviationUseCase getForecastDeviationUseCase;



  private static ForecastDeviationData expectedAllForecastDeviation() {
    final ForecastDeviationQuantity totalForecastDeviationQuantity = new ForecastDeviationQuantity(TOTAL_PLANNED_ONE,
                                                                                                   TOTAL_REAL_ONE,
                                                                                                   TOTAL_DEVIATION_ONE,
                                                                                                   TOTAL_DEVIATION_PERCENTAGE_ONE);
    return new ForecastDeviationData(
        totalForecastDeviationQuantity,
        Map.of(
            DATE_ONE, new ForecastDeviationQuantity(SALES_DISTRIBUTION_ONE,
                                                    REAL_SALES_ONE,
                                                    DEVIATION_ONE,
                                                    DEVIATION_PERCENTAGE_ONE),
            DATE_TWO, new ForecastDeviationQuantity(SALES_DISTRIBUTION_TWO,
                                                    REAL_SALES_TWO,
                                                    DEVIATION_TWO,
                                                    DEVIATION_PERCENTAGE_TWO),
            DATE_THREE, new ForecastDeviationQuantity(SALES_DISTRIBUTION_THREE,
                                                      REAL_SALES_THREE,
                                                      DEVIATION_THREE,
                                                      DEVIATION_PERCENTAGE_THREE)
        )
    );
  }

  private static ForecastDeviationData expectedPartialRealForecastDeviation() {

    final ForecastDeviationQuantity totalForecastDeviationQuantity = new ForecastDeviationQuantity(TOTAL_PLANNED_TWO,
                                                                                                   TOTAL_REAL_TWO,
                                                                                                   TOTAL_DEVIATION_TWO,
                                                                                                   TOTAL_DEVIATION_PERCENTAGE_TWO);

    return new ForecastDeviationData(
        totalForecastDeviationQuantity,
        Map.of(
            DATE_ONE, new ForecastDeviationQuantity(SALES_DISTRIBUTION_ONE,
                                                    REAL_SALES_ONE,
                                                    DEVIATION_ONE,
                                                    DEVIATION_PERCENTAGE_ONE),
            DATE_TWO, new ForecastDeviationQuantity(PARTIAL_SALES_DISTRIBUTION,
                                                    REAL_SALES_TWO,
                                                    DEVIATION_WITH_PARTIAL_SALES,
                                                    DEVIATION_PERCENTAGE_WITH_PARTIAL_SALES),
            DATE_THREE, new ForecastDeviationQuantity(SALES_DISTRIBUTION_THREE)
        )
    );
  }

  private static ForecastDeviationData expectedOnlyPlannedForecastDeviation() {
    final ForecastDeviationQuantity totalForecastDeviationQuantity = new ForecastDeviationQuantity(TOTAL_PLANNED_ONE);
    return new ForecastDeviationData(
        totalForecastDeviationQuantity,
        Map.of(
            DATE_ONE, new ForecastDeviationQuantity(SALES_DISTRIBUTION_ONE),
            DATE_TWO, new ForecastDeviationQuantity(SALES_DISTRIBUTION_TWO),
            DATE_THREE, new ForecastDeviationQuantity(SALES_DISTRIBUTION_THREE)
        )
    );
  }

  private static Map<Instant, Integer> mockSalesDistribution() {
    return Map.of(
        DATE_ONE, SALES_DISTRIBUTION_ONE,
        DATE_TWO, SALES_DISTRIBUTION_TWO,
        DATE_THREE, SALES_DISTRIBUTION_THREE
    );
  }

  private static Map<Instant, Integer> mockRealSales() {
    return Map.of(
        DATE_ONE, REAL_SALES_ONE,
        DATE_TWO, REAL_SALES_TWO,
        DATE_THREE, REAL_SALES_THREE
    );
  }

  private static Stream<Arguments> provideArgumentsAndExpectedToForecastDeviation() {
    return Stream.of(
        Arguments.of(DATE_ONE,
                     DATE_THREE,
                     DATE_ONE,
                     DATE_THREE.plus(DAYS_TO_SEARCH, DAYS),
                     VIEW_DATE_ONE,
                     DATE_IN,
                     mockSalesDistribution(),
                     mockRealSales(),
                     expectedAllForecastDeviation().totalForecastDeviationQuantity(),
                     expectedAllForecastDeviation().forecastDeviationQuantityByDate()
        ),
        Arguments.of(DATE_ONE,
                     DATE_THREE,
                     DATE_ONE,
                     DATE_THREE.plus(DAYS_TO_SEARCH, DAYS),
                     VIEW_DATE_TWO,
                     DATE_IN,
                     mockSalesDistribution(),
                     mockRealSales(),
                     expectedPartialRealForecastDeviation().totalForecastDeviationQuantity(),
                     expectedPartialRealForecastDeviation().forecastDeviationQuantityByDate()
        ),
        Arguments.of(DATE_ONE,
                     DATE_THREE,
                     DATE_ONE,
                     DATE_THREE.plus(DAYS_TO_SEARCH, DAYS),
                     VIEW_DATE_THREE,
                     DATE_IN,
                     mockSalesDistribution(),
                     mockRealSales(),
                     expectedOnlyPlannedForecastDeviation().totalForecastDeviationQuantity(),
                     expectedOnlyPlannedForecastDeviation().forecastDeviationQuantityByDate()
        ),
        Arguments.of(DATE_ONE.minus(DAYS_TO_SEARCH, DAYS),
                     DATE_THREE,
                     DATE_ONE,
                     DATE_THREE,
                     VIEW_DATE_ONE,
                     DATE_OUT,
                     mockSalesDistribution(),
                     mockRealSales(),
                     expectedAllForecastDeviation().totalForecastDeviationQuantity(),
                     expectedAllForecastDeviation().forecastDeviationQuantityByDate()
        ),
        Arguments.of(DATE_ONE.minus(DAYS_TO_SEARCH, DAYS),
                     DATE_THREE,
                     DATE_ONE,
                     DATE_THREE,
                     VIEW_DATE_TWO,
                     DATE_OUT,
                     mockSalesDistribution(),
                     mockRealSales(),
                     expectedPartialRealForecastDeviation().totalForecastDeviationQuantity(),
                     expectedPartialRealForecastDeviation().forecastDeviationQuantityByDate()
        ),
        Arguments.of(DATE_ONE.minus(DAYS_TO_SEARCH, DAYS),
                     DATE_THREE,
                     DATE_ONE,
                     DATE_THREE,
                     VIEW_DATE_THREE,
                     DATE_OUT,
                     mockSalesDistribution(),
                     mockRealSales(),
                     expectedOnlyPlannedForecastDeviation().totalForecastDeviationQuantity(),
                     expectedOnlyPlannedForecastDeviation().forecastDeviationQuantityByDate()
        )
    );
  }

  @ParameterizedTest
  @MethodSource("provideArgumentsAndExpectedToForecastDeviation")
  @DisplayName("Forecast deviation is ok")
  void testForecastDeviationOk(final Instant dateInFrom,
                               final Instant dateInTo,
                               final Instant dateOutFrom,
                               final Instant dateOutTo,
                               final Instant viewDate,
                               final Filter filter,
                               final Map<Instant, Integer> mockSalesDistribution,
                               final Map<Instant, Integer> mockRealSales,
                               final ForecastDeviationQuantity expectedForecastQuantityTotal,
                               final Map<Instant, ForecastDeviationQuantity> expectedForecastQuantityDetails) {
    //GIVEN
    when(salesDistributionPlanGateway.getSalesDistributionPlanned(LOGISTIC_CENTER_ID,
                                                                  FBM_WMS_OUTBOUND,
                                                                  filter,
                                                                  dateInFrom,
                                                                  dateInTo,
                                                                  dateOutFrom,
                                                                  dateOutTo,
                                                                  viewDate)).thenReturn(mockSalesDistribution);

    when(realSalesGateway.getRealSales(LOGISTIC_CENTER_ID,
                                       FBM_WMS_OUTBOUND,
                                       filter,
                                       dateInFrom,
                                       dateInTo,
                                       dateOutFrom,
                                       dateOutTo,
                                       DATE_THREE)).thenReturn(mockRealSales);

    //WHEN
    final ForecastDeviationData forecastDeviationData = getForecastDeviationUseCase.execute(LOGISTIC_CENTER_ID,
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
                   format("Date: %s, Planned is not equal", date));
      assertEquals(expectedForecastQuantityDetails.get(date).getReal(),
                   forecastDeviationQuantity.getReal(),
                   format("Date: %s, Real is not equal", date));
      assertEquals(expectedForecastQuantityDetails.get(date).getDeviation(),
                   forecastDeviationQuantity.getDeviation(),
                   format("Date: %s, Deviation is not equal", date));
      assertEquals(expectedForecastQuantityDetails.get(date).getDeviationPercentage(),
                   forecastDeviationQuantity.getDeviationPercentage(),
                   format("Date: %s, Deviation percentage is not equal", date));
    });

  }
}
