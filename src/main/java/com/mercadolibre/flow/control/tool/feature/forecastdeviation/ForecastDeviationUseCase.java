package com.mercadolibre.flow.control.tool.feature.forecastdeviation;

import static java.lang.Math.toIntExact;
import static java.time.temporal.ChronoUnit.HOURS;

import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import com.mercadolibre.flow.control.tool.feature.forecastdeviation.constant.Filter;
import com.mercadolibre.flow.control.tool.feature.forecastdeviation.domain.ForecastDeviationData;
import com.mercadolibre.flow.control.tool.feature.forecastdeviation.domain.ForecastDeviationQuantity;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ForecastDeviationUseCase {

  private final SalesDistributionPlanGateway salesDistributionPlanGateway;

  private final RealSalesGateway realSalesGateway;

  public ForecastDeviationData getForecastDeviation(final String logisticCenterId,
                                                    final Workflow workflow,
                                                    final Instant dateFrom,
                                                    final Instant dateTo,
                                                    final Instant viewDate,
                                                    final Filter filter) {

    final Filter.DateFilter dateFilter = filter.dateFilterFunction.apply(dateFrom, dateTo);

    final Map<Instant, Integer> plannedUnits = salesDistributionPlanGateway.getSalesDistributionPlanned(
        logisticCenterId,
        workflow,
        filter,
        dateFilter.dateInFrom(),
        dateFilter.dateInTo(),
        dateFilter.dateOutFrom(),
        dateFilter.dateOutTo()
    );

    final Map<Instant, Integer> realUnits = realSalesGateway.getRealSales(logisticCenterId,
                                                                          workflow,
                                                                          filter,
                                                                          dateFilter.dateInFrom(),
                                                                          dateFilter.dateInTo(),
                                                                          dateFilter.dateOutFrom(),
                                                                          dateFilter.dateOutTo(),
                                                                          dateTo);

    final Map<Instant, ForecastDeviationQuantity> deviationDetailByDate = buildDeviationQuantityByDate(dateFrom,
                                                                                                       dateTo,
                                                                                                       viewDate,
                                                                                                       plannedUnits,
                                                                                                       realUnits);

    return new ForecastDeviationData(buildDeviationTotalQuantity(deviationDetailByDate, dateFrom, viewDate), deviationDetailByDate);

  }

  private Map<Instant, ForecastDeviationQuantity> buildDeviationQuantityByDate(final Instant dateFrom,
                                                                               final Instant dateTo,
                                                                               final Instant viewDate,
                                                                               final Map<Instant, Integer> plannedUnits,
                                                                               final Map<Instant, Integer> realUnits) {

    return IntStream.rangeClosed(0, toIntExact(HOURS.between(dateFrom, dateTo)))
        .mapToObj(hour -> dateFrom.plus(hour, HOURS))
        .collect(
            Collectors.toMap(
                hourInstant -> hourInstant,
                hourInstant -> buildDeviationQuantity(
                    plannedUnits.getOrDefault(hourInstant, 0),
                    realUnits.getOrDefault(hourInstant, 0),
                    !hourInstant.isAfter(viewDate)
                )
            )
        );
  }

  private ForecastDeviationQuantity buildDeviationQuantity(final int planned,
                                                           final int real,
                                                           final boolean isNecessaryToFillRealInfo) {
    final int deviation = real - planned;
    final double deviationPercentage = planned == 0 ? 1 : deviation / (double) planned;

    return isNecessaryToFillRealInfo
        ? new ForecastDeviationQuantity(planned, real, deviation, deviationPercentage)
        : new ForecastDeviationQuantity(planned);
  }

  private ForecastDeviationQuantity buildDeviationTotalQuantity(final Map<Instant, ForecastDeviationQuantity> deviationDetailByDate,
                                                                final Instant dateFrom,
                                                                final Instant viewDate) {

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

    final double deviationPercentage = totalPlanned == 0 ? 1 : deviation / (double) totalPlanned;

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
     * @return sales distribution planned
     */
    Map<Instant, Integer> getSalesDistributionPlanned(
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
