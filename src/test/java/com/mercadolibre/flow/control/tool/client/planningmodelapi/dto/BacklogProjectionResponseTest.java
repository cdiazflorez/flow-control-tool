package com.mercadolibre.flow.control.tool.client.planningmodelapi.dto;

import static com.mercadolibre.flow.control.tool.client.planningmodelapi.constant.OutboundProcessName.PICKING;
import static com.mercadolibre.flow.control.tool.feature.entity.ProcessPathName.TOT_MONO;
import static com.mercadolibre.flow.control.tool.util.TestUtils.getResourceAsString;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.BacklogProjectionResponse.Backlog;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.BacklogProjectionResponse.Process;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.BacklogProjectionResponse.ProcessPath;
import com.mercadolibre.flow.control.tool.client.planningmodelapi.dto.BacklogProjectionResponse.Sla;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class BacklogProjectionResponseTest {

  private static final Instant OPERATION_HOUR = Instant.parse("2023-03-17T14:00:00Z");

  private static final Integer QUANTITY = 50;

  private static final Instant DATE_OUT = Instant.parse("2023-03-17T15:00:00Z");

  @Test
  void testBacklogProjectionResponseValues() throws JsonProcessingException {

    // GIVEN
    final ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    final List<BacklogProjectionResponse> backlogProjectionResponse = objectMapper.readValue(
        getResourceAsString("client/response_get_backlog_projection.json"),
        objectMapper.getTypeFactory().constructCollectionType(List.class, BacklogProjectionResponse.class)
    );
    // THEN
    assertEquals(OPERATION_HOUR, backlogProjectionResponse.get(0).operationHour());
    assertProjectionResponseBacklog(backlogProjectionResponse.get(0).backlog().get(0));

  }

  private Backlog mockBacklogForProjectionResponse() {

    final ProcessPath processPath = new ProcessPath(TOT_MONO, QUANTITY);
    final Sla sla = new Sla(DATE_OUT, List.of(processPath));
    final Process process = new Process(PICKING, List.of(sla));

    return new Backlog(List.of(process));
  }

  private void assertProjectionResponseBacklog(final Backlog backlogInProjectionResponse) {

    final Backlog expectedBacklog = mockBacklogForProjectionResponse();

    assertEquals(expectedBacklog, backlogInProjectionResponse);
    assertEquals(expectedBacklog.process(), backlogInProjectionResponse.process());
    assertEquals(PICKING, backlogInProjectionResponse.process().get(0).name());
    assertEquals(expectedBacklog.process().get(0).sla(), backlogInProjectionResponse.process().get(0).sla());
    assertEquals(DATE_OUT, backlogInProjectionResponse.process().get(0).sla().get(0).dateOut());
    assertEquals(
        expectedBacklog.process().get(0).sla().get(0).processPath(),
        backlogInProjectionResponse.process().get(0).sla().get(0).processPath()
    );
    assertEquals(TOT_MONO, backlogInProjectionResponse.process().get(0).sla().get(0).processPath().get(0).name());
    assertEquals(QUANTITY, backlogInProjectionResponse.process().get(0).sla().get(0).processPath().get(0).quantity());
  }

}
