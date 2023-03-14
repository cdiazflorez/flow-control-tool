package com.mercadolibre.flow.control.tool.client.config;

import static java.lang.String.format;

import com.mercadolibre.restclient.Request;
import com.mercadolibre.restclient.Response;
import com.mercadolibre.restclient.exception.RestException;
import com.mercadolibre.restclient.interceptor.RequestResponseInterceptor;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

@Slf4j
public enum RestClientLoggingInterceptor implements RequestResponseInterceptor {
  INSTANCE;

  private static final String REQUEST_LOG = "%n\tRequest:";

  private static final String METHOD_LOG = "%n\t\tMethod = %s";

  private static final String URI_LOG = "%n\t\tURI = %s";

  private static final String QUERY_LOG = "%n\t\tQuery = %s";

  private static final String HEADERS_LOG = "%n\t\tHeaders = %s";

  private static final String BODY_LOG = "%n\t\tBody = %s";

  private static final String STATUS_LOG = "%n\t\tStatus = %s";

  private static final String TIME_LOG = "%n\t\tTime = %s";

  private static final String NAME_LOG = "%n\t\tName = %s";

  private static final String MESSAGE_LOG = "%n\t\tMessage = %s";

  private static final String LINE_BREAK = "%n";

  @Override
  public void intercept(final Request request, final Response response) {
    log.debug(buildSuccessLog(request, response));
  }

  @Override
  public void intercept(final Request request, final RestException restException) {
    log.error(buildErrorLog(request, restException), restException);
  }

  public String buildSuccessLog(final Request request, final Response response) {
    return format("[api:%s] Api Request"
            + REQUEST_LOG
            + METHOD_LOG
            + URI_LOG
            + QUERY_LOG
            + HEADERS_LOG
            + BODY_LOG
            + "%n\tResponse:"
            + STATUS_LOG
            + TIME_LOG
            + HEADERS_LOG
            + BODY_LOG
            + LINE_BREAK,
        request.getPool().getName().toLowerCase(Locale.getDefault()),
        request.getMethod(),
        request.getPlainURL(),
        request.getParameters(),
        request.getHeaders(),
        request.getBody() == null
            ? null : new String(request.getBody(), StandardCharsets.UTF_8),
        HttpStatus.valueOf(response.getStatus()),
        System.currentTimeMillis() - (Long) request.getAttribute("requestTime"),
        response.getHeaders(),
        response.getString()
    );
  }

  public String buildErrorLog(final Request request, final RestException exception) {
    return format("[api:%s] Api Request Error"
            + REQUEST_LOG
            + METHOD_LOG
            + URI_LOG
            + QUERY_LOG
            + BODY_LOG
            + "%n\tError:"
            + NAME_LOG
            + MESSAGE_LOG
            + BODY_LOG
            + LINE_BREAK,
        request.getPool().getName().toLowerCase(Locale.getDefault()),
        request.getMethod(),
        request.getPlainURL(),
        request.getParameters(),
        request.getBody() == null
            ? null : new String(request.getBody(), StandardCharsets.UTF_8),
        exception.getClass().getSimpleName(),
        exception.getMessage(),
        exception.getBody()
    );
  }
}
