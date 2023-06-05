package com.mercadolibre.flow.control.tool.feature.forecastdeviation;

import static com.mercadolibre.flow.control.tool.feature.forecastdeviation.constant.Filter.DATE_IN;
import static com.mercadolibre.flow.control.tool.feature.forecastdeviation.constant.Filter.DATE_OUT;
import static java.lang.Math.round;
import static java.lang.Math.toIntExact;
import static java.time.temporal.ChronoUnit.HOURS;

import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import com.mercadolibre.flow.control.tool.feature.forecastdeviation.constant.Filter;
import com.mercadolibre.flow.control.tool.feature.forecastdeviation.domain.ForecastDeviationData;
import com.mercadolibre.flow.control.tool.feature.forecastdeviation.domain.ForecastDeviationQuantity;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ForecastDeviationUseCase {

  private static final int DAYS_TO_SEARCH = 7;

  private static final Map<Filter, BiFunction<Instant, Instant, DateFilter>> BUILD_DATE_BY_FILTER = Map.of(
      DATE_IN, ForecastDeviationUseCase::buildDateInFilter,
      DATE_OUT, ForecastDeviationUseCase::buildDateOutFilter
  );

  private final SalesDistributionPlanGateway salesDistributionPlanGateway;

  private final RealSalesGateway realSalesGateway;

  public ForecastDeviationData getForecastDeviation(final String logisticCenterId,
                                                    final Workflow workflow,
                                                    final Instant dateFrom,
                                                    final Instant dateTo,
                                                    final Instant viewDate,
                                                    final Filter filter) {

    final DateFilter dateFilter = BUILD_DATE_BY_FILTER.get(filter).apply(dateFrom, dateTo);

    final Map<Instant, Double> salesDistributionPlanned = salesDistributionPlanGateway.getSalesDistributionPlanned(
        logisticCenterId,
        workflow,
        filter,
        dateFilter.dateInFrom,
        dateFilter.dateInTo,
        dateFilter.dateOutFrom,
        dateFilter.dateOutTo
    );

    final Map<Instant, Integer> realSales = realSalesGateway.getRealSales(logisticCenterId,
                                                                       workflow,
                                                                       filter,
                                                                       dateFilter.dateInFrom,
                                                                       dateFilter.dateInTo,
                                                                       dateFilter.dateOutFrom,
                                                                       dateFilter.dateOutTo,
                                                                       dateTo);

    final Map<Instant, ForecastDeviationQuantity> deviationDetailByDate = new ConcurrentHashMap<>();

    IntStream.range(0, toIntExact(HOURS.between(dateFrom, dateTo)) + 1)
        .forEach(hour -> {
          final Instant hourInstant = dateFrom.plus(hour, HOURS);
          final ForecastDeviationQuantity deviationDetail = buildForecastDeviationDetail(
              salesDistributionPlanned.getOrDefault(hourInstant, 0D),
              realSales.getOrDefault(hourInstant, 0),
              !hourInstant.isAfter(viewDate)
          );
          deviationDetailByDate.put(hourInstant, deviationDetail);
        });

    final int totalPlanned = !dateFrom.isAfter(viewDate)
        ? deviationDetailByDate.entrySet().stream()
        .filter(entry -> !entry.getKey().isAfter(viewDate))
        .mapToInt(entry -> entry.getValue().getPlanned())
        .sum()
        : deviationDetailByDate.values().stream()
        .mapToInt(ForecastDeviationQuantity::getPlanned)
        .sum();

    final int totalReal = deviationDetailByDate.entrySet().stream()
        .filter(entry -> !entry.getKey().isAfter(viewDate) && entry.getValue().getReal() != null)
        .mapToInt(entry -> entry.getValue().getReal())
        .sum();

    final int deviation = totalReal - totalPlanned;

    final double deviationPercentage = totalPlanned == 0 ? 0 : (double) deviation / (double) totalPlanned;

    final ForecastDeviationQuantity totalForecastDeviationQuantity = !dateFrom.isAfter(viewDate)
        ? ForecastDeviationQuantity.builder()
        .planned(totalPlanned)
        .real(totalReal)
        .deviation(deviation)
        .deviationPercentage(deviationPercentage)
        .build()
        : ForecastDeviationQuantity.builder()
        .planned(totalPlanned)
        .build();

    return new ForecastDeviationData(totalForecastDeviationQuantity, deviationDetailByDate);

  }

  private ForecastDeviationQuantity buildForecastDeviationDetail(final double planned,
                                                                 final int real,
                                                                 final boolean isNecessaryToFillRealInfo) {
    final int plannedRounded = toIntExact(round(planned));
    final int deviation = real - plannedRounded;
    final double deviationPercentage = planned == 0 ? 0 : deviation / planned;

    return isNecessaryToFillRealInfo
        ? ForecastDeviationQuantity.builder()
        .planned(plannedRounded)
        .real(real)
        .deviation(deviation)
        .deviationPercentage(deviationPercentage)
        .build()
        : ForecastDeviationQuantity.builder().planned(plannedRounded).build();
  }

  private static DateFilter buildDateInFilter(final Instant dateFrom, final Instant dateTo) {
    return new DateFilter(dateFrom, dateTo, dateFrom, dateTo.plus(DAYS_TO_SEARCH, HOURS));
  }

  private static DateFilter buildDateOutFilter(final Instant dateFrom, final Instant dateTo) {
    return new DateFilter(dateFrom.minus(DAYS_TO_SEARCH, HOURS), dateTo, dateFrom, dateTo);
  }

  private record DateFilter(Instant dateInFrom, Instant dateInTo, Instant dateOutFrom, Instant dateOutTo) {
  }

  /**
   * Gateway to get sales distribution planned.
   */
  public interface SalesDistributionPlanGateway {

    /**
     * Get sales distribution planned.
     *
     * @param logisticCenterId logistic center id
     * @param workflow workflow
     * @param groupBy group by
     * @param dateInFrom date in from
     * @param dateInTo date in to
     * @param dateOutFrom date out from
     * @param dateOutTo date out to
     * @return sales distribution planned
     */
    Map<Instant, Double> getSalesDistributionPlanned(
        String logisticCenterId,
        Workflow workflow,
        Filter groupBy,
        Instant dateInFrom,
        Instant dateInTo,
        Instant dateOutFrom,
        Instant dateOutTo
    );

  }

  /**
   * Gateway to get real sales.
   */
  public interface RealSalesGateway {
    /**
     * Get real sales.
     *
     * @param logisticCenterId logistic center id
     * @param workflow workflow
     * @param groupBy group by
     * @param dateInFrom date in from
     * @param dateInTo date in to
     * @param dateOutFrom date out from
     * @param dateOutTo date out to
     * @param dateTo date to
     * @return real sales
     */
    Map<Instant, Integer> getRealSales(
        String logisticCenterId,
        Workflow workflow,
        Filter groupBy,
        Instant dateInFrom,
        Instant dateInTo,
        Instant dateOutFrom,
        Instant dateOutTo,
        Instant dateTo
    );

  }

}
