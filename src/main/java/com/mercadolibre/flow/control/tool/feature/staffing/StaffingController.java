package com.mercadolibre.flow.control.tool.feature.staffing;

import com.mercadolibre.flow.control.tool.feature.editor.WorkflowEditor;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import com.mercadolibre.flow.control.tool.feature.staffing.domain.StaffingOperation;
import com.mercadolibre.flow.control.tool.feature.staffing.domain.StaffingOperationValues;
import com.mercadolibre.flow.control.tool.feature.staffing.dto.StaffingOperationDto;
import java.time.Instant;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/control_tool/logistic_center/{logisticCenterId}/plan/staffing")
public class StaffingController {

  private StaffingPlanUseCase staffingPlanUseCase;

  @GetMapping
  public StaffingOperationDto getStaffingOperation(@PathVariable final String logisticCenterId,
                                                    @RequestParam final Workflow workflow,
                                                    @RequestParam(name = "date_from") final Instant dateFrom,
                                                    @RequestParam(name = "date_to") final Instant dateTo) {

    final StaffingOperation staffingOperation = staffingPlanUseCase.getStaffing(logisticCenterId, workflow, dateFrom, dateTo);
    return new StaffingOperationDto(
        staffingOperation.lastModifiedDate(),
        staffingOperation.values().entrySet()
            .stream()
            .collect(
                Collectors.toMap(
                    k -> k.getKey().getName(),
                    v -> convertStaffingOperationToDtoAndGroupedByProcess(v.getValue())
                )
            )
    );
  }

  private Map<String, StaffingOperationDto.StaffingOperationValuesDto> convertStaffingOperationToDtoAndGroupedByProcess(
      final Map<ProcessName, StaffingOperationValues> staffingOperationValuesByProcess
  ) {
    return staffingOperationValuesByProcess.entrySet().stream()
        .collect(
            Collectors.toMap(
                k -> k.getKey().getName(),
                v -> {
                  final var staffingOperationTotal = v.getValue().staffingOperationTotal();
                  return new StaffingOperationDto.StaffingOperationValuesDto(
                      new StaffingOperationDto.StaffingOperationDataDto(
                          staffingOperationTotal.getDate(),
                          staffingOperationTotal.getPlanned(),
                          staffingOperationTotal.getPlannedSystemic(),
                          staffingOperationTotal.getPlannedEdited(),
                          staffingOperationTotal.getPlannedSystemicEdited(),
                          staffingOperationTotal.getPlannedNonSystemic(),
                          staffingOperationTotal.getPlannedNonSystemicEdited()
                      ),
                      v.getValue().staffingOperationValues().stream()
                          .map(staffingOperationData -> new StaffingOperationDto.StaffingOperationDataDto(
                              staffingOperationData.getDate(),
                              staffingOperationData.getPlanned(),
                              staffingOperationData.getPlannedSystemic(),
                              staffingOperationData.getPlannedEdited(),
                              staffingOperationData.getPlannedSystemicEdited(),
                              staffingOperationData.getPlannedNonSystemic(),
                              staffingOperationData.getPlannedNonSystemicEdited())
                          )
                          .sorted(Comparator.comparing(StaffingOperationDto.StaffingOperationDataDto::date))
                          .toList()
                  );
                }
            )
        );
  }

  @InitBinder
  public void initBinder(final PropertyEditorRegistry dataBinder) {
    dataBinder.registerCustomEditor(Workflow.class, new WorkflowEditor());
  }

}
