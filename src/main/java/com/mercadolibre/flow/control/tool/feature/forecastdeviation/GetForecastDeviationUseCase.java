package com.mercadolibre.flow.control.tool.feature.forecastdeviation;

import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MINUTES;

import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import com.mercadolibre.flow.control.tool.feature.forecastdeviation.constant.Filter;
import com.mercadolibre.flow.control.tool.feature.forecastdeviation.domain.ForecastDeviationData;
import com.mercadolibre.flow.control.tool.feature.forecastdeviation.domain.ForecastDeviationQuantity;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetForecastDeviationUseCase {

  private static final int MINUTES_IN_AN_HOUR = 60;

  private final SalesDistributionPlanGateway salesDistributionPlanGateway;

  private final RealSalesGateway realSalesGateway;

  private static Map<Instant, ForecastDeviationQuantity> buildDeviationQuantityByDate(final Instant dateFrom,
                                                                                      final Instant dateTo,
                                                                                      final Instant viewDate,
                                                                                      final Map<Instant, Integer> plannedUnits,
                                                                                      final Map<Instant, Integer> realUnits) {

    return LongStream.rangeClosed(0, HOURS.between(dateFrom, dateTo))
        .mapToObj(hour -> dateFrom.plus(hour, HOURS))
        .collect(
            Collectors.toMap(
                Function.identity(),
                hourInstant -> buildDeviationQuantity(
                    calculatePlannedQuantity(hourInstant, viewDate, plannedUnits.getOrDefault(hourInstant, 0)),
                    realUnits.getOrDefault(hourInstant, 0),
                    !hourInstant.isAfter(viewDate)
                )
            )
        );
  }

  private static ForecastDeviationQuantity buildDeviationQuantity(final int planned,
                                                                  final int real,
                                                                  final boolean isNecessaryToFillRealInfo) {
    final int deviation = real - planned;
    final double deviationPercentage = calculateDeviationPercentage(planned, deviation);

    return isNecessaryToFillRealInfo
        ? new ForecastDeviationQuantity(planned, real, deviation, deviationPercentage)
        : new ForecastDeviationQuantity(planned);
  }

  private static int calculatePlannedQuantity(final Instant date,
                                              final Instant viewDate,
                                              final int plannedQuantity) {
    if (!date.isAfter(viewDate)) {
      long minutes = MINUTES.between(date, viewDate);
      if (minutes < MINUTES_IN_AN_HOUR) {
        final double ratio = (double) minutes / MINUTES_IN_AN_HOUR;
        return Math.toIntExact(Math.round(plannedQuantity * ratio));
      }
    }
    return plannedQuantity;
  }

  private static double calculateDeviationPercentage(final int plannedQuantity,
                                                     final int deviationQuantity) {
    if (plannedQuantity == 0) {
      return deviationQuantity == 0 ? 0 : 1d;
    }
    return deviationQuantity / (double) plannedQuantity;
  }

  public ForecastDeviationData execute(final String logisticCenterId,
                                       final Workflow workflow,
                                       final Instant dateFrom,
                                       final Instant dateTo,
                                       final Instant viewDate,
                                       final Filter filter) {

    final Instant dateFromTruncated = dateFrom.truncatedTo(HOURS);
    final Instant dateToTruncated = dateTo.truncatedTo(HOURS);

    final Filter.DateFilter dateFilter = filter.dateFilterFunction.apply(dateFromTruncated, dateToTruncated);

    final Map<Instant, Integer> plannedUnits = salesDistributionPlanGateway.getSalesDistributionPlanned(
        logisticCenterId,
        workflow,
        filter,
        dateFilter.dateInFrom(),
        dateFilter.dateInTo(),
        dateFilter.dateOutFrom(),
        dateFilter.dateOutTo(),
        viewDate
    );

    final Map<Instant, Integer> realUnits = realSalesGateway.getRealSales(logisticCenterId,
                                                                          workflow,
                                                                          filter,
                                                                          dateFilter.dateInFrom(),
                                                                          dateFilter.dateInTo(),
                                                                          dateFilter.dateOutFrom(),
                                                                          dateFilter.dateOutTo(),
                                                                          dateTo);

    final Map<Instant, ForecastDeviationQuantity> deviationDetailByDate = buildDeviationQuantityByDate(dateFromTruncated,
                                                                                                       dateToTruncated,
                                                                                                       viewDate,
                                                                                                       plannedUnits,
                                                                                                       realUnits);

    return new ForecastDeviationData(
        buildDeviationTotalQuantity(deviationDetailByDate, dateFromTruncated, viewDate),
        deviationDetailByDate
    );

  }


  private ForecastDeviationQuantity buildDeviationTotalQuantity(final Map<Instant, ForecastDeviationQuantity> deviationDetailByDate,
                                                                final Instant dateFrom,
                                                                final Instant viewDate) {


    final int totalPlanned = dateFrom.isAfter(viewDate)
        ? deviationDetailByDate.values().stream()
        .mapToInt(ForecastDeviationQuantity::getPlanned)
        .sum()
        : deviationDetailByDate.keySet()
        .stream()
        .filter(date -> !date.isAfter(viewDate))
        .map(deviationDetailByDate::get)
        .filter(Objects::nonNull)
        .mapToInt(ForecastDeviationQuantity::getPlanned)
        .sum();

    final int totalReal = deviationDetailByDate.keySet()
        .stream()
        .filter(date -> !date.isAfter(viewDate))
        .map(deviationDetailByDate::get)
        .filter(Objects::nonNull)
        .mapToInt(ForecastDeviationQuantity::getReal)
        .sum();

    final int deviation = totalReal - totalPlanned;

    final double deviationPercentage = calculateDeviationPercentage(totalPlanned, deviation);

    return !dateFrom.isAfter(viewDate)
        ? new ForecastDeviationQuantity(totalPlanned, totalReal, deviation, deviationPercentage)
        : new ForecastDeviationQuantity(totalPlanned);
  }

  /**
   * Gateway to get sales distribution planned.
   */
  public interface SalesDistributionPlanGateway {

    /**
     * Get sales distribution planned.
     *
     * @param logisticCenterId logistic center id
     * @param workflow         workflow
     * @param groupBy          group by
     * @param dateInFrom       date in from
     * @param dateInTo         date in to
     * @param dateOutFrom      date out from
     * @param dateOutTo        date out to
     * @param viewDate         view date
     * @return sales distribution planned
     */
    Map<Instant, Integer> getSalesDistributionPlanned(
        String logisticCenterId,
        Workflow workflow,
        Filter groupBy,
        Instant dateInFrom,
        Instant dateInTo,
        Instant dateOutFrom,
        Instant dateOutTo,
        Instant viewDate
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
     * @param workflow         workflow
     * @param groupBy          group by
     * @param dateInFrom       date in from
     * @param dateInTo         date in to
     * @param dateOutFrom      date out from
     * @param dateOutTo        date out to
     * @param dateTo           date to
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
