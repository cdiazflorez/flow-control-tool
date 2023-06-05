package com.mercadolibre.flow.control.tool.feature.forecastdeviation;

import static com.mercadolibre.flow.control.tool.util.DateUtils.validateDateRange;

import com.mercadolibre.flow.control.tool.feature.editor.FilterEditor;
import com.mercadolibre.flow.control.tool.feature.editor.WorkflowEditor;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import com.mercadolibre.flow.control.tool.feature.forecastdeviation.constant.Filter;
import com.mercadolibre.flow.control.tool.feature.forecastdeviation.dto.ForecastDeviation;
import com.newrelic.api.agent.Trace;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/control_tool/logistic_center/{logisticCenter}/forecast_deviation")
public class ForecastDeviationController {


  @Trace
  @GetMapping
  public ResponseEntity<ForecastDeviation> getForecastDeviation(@PathVariable final String logisticCenter,
                                                                @RequestParam final Workflow workflow,
                                                                @RequestParam(name = "filter_by") final Filter filterBy,
                                                                @RequestParam(name = "date_from") final Instant dateFrom,
                                                                @RequestParam(name = "date_to") final Instant dateTo,
                                                                @RequestParam(name = "view_date") final Instant viewDate) {

    validateDateRange(dateFrom, dateTo);

    final ForecastDeviation response = mockForecastDeviation(dateFrom);

    return ResponseEntity.ok(response);


  }

  private ForecastDeviation mockForecastDeviation(final Instant date) {

    return new ForecastDeviation(
        260,
        420,
        40,
        1.016,
        List.of(
            new ForecastDeviation.DeviationDetail(date, 100, 60, 40, 0.4),
            new ForecastDeviation.DeviationDetail(date.plus(1, ChronoUnit.HOURS), 150, 60, 90, 0.6),
            new ForecastDeviation.DeviationDetail(date.plus(2, ChronoUnit.HOURS), 50, 70, -20, -0.4),
            new ForecastDeviation.DeviationDetail(date.plus(3, ChronoUnit.HOURS), 120, 70, 50, 0.416)
        )
    );
  }

  @InitBinder
  public void initBinder(final PropertyEditorRegistry dataBinder) {
    dataBinder.registerCustomEditor(Workflow.class, new WorkflowEditor());
    dataBinder.registerCustomEditor(Filter.class, new FilterEditor());
  }
}
