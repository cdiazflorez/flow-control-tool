package com.mercadolibre.flow.control.tool.client.backlog.adapter;

import static com.mercadolibre.flow.control.tool.client.backlog.adapter.StepAndPathToProcessMapper.pathAndStepToProcessName;
import static com.mercadolibre.flow.control.tool.client.backlog.adapter.Util.toSteps;

import com.mercadolibre.flow.control.tool.client.backlog.BacklogApiClient;
import com.mercadolibre.flow.control.tool.client.backlog.dto.LastPhotoRequest;
import com.mercadolibre.flow.control.tool.client.backlog.dto.PhotoResponse;
import com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoGrouper;
import com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep;
import com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoWorkflow;
import com.mercadolibre.flow.control.tool.feature.backlog.genericgateway.BacklogGateway;
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
public class BacklogByProcessAdapter implements BacklogGateway {

  public static final String PATH = "path";
  public static final String STEP = "step";
  private final BacklogApiClient backlogApiClient;

  @Override
  public Map<ProcessName, Integer> getBacklogTotalsByProcess(
      final String logisticCenterId,
      final Workflow workflow,
      final Set<ProcessName> processes,
      final Instant viewDate
  ) {
    final LastPhotoRequest backlogPhotosLastRequest = new LastPhotoRequest(
        logisticCenterId,
        Set.of(PhotoWorkflow.from(workflow)),
        Set.of(PhotoGrouper.STEP, PhotoGrouper.PATH),
        toSteps(processes),
        viewDate
    );
    final PhotoResponse groups = backlogApiClient.getLastPhoto(backlogPhotosLastRequest);

    if (groups == null) {
      return Map.of();
    }

    final var unitsByProcess = groups.groups().stream()
        .filter(group -> ProcessPathName.of(group.key().get(PATH)).isPresent() && PhotoStep.of(group.key().get(STEP)).isPresent())
        .collect(Collectors.toMap(
            group -> pathAndStepToProcessName(ProcessPathName.from(group.key().get(PATH)), PhotoStep.from(group.key().get(STEP))),
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
