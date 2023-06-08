package com.mercadolibre.flow.control.tool.client.backlog.adapter;

import static com.mercadolibre.flow.control.tool.client.backlog.adapter.StepAndPathToProcessMapper.pathAndStepToProcessName;
import static com.mercadolibre.flow.control.tool.client.backlog.adapter.Util.filterExistingProcessPathAndSteps;
import static com.mercadolibre.flow.control.tool.client.backlog.adapter.Util.toSteps;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoGrouper.DATE_OUT;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoGrouper.PATH;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoGrouper.STEP;

import com.mercadolibre.fbm.wms.outbound.commons.rest.exception.ClientException;
import com.mercadolibre.flow.control.tool.client.backlog.BacklogApiClient;
import com.mercadolibre.flow.control.tool.client.backlog.dto.LastPhotoRequest;
import com.mercadolibre.flow.control.tool.client.backlog.dto.PhotoResponse;
import com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep;
import com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoWorkflow;
import com.mercadolibre.flow.control.tool.exception.ProjectionInputsNotFoundException;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.BacklogProjectedGateway;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BacklogProjectedAdapter implements BacklogProjectedGateway {

  private static final String INPUT_TYPE = "Real backlog";
  private final BacklogApiClient backlogApiClient;

  @Override
  public Map<ProcessName, Map<ProcessPathName, Map<Instant, Integer>>> getBacklogTotalsByProcessAndPPandSla(
      final String logisticCenterId, final Workflow workflow, final Set<ProcessName> processes, final Instant viewDate) {

    final LastPhotoRequest backlogPhotosLastRequest = LastPhotoRequest.builder()
        .logisticCenterId(logisticCenterId)
        .workflows(Set.of(PhotoWorkflow.from(workflow)))
        .groupBy(Set.of(STEP, PATH, DATE_OUT))
        .steps(toSteps(processes))
        .photoDateTo(viewDate)
        .build();

    try {
      final PhotoResponse lastPhoto = backlogApiClient.getLastPhoto(backlogPhotosLastRequest);

      if (lastPhoto == null) {
        return Collections.emptyMap();
      }

      return filterExistingProcessPathAndSteps(lastPhoto.groups())
          .map(group -> new Group(
                  pathAndStepToProcessName(ProcessPathName.from(group.key().get(PATH.getName())),
                      PhotoStep.from(group.key().get(STEP.getName()))).orElseThrow(),
                  ProcessPathName.from(group.key().get(PATH.getName())),
                  Instant.parse(group.key().get(DATE_OUT.getName())),
                  group.total()
              )
          )
          .collect(Collectors.groupingBy(Group::processName, Collectors.groupingBy(Group::path,
              Collectors.groupingBy(Group::dateOut, Collectors.summingInt(Group::total))))
          );
    } catch (ClientException ce) {
      throw new ProjectionInputsNotFoundException(INPUT_TYPE, logisticCenterId, workflow.getName(), ce);
    }
  }

  private record Group(
      ProcessName processName,
      ProcessPathName path, Instant dateOut,
      Integer total) {
  }
}
