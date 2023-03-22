package com.mercadolibre.flow.control.tool.client.backlog;

import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoSteps.GROUPED;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoSteps.GROUPING;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoSteps.PACKED;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoSteps.PENDING;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoSteps.PICKED;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoSteps.SORTED;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoSteps.TO_DISPATCH;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoSteps.TO_GROUP;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoSteps.TO_OUT;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoSteps.TO_PACK;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoSteps.TO_PICK;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoSteps.TO_ROUTE;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoSteps.TO_SORT;
import static com.mercadolibre.flow.control.tool.feature.status.usecase.constant.Processes.BATCH_SORTER;
import static com.mercadolibre.flow.control.tool.feature.status.usecase.constant.Processes.HU_ASSEMBLY;
import static com.mercadolibre.flow.control.tool.feature.status.usecase.constant.Processes.PACKING;
import static com.mercadolibre.flow.control.tool.feature.status.usecase.constant.Processes.PACKING_WALL;
import static com.mercadolibre.flow.control.tool.feature.status.usecase.constant.Processes.PICKING;
import static com.mercadolibre.flow.control.tool.feature.status.usecase.constant.Processes.SHIPPED;
import static com.mercadolibre.flow.control.tool.feature.status.usecase.constant.Processes.WALL_IN;
import static com.mercadolibre.flow.control.tool.feature.status.usecase.constant.Processes.WAVING;

import com.mercadolibre.flow.control.tool.client.backlog.dto.LastPhotoRequest;
import com.mercadolibre.flow.control.tool.client.backlog.dto.PhotoResponse;
import com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoGrouper;
import com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoSteps;
import com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoWorkflows;
import com.mercadolibre.flow.control.tool.client.backlog.dto.constant.ProcessPath;
import com.mercadolibre.flow.control.tool.client.backlog.dto.constant.ProcessToStep;
import com.mercadolibre.flow.control.tool.feature.status.usecase.BacklogStatusUseCase.BacklogGateway;
import com.mercadolibre.flow.control.tool.feature.status.usecase.constant.Processes;
import com.mercadolibre.flow.control.tool.feature.status.usecase.constant.Workflow;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BacklogAdapter implements BacklogGateway {

  public static final String PATH = "path";
  public static final String STEP = "step";
  private final BacklogApiClient backlogApiClient;

  private static Map<Processes, Map<PhotoSteps, List<ProcessPath>>> stepAndPathToProcess() {
    return
        Map.of(
            WAVING, Map.of(
                PENDING, ProcessPath.allPaths(),
                TO_ROUTE, ProcessPath.allPaths()
            ),
            PICKING, Map.of(
                TO_PICK, ProcessPath.allPaths(),
                PhotoSteps.PICKING, ProcessPath.allPaths()
            ),
            BATCH_SORTER, Map.of(
                PICKED, ProcessPath.multiBatchPaths(),
                TO_SORT, ProcessPath.allPaths()
            ),
            WALL_IN, Map.of(
                SORTED, ProcessPath.allPaths(),
                TO_GROUP, ProcessPath.allPaths(),
                GROUPING, ProcessPath.allPaths(),
                GROUPED, ProcessPath.allPaths()
            ),
            PACKING, Map.of(
                PICKED, ProcessPath.pathsMinusMultiBatch(),
                TO_PACK, ProcessPath.pathsMinusMultiBatch()
            ),
            PACKING_WALL, Map.of(TO_PACK, ProcessPath.multiBatchPaths()
            ),
            HU_ASSEMBLY, Map.of(PACKED, ProcessPath.allPaths(),
                PhotoSteps.PACKING, ProcessPath.allPaths()
            ),
            SHIPPED, Map.of(
                TO_DISPATCH, ProcessPath.allPaths(),
                TO_OUT, ProcessPath.allPaths())
        );
  }

  @Override
  public Map<Processes, Integer> getBacklogTotalsByProcess(
      final String logisticCenterId,
      final Workflow workflow,
      final Set<Processes> processes,
      final Instant viewDate
  ) {
    final Set<PhotoSteps> steps = processes.stream()
        .map(process -> ProcessToStep.from(process.getName()).getBacklogPhotoSteps())
        .flatMap(List::stream)
        .collect(Collectors.toSet());
    final LastPhotoRequest backlogPhotosLastRequest = new LastPhotoRequest(
        logisticCenterId,
        Set.of(PhotoWorkflows.from(workflow)),
        Set.of(PhotoGrouper.STEP, PhotoGrouper.PATH),
        steps,
        viewDate
    );
    final List<PhotoResponse.Group> groups = backlogApiClient.getLastPhoto(backlogPhotosLastRequest).groups();

    var unitsByProcess = groups.stream()
        .filter(group -> ProcessPath.of(group.key().get(PATH)).isPresent() && PhotoSteps.of(group.key().get(STEP)).isPresent())
        .collect(Collectors.toMap(
        group -> mapGroupKeyToProcess(ProcessPath.from(group.key().get(PATH)), PhotoSteps.from(group.key().get(STEP))),
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

  public Optional<Processes> mapGroupKeyToProcess(final ProcessPath path, final PhotoSteps steps) {
    return stepAndPathToProcess().entrySet().stream()
        .filter(processesMapEntry -> processesMapEntry.getValue().containsKey(steps)
            && processesMapEntry.getValue().get(steps).contains(path))
        .map(Map.Entry::getKey)
        .findFirst();
  }
}
