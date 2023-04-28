package com.mercadolibre.flow.control.tool.client;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mercadolibre.flow.control.tool.feature.backlog.monitor.BacklogProjectedUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * TODO: Temporary class. Should be removed once the real adapter has been implemented.
 */
@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class AdapterAuxTest {

  @Autowired
  BacklogProjectedUseCase.BacklogGateway backlogGateway;

  @Autowired
  BacklogProjectedUseCase.PlanningEntitiesGateway planningApiGateway;

  @Autowired
  BacklogProjectedUseCase.BacklogProjectionGateway backlogProjectionGateway;

  @Test
  public void test() {
    backlogProjectionGateway.executeBacklogProjection(null, null, null, null, null, null);
    backlogGateway.getCurrentBacklog(null, null, null, null);
    planningApiGateway.getPlannedBacklog(null, null, null, null);
    planningApiGateway.getThroughput(null, null, null, null, null);

    assertTrue(true);
  }
}
