package com.mercadolibre.flow.control.tool.client.backlog.adapter;

import static com.mercadolibre.flow.control.tool.client.backlog.adapter.StepAndPathToProcessMapper.pathAndStepToProcessName;

import com.mercadolibre.flow.control.tool.client.backlog.BacklogApiClient;
import com.mercadolibre.flow.control.tool.client.backlog.dto.PhotoRequest;
import com.mercadolibre.flow.control.tool.client.backlog.dto.PhotoResponse;
import com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoGrouper;
import com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep;
import com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoWorkflow;
import com.mercadolibre.flow.control.tool.client.backlog.dto.constant.ProcessToStep;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.GetHistoricalBacklogUseCase;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessPath;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BacklogHistoricalAdapter implements GetHistoricalBacklogUseCase.BacklogGateway {
  private static final String PATH = "path";
  private static final String STEP = "step";
  private static final String DATE_OUT = "date_out";
  final BacklogApiClient backlogApiClient;

  @Override
  public Map<Instant, Map<ProcessName, Map<Instant, Map<ProcessPath, Integer>>>> getBacklogByDateProcessAndPP(
      final Workflow workflow,
      final String logisticCenterId,
      final Set<ProcessName> processes,
      final Instant dateFrom,
      final Instant dateTo) {

    final Set<PhotoStep> steps = processes.stream()
        .map(process -> ProcessToStep.from(process.getName()).getBacklogPhotoSteps())
        .flatMap(List::stream)
        .collect(Collectors.toSet());

    final PhotoRequest request = new PhotoRequest(
        logisticCenterId,
        Set.of(PhotoWorkflow.from(workflow)),
        Set.of(PhotoGrouper.STEP, PhotoGrouper.DATE_OUT, PhotoGrouper.PATH),
        steps,
        dateFrom,
        dateTo);

    final List<PhotoResponse> responseFromBacklogsAPI = backlogApiClient.getPhotos(request);

    if (responseFromBacklogsAPI == null) {
      return Map.of();
    }

    return responseFromBacklogsAPI.stream()
        .collect(Collectors.toMap(
            PhotoResponse::takenOn,
            photoResponse -> photoResponse.groups().stream()
                .collect(Collectors.groupingBy(
                    group -> pathAndStepToProcessName(ProcessPath.from(group.key().get(PATH)),
                        PhotoStep.from(group.key().get(STEP))).orElseThrow(),
                    Collectors.groupingBy(
                        entry -> Instant.parse(entry.key().get(DATE_OUT)),
                        Collectors.toMap(
                            entry -> ProcessPath.from(entry.key().get(PATH)),
                            PhotoResponse.Group::total,
                            Integer::sum
                        )
                    )
                ))
        ));
  }
}
