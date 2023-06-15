package com.mercadolibre.flow.control.tool.feature.deferral;

import static com.mercadolibre.flow.control.tool.util.DateUtils.validateDateRange;

import com.mercadolibre.flow.control.tool.feature.deferral.dto.MaximumCapacityDataDto;
import com.mercadolibre.flow.control.tool.feature.deferral.dto.MaximumCapacityResponse;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import com.newrelic.api.agent.Trace;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/control_tool/logistic_center/{logisticCenterId}/staffing/throughput")
public class DeferralController {

  private static final long MAX_CAPACITY_VALUE = 1000L;
  private static final Instant DATE_FROM_MOCK = Instant.parse("2023-03-24T13:00:00Z");


  @Trace
  @GetMapping("max_capacity/plan")
  public ResponseEntity<MaximumCapacityResponse> getMaximumCapacity(
      @PathVariable final String logisticCenterId,
      @RequestParam final Workflow workflow,
      @RequestParam(name = "date_from") final Instant dateFrom,
      @RequestParam(name = "date_to") final Instant dateTo) {
    validateDateRange(dateFrom, dateTo);

    return ResponseEntity.ok(
        new MaximumCapacityResponse(logisticCenterId,
            List.of(
                new MaximumCapacityDataDto(dateFrom, MAX_CAPACITY_VALUE),
                new MaximumCapacityDataDto(DATE_FROM_MOCK, MAX_CAPACITY_VALUE))));
  }

}
