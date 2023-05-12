package com.mercadolibre.flow.control.tool.client.planningmodelapi.adapter;

import static com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.PlanningWorkflow.FBM_WMS_OUTBOUND;
import static com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.Source.FORECAST;
import static com.mercadolibre.flow.control.tool.util.TestUtils.LOGISTIC_CENTER_ID;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.mercadolibre.flow.control.tool.client.planningmodelapi.PlanningModelApiClient;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.EntityType;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.OutboundProcessName;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.ProcessingType;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.EntityDataDto;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.EntityRequestDto;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.BacklogLimit;
import com.mercadolibre.flow.control.tool.feature.backlog.monitor.dto.ProcessLimit;
import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BacklogLimitAdapterTest {

  private static final String METRIC_UNIT = "units";

  private static final Instant DATE_FROM = Instant.parse("2023-03-21T08:00:00Z");

  private static final Instant DATE_TO = Instant.parse("2023-03-21T09:00:00Z");

  private static final List<EntityType> ENTITY_TYPES =
      List.of(EntityType.BACKLOG_UPPER_LIMIT, EntityType.BACKLOG_LOWER_LIMIT);

  private static final EntityRequestDto ENTITY_REQUEST =
      new EntityRequestDto(FBM_WMS_OUTBOUND,
          ENTITY_TYPES, LOGISTIC_CENTER_ID,
          DATE_FROM,
          DATE_TO,
          List.of(OutboundProcessName.PICKING,
              OutboundProcessName.PACKING),
          emptyMap());

  @InjectMocks
  private BacklogLimitAdapter backlogLimitAdapter;
  @Mock
  private PlanningModelApiClient planningModelApiClient;

  private static Stream<Arguments> parameterTest() {
    return Stream.of(
        Arguments.of(
            mockMetadata(),
            getBacklogLimitsExpected()
        ),
        Arguments.of(
            emptyMap(),
            emptyList()
        )
    );
  }

  @ParameterizedTest
  @MethodSource("parameterTest")
  void testBacklogLimitAdapter(final Map<EntityType, List<EntityDataDto>> clientResponse, final List<BacklogLimit> expectedResponse) {
    // GIVEN

    when(planningModelApiClient.searchEntities(ENTITY_REQUEST)).thenReturn(clientResponse);

    // WHEN
    List<BacklogLimit> backlogLimits = backlogLimitAdapter.getBacklogLimits(LOGISTIC_CENTER_ID, FBM_WMS_OUTBOUND,
        List.of(OutboundProcessName.PICKING, OutboundProcessName.PACKING), DATE_FROM, DATE_TO);
    // THEN

    assertEquals(expectedResponse.size(), backlogLimits.size());
    assertion(expectedResponse, backlogLimits);
  }

  private static List<BacklogLimit> getBacklogLimitsExpected() {
    final ProcessLimit processLimit2 = new ProcessLimit(ProcessName.PICKING, 5000L, 11000L);
    final ProcessLimit processLimit4 = new ProcessLimit(ProcessName.PICKING, 4000L, 10000L);
    final ProcessLimit processLimit3 = new ProcessLimit(ProcessName.PACKING, 3000L, 90000L);
    final ProcessLimit processLimit1 = new ProcessLimit(ProcessName.PACKING, 2000L, 8000L);
    final List<ProcessLimit> processLimits1 = List.of(processLimit1, processLimit2);
    final List<ProcessLimit> processLimits2 = List.of(processLimit3, processLimit4);

    final BacklogLimit backlogLimit1 = new BacklogLimit(DATE_TO, processLimits1);
    final BacklogLimit backlogLimit2 = new BacklogLimit(DATE_FROM, processLimits2);

    return List.of(backlogLimit1, backlogLimit2);
  }

  private static Map<EntityType, List<EntityDataDto>> mockMetadata() {

    final EntityDataDto entityUpperPicking =
        buildEntity(OutboundProcessName.PICKING, ProcessingType.BACKLOG_UPPER_LIMIT, 10000L, DATE_FROM);

    final EntityDataDto entityUpperPacking =
        buildEntity(OutboundProcessName.PACKING, ProcessingType.BACKLOG_UPPER_LIMIT, 90000L, DATE_FROM);

    final EntityDataDto entityLowerPicking =
        buildEntity(OutboundProcessName.PICKING, ProcessingType.BACKLOG_LOWER_LIMIT, 4000L, DATE_FROM);

    final EntityDataDto entityLowerPacking =
        buildEntity(OutboundProcessName.PACKING, ProcessingType.BACKLOG_LOWER_LIMIT, 3000L, DATE_FROM);

    final EntityDataDto entityUpperHourPicking =
        buildEntity(OutboundProcessName.PICKING, ProcessingType.BACKLOG_UPPER_LIMIT, 11000L, DATE_TO);

    final EntityDataDto entityUpperHourPacking =
        buildEntity(OutboundProcessName.PACKING, ProcessingType.BACKLOG_UPPER_LIMIT, 8000L, DATE_TO);

    final EntityDataDto entityLowerHourPicking =
        buildEntity(OutboundProcessName.PICKING, ProcessingType.BACKLOG_LOWER_LIMIT, 5000L, DATE_TO);

    final EntityDataDto entityLowerHourPacking =
        buildEntity(OutboundProcessName.PACKING, ProcessingType.BACKLOG_LOWER_LIMIT, 2000L, DATE_TO);

    final List<EntityDataDto> entityUpperDataDto = new ArrayList<>();
    final List<EntityDataDto> entityLowerDataDto = new ArrayList<>();
    entityUpperDataDto.add(entityUpperPacking);
    entityLowerDataDto.add(entityLowerPacking);
    entityUpperDataDto.add(entityUpperPicking);
    entityLowerDataDto.add(entityLowerPicking);
    entityUpperDataDto.add(entityUpperHourPacking);
    entityLowerDataDto.add(entityLowerHourPacking);
    entityUpperDataDto.add(entityUpperHourPicking);
    entityLowerDataDto.add(entityLowerHourPicking);

    final Map<EntityType, List<EntityDataDto>> entityDataDtoMap = new ConcurrentHashMap<>();
    entityDataDtoMap.put(EntityType.BACKLOG_UPPER_LIMIT, entityUpperDataDto);
    entityDataDtoMap.put(EntityType.BACKLOG_LOWER_LIMIT, entityLowerDataDto);

    return entityDataDtoMap;
  }

  private static EntityDataDto buildEntity(final OutboundProcessName processName,
                                           final ProcessingType processingType,
                                           final Long value,
                                           final Instant date) {
    return new EntityDataDto(
        FBM_WMS_OUTBOUND,
        date,
        null,
        processName,
        processingType,
        METRIC_UNIT,
        FORECAST,
        value);
  }

  private void assertion(final List<BacklogLimit> expecteds, final List<BacklogLimit> response) {
    expecteds.forEach(expected -> {
      assertTrue(response.stream().anyMatch(x -> x.date().equals(expected.date())));
      var backlogLimitResponse = response.stream().filter(x -> x.date().equals(expected.date())).findAny().orElseThrow();
      assertionProcessName(expected.processes(), backlogLimitResponse.processes());
    });
  }

  private void assertionProcessName(final List<ProcessLimit> expected, final List<ProcessLimit> response) {
    assertEquals(expected.size(), response.size());
    expected.forEach(
        processExpected -> {
          var processLimitResponse = response.stream()
              .filter(x -> x.name().equals(processExpected.name())).findAny().orElseThrow();
          assertEquals(processExpected, processLimitResponse);
        }
    );
  }
}
