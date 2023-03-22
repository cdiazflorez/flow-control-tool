package com.mercadolibre.flow.control.tool.client.backlog.adapter;

import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.GROUPED;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.GROUPING;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.PACKED;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.PENDING;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.PICKED;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.SORTED;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.TO_DISPATCH;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.TO_GROUP;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.TO_OUT;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.TO_PACK;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.TO_PICK;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.TO_ROUTE;
import static com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep.TO_SORT;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.BATCH_SORTER;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.HU_ASSEMBLY;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.PACKING;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.PACKING_WALL;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.PICKING;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.SHIPPED;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.WALL_IN;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessName.WAVING;

import com.mercadolibre.flow.control.tool.client.backlog.BacklogApiClient;
import com.mercadolibre.flow.control.tool.client.backlog.dto.LastPhotoRequest;
import com.mercadolibre.flow.control.tool.client.backlog.dto.PhotoResponse;
import com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoGrouper;
import com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoStep;
import com.mercadolibre.flow.control.tool.client.backlog.dto.constant.PhotoWorkflow;
import com.mercadolibre.flow.control.tool.client.backlog.dto.constant.ProcessPath;
import com.mercadolibre.flow.control.tool.client.backlog.dto.constant.ProcessToStep;
import com.mercadolibre.flow.control.tool.feature.backlog.status.BacklogStatusUseCase.BacklogGateway;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.entity.Workflow;
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
public class BacklogByProcessAdapter implements BacklogGateway {

  public static final String PATH = "path";
  public static final String STEP = "step";
  private final BacklogApiClient backlogApiClient;

  private static Map<ProcessName, Map<PhotoStep, List<ProcessPath>>> stepAndPathToProcess() {
    return
        Map.of(
            WAVING, Map.of(
                PENDING, ProcessPath.allPaths(),
                TO_ROUTE, ProcessPath.allPaths()
            ),
            PICKING, Map.of(
                TO_PICK, ProcessPath.allPaths(),
                PhotoStep.PICKING, ProcessPath.allPaths()
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
                PhotoStep.PACKING, ProcessPath.allPaths()
            ),
            SHIPPED, Map.of(
                TO_DISPATCH, ProcessPath.allPaths(),
                TO_OUT, ProcessPath.allPaths())
        );
  }

  @Override
  public Map<ProcessName, Integer> getBacklogTotalsByProcess(
      final String logisticCenterId,
      final Workflow workflow,
      final Set<ProcessName> processes,
      final Instant viewDate
  ) {
    final Set<PhotoStep> steps = processes.stream()
        .map(process -> ProcessToStep.from(process.getName()).getBacklogPhotoSteps())
        .flatMap(List::stream)
        .collect(Collectors.toSet());
    final LastPhotoRequest backlogPhotosLastRequest = new LastPhotoRequest(
        logisticCenterId,
        Set.of(PhotoWorkflow.from(workflow)),
        Set.of(PhotoGrouper.STEP, PhotoGrouper.PATH),
        steps,
        viewDate
    );
    final List<PhotoResponse.Group> groups = backlogApiClient.getLastPhoto(backlogPhotosLastRequest).groups();

    var unitsByProcess = groups.stream()
        .filter(group -> ProcessPath.of(group.key().get(PATH)).isPresent() && PhotoStep.of(group.key().get(STEP)).isPresent())
        .collect(Collectors.toMap(
        group -> mapGroupKeyToProcess(ProcessPath.from(group.key().get(PATH)), PhotoStep.from(group.key().get(STEP))),
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

  public Optional<ProcessName> mapGroupKeyToProcess(final ProcessPath path, final PhotoStep steps) {
    return stepAndPathToProcess().entrySet().stream()
        .filter(processesMapEntry -> processesMapEntry.getValue().containsKey(steps)
            && processesMapEntry.getValue().get(steps).contains(path))
        .map(Map.Entry::getKey)
        .findFirst();
  }
}
