package com.mercadolibre.flow.control.tool.client.backlog.adapter;

import static com.mercadolibre.flow.control.tool.client.backlog.adapter.StepAndPathToProcessMapper.pathAndStepToProcessName;
import static com.mercadolibre.flow.control.tool.client.backlog.adapter.Util.filterExistingProcessPathAndSteps;
import static com.mercadolibre.flow.control.tool.client.backlog.adapter.Util.toSteps;

import com.mercadolibre.flow.control.tool.client.backlog.BacklogApiClient;
import com.mercadolibre.flow.control.tool.client.backlog.dto.LastPhotoRequest;
import com.mercadolibre.flow.control.tool.client.backlog.dto.PhotoRequest;
import com.mercadolibre.flow.control.tool.client.backlog.dto.PhotoResponse;
import com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoGrouper;
import com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep;
import com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoWorkflow;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.GetHistoricalBacklogUseCase;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName;
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
  public Map<Instant, Map<ProcessName, Map<Instant, Map<ProcessPathName, Integer>>>> getBacklogByDateProcessAndPP(
      final Workflow workflow,
      final String logisticCenterId,
      final Set<ProcessName> processes,
      final Instant dateFrom,
      final Instant dateTo) {

    final PhotoRequest request = new PhotoRequest(
        logisticCenterId,
        Set.of(PhotoWorkflow.from(workflow)),
        Set.of(PhotoGrouper.STEP, PhotoGrouper.DATE_OUT, PhotoGrouper.PATH),
        toSteps(processes),
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
                    group -> pathAndStepToProcessName(ProcessPathName.from(group.key().get(PATH)),
                        PhotoStep.from(group.key().get(STEP))).orElseThrow(),
                    Collectors.groupingBy(
                        entry -> Instant.parse(entry.key().get(DATE_OUT)),
                        Collectors.toMap(
                            entry -> ProcessPathName.from(entry.key().get(PATH)),
                            PhotoResponse.Group::total,
                            Integer::sum
                        )
                    )
                ))
        ));
  }

  @Override
  public Map<Instant, Map<ProcessName, Map<Instant, Map<ProcessPathName, Integer>>>> getLastBacklogByDateProcessAndPP(
      final Workflow workflow,
      final String logisticCenter,
      final Set<ProcessName> processes,
      final Instant viewDate) {

    final LastPhotoRequest backlogPhotosLastRequest =
        LastPhotoRequest.builder()
            .logisticCenterId(logisticCenter)
            .workflows(Set.of(PhotoWorkflow.from(workflow)))
            .groupBy(Set.of(PhotoGrouper.STEP, PhotoGrouper.PATH, PhotoGrouper.DATE_OUT))
            .steps(toSteps(processes))
            .photoDateTo(viewDate)
            .build();

    final PhotoResponse lastPhoto = backlogApiClient.getLastPhoto(backlogPhotosLastRequest);

    if (lastPhoto == null) {
      return Map.of();
    }

    return Map.of(
        lastPhoto.takenOn(),
        filterExistingProcessPathAndSteps(lastPhoto.groups())
            .map(group -> new Group(
                    pathAndStepToProcessName(ProcessPathName.from(group.key().get(PATH)),
                        PhotoStep.from(group.key().get(STEP))).orElseThrow(),
                    ProcessPathName.from(group.key().get(PATH)),
                    Instant.parse(group.key().get(DATE_OUT)),
                    group.total()
                )
            )
            .collect(
                Collectors.groupingBy(Group::processName,
                    Collectors.groupingBy(Group::dateOut,
                        Collectors.groupingBy(Group::path,
                            Collectors.summingInt(Group::total))))
            ));
  }

  private record Group(
      ProcessName processName,
      ProcessPathName path,
      Instant dateOut,
      Integer total) {
  }

}
