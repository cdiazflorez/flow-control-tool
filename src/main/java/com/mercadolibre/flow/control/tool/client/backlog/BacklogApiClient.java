package com.mercadolibre.flow.control.tool.client.backlog;

import static com.mercadolibre.flow.control.tool.client.config.RestPool.BACKLOG;
import static java.lang.String.format;
import static org.springframework.http.HttpStatus.OK;

import com.mercadolibre.fbm.wms.outbound.commons.rest.HttpClient;
import com.mercadolibre.fbm.wms.outbound.commons.rest.HttpRequest;
import com.mercadolibre.flow.control.tool.client.backlog.dto.LastPhotoRequest;
import com.mercadolibre.flow.control.tool.client.backlog.dto.PhotoRequest;
import com.mercadolibre.flow.control.tool.client.backlog.dto.PhotoResponse;
import com.mercadolibre.json.type.TypeReference;
import com.mercadolibre.restclient.MeliRestClient;
import com.newrelic.api.agent.Trace;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

/**
 * BacklogApi Client.
 */
@Slf4j
@Component
public class BacklogApiClient extends HttpClient {

  private static final String BACKLOG_PHOTO_LAST_URL = "/fbm/flow/backlogs/logistic_centers/%s/photos/last";
  private static final String BACKLOG_PHOTO_URL = "/fbm/flow/backlogs/logistic_centers/%s/photos";

  public BacklogApiClient(final MeliRestClient restClient) {
    super(restClient, BACKLOG.name());
  }

  /**
   * Get the Last Photo from BacklogApi Client using the given request.
   *
   * @param lastPhotoRequest the request object
   * @return LastPhotoResponse based on client json response
   */
  @Trace
  public PhotoResponse getLastPhoto(final LastPhotoRequest lastPhotoRequest) {
    final HttpRequest httpRequest = HttpRequest.builder()
        .url(format(BACKLOG_PHOTO_LAST_URL, lastPhotoRequest.getLogisticCenterId()))
        .GET()
        .queryParams(lastPhotoRequest.getQueryParams())
        .acceptedHttpStatuses(Set.of(OK))
        .build();

    return send(httpRequest, response -> response.getData(new TypeReference<>() {
    }));
  }

  /**
   * Get Photos from BacklogApi Client using the given request.
   *
   * @param photoRequest the request object
   * @return PhotoResponse list based on client json response
   */
  @Trace
  public List<PhotoResponse> getPhotos(final PhotoRequest photoRequest) {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("X-Iterative-Query", String.valueOf(true));

    final HttpRequest request = HttpRequest.builder()
        .url(format(BACKLOG_PHOTO_URL, photoRequest.logisticCenterId()))
        .headers(httpHeaders.toSingleValueMap())
        .GET()
        .queryParams(photoRequest.queryParams())
        .acceptedHttpStatuses(Set.of(OK))
        .build();

    return send(
        request,
        response -> response.getData(new TypeReference<>() {
        })
    );
  }
}
