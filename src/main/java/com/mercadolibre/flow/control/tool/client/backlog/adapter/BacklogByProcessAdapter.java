package com.mercadolibre.flow.control.tool.client.backlog.adapter;

import static com.mercadolibre.flow.control.tool.client.backlog.adapter.StepAndPathToProcessMapper.pathAndStepToProcessName;
import static com.mercadolibre.flow.control.tool.client.backlog.adapter.Util.filterExistingProcessPathAndSteps;
import static com.mercadolibre.flow.control.tool.client.backlog.adapter.Util.toSteps;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoGrouper.PATH;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoGrouper.STEP;

import com.mercadolibre.flow.control.tool.client.backlog.BacklogApiClient;
import com.mercadolibre.flow.control.tool.client.backlog.dto.LastPhotoRequest;
import com.mercadolibre.flow.control.tool.client.backlog.dto.PhotoResponse;
import com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep;
import com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoWorkflow;
import com.mercadolibre.flow.control.tool.feature.backlog.status.BacklogStatusUseCase;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BacklogByProcessAdapter implements BacklogStatusUseCase.BacklogGateway {

  private final BacklogApiClient backlogApiClient;

  @Override
  public Map<ProcessName, Integer> getBacklogTotalsByProcess(
      final String logisticCenterId,
      final Workflow workflow,
      final Set<ProcessName> processes,
      final Instant viewDate
  ) {
    final LastPhotoRequest backlogPhotosLastRequest = LastPhotoRequest.builder()
        .logisticCenterId(logisticCenterId)
        .workflows(Set.of(PhotoWorkflow.from(workflow)))
        .groupBy(Set.of(STEP, PATH))
        .steps(toSteps(processes))
        .photoDateTo(viewDate)
        .build();

    final PhotoResponse groups = backlogApiClient.getLastPhoto(backlogPhotosLastRequest);

    if (groups == null) {
      return Map.of();
    }

    final var unitsByProcess = filterExistingProcessPathAndSteps(groups.groups())
        .collect(Collectors.toMap(
            group -> pathAndStepToProcessName(ProcessPathName.from(group.key().get(PATH.getName())),
                PhotoStep.from(group.key().get(STEP.getName()))),
            PhotoResponse.Group::total,
            Integer::sum
        ));

    return unitsByProcess.entrySet().stream()
        .filter(optionalEntry -> optionalEntry.getKey().isPresent())
        .collect(Collectors.toMap(
            entry -> entry.getKey().get(),
            Map.Entry::getValue
        ));
  }
}
