package com.mercadolibre.flow.control.tool.exception;

import com.mercadolibre.flow.control.tool.feature.entity.ProcessName;
import com.mercadolibre.flow.control.tool.feature.forecastdeviation.constant.Filter;
import com.newrelic.api.agent.NewRelic;
import java.time.DateTimeException;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * Basic handling for exceptions.
 */
@ControllerAdvice
public class ControllerExceptionHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(ControllerExceptionHandler.class);

  /**
   * Handler for not found routes.
   *
   * @param req the incoming request.
   * @param ex  the exception thrown when route is not found.
   * @return {@link ResponseEntity} with 404 status code and the route that was not found in the body.
   */
  @ExceptionHandler(NoHandlerFoundException.class)
  public ResponseEntity<ApiError> noHandlerFoundException(
      HttpServletRequest req, NoHandlerFoundException ex) {
    ApiError apiError =
        new ApiError(
            "route_not_found",
            String.format("Route %s not found", req.getRequestURI()),
            HttpStatus.NOT_FOUND.value());
    return ResponseEntity.status(apiError.getStatus()).body(apiError);
  }

  /**
   * Handler for when enums don't match.
   *
   * @param req the incoming request.
   * @return {@link ResponseEntity} with 400 status code .
   */
  @ExceptionHandler(WorkflowNotSupportedException.class)
  public ResponseEntity<ApiError> handleWorkflowNotSupportedException(
      HttpServletRequest req) {
    ApiError apiError =
        new ApiError(
            "bad_request",
            String.format("bad request %s", req.getRequestURI()),
            HttpStatus.BAD_REQUEST.value());
    return ResponseEntity.status(apiError.getStatus()).body(apiError);
  }

  /**
   * Handler for external API exceptions.
   *
   * @param e the exception thrown during a request to external API.
   * @return {@link ResponseEntity} with status code and description provided for the handled exception.
   */
  @ExceptionHandler(ApiException.class)
  protected ResponseEntity<ApiError> handleApiException(ApiException e) {
    Integer statusCode = e.getStatusCode();
    boolean expected = HttpStatus.INTERNAL_SERVER_ERROR.value() > statusCode;
    NewRelic.noticeError(e, expected);
    if (expected) {
      LOGGER.warn("Internal Api warn. Status Code: " + statusCode, e);
    } else {
      LOGGER.error("Internal Api error. Status Code: " + statusCode, e);
    }

    ApiError apiError = new ApiError(e.getCode(), e.getDescription(), statusCode);
    return ResponseEntity.status(apiError.getStatus()).body(apiError);
  }

  /**
   * Handler for internal exceptions.
   *
   * @param e the exception thrown during request processing.
   * @return {@link ResponseEntity} with 500 status code and description indicating an internal error.
   */
  @ExceptionHandler(Exception.class)
  protected ResponseEntity<ApiError> handleUnknownException(Exception e) {
    LOGGER.error("Internal error", e);
    NewRelic.noticeError(e);

    ApiError apiError =
        new ApiError(
            "internal_error", "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR.value());
    return ResponseEntity.status(apiError.getStatus()).body(apiError);
  }

  /**
   * Handler for ForecastMetadata exceptions.
   *
   * @param ex the exception thrown during request processing.
   * @return {@link ResponseEntity} with 500 status code and description indicating an internal error.
   */
  @ExceptionHandler(NoForecastMetadataFoundException.class)
  public ResponseEntity<ApiError> handlerNoForecastMetadataFoundException(
      NoForecastMetadataFoundException ex) {
    LOGGER.error("No Content", ex);
    NewRelic.noticeError(ex);

    ApiError apiError = new ApiError(
        "no_content",
        ex.getMessage(),
        HttpStatus.NO_CONTENT.value()
    );

    return ResponseEntity.status(apiError.getStatus()).body(apiError);
  }

  /**
   * Handler for when projection inputs cannot be obtained.
   *
   * @param ex the exception thrown during request processing.
   * @return {@link ResponseEntity} with 404 status code and description indicating an internal error.
   */
  @ExceptionHandler(ProjectionInputsNotFoundException.class)
  public ResponseEntity<ApiError> handlerProjectionInputsNotFoundException(
      ProjectionInputsNotFoundException ex) {
    LOGGER.error("Projection inputs not found", ex);
    NewRelic.noticeError(ex);

    ApiError apiError = new ApiError(
        "not_found",
        ex.getMessage(),
        HttpStatus.FAILED_DEPENDENCY.value()
    );

    return ResponseEntity.status(apiError.getStatus()).body(apiError);
  }

  /**
   * Handler for ForecastNotFound exceptions.
   *
   * @param ex the exception thrown during request processing.
   * @return {@link ResponseEntity} with 204 status code and description indicating a no content.
   */
  @ExceptionHandler(ForecastNotFoundException.class)
  public ResponseEntity<ApiError> handlerForecastNotFoundException(
      ForecastNotFoundException ex) {
    LOGGER.error("No Content", ex);
    NewRelic.noticeError(ex);

    ApiError apiError = new ApiError(
        "no_content",
        ex.getMessage(),
        HttpStatus.NO_CONTENT.value()
    );

    return ResponseEntity.status(apiError.getStatus()).body(apiError);
  }

  /**
   * Handler for UnitsPerOrderRatio exceptions.
   *
   * @param ex the exception thrown during request processing.
   * @return {@link ResponseEntity} with 204 status code and description indicating a no content.
   */
  @ExceptionHandler(NoUnitsPerOrderRatioFound.class)
  public ResponseEntity<ApiError> handlerUnitsPerOrderRatioException(
      NoUnitsPerOrderRatioFound ex) {
    LOGGER.error("No Content", ex);
    NewRelic.noticeError(ex);

    ApiError apiError = new ApiError(
        "no_content",
        ex.getMessage(),
        HttpStatus.NO_CONTENT.value()
    );

    return ResponseEntity.status(apiError.getStatus()).body(apiError);
  }

  /**
   * Handler for Throughput exceptions.
   *
   * @param ex the exception thrown during request processing.
   * @return {@link ResponseEntity} with 204 status code and description indicating a no content.
   */
  @ExceptionHandler(ThroughputNotFoundException.class)
  public ResponseEntity<ApiError> handlerThroughputException(
      ThroughputNotFoundException ex) {
    LOGGER.error("Global throughput dependency failure", ex);
    NewRelic.noticeError(ex);

    ApiError apiError = new ApiError(
        "failed_dependency",
        ex.getMessage(),
        HttpStatus.FAILED_DEPENDENCY.value()
    );

    return ResponseEntity.status(apiError.getStatus()).body(apiError);
  }

  /**
   * Handler for when enums Process don't match.
   *
   * @param req the incoming request.
   * @return {@link ResponseEntity} with 400 status code .
   */
  @ExceptionHandler(ProcessNotSupportedException.class)
  public ResponseEntity<ApiError> handleProcessNotSupported(final HttpServletRequest req) {
    final List<String> allowedValues = Arrays.stream(ProcessName.values())
        .map(Enum::name)
        .toList();

    final ApiError apiError = new ApiError(
        "bad_request",
        String.format("bad request %s. Allowed values are: %s", req.getRequestURI(), allowedValues),
        HttpStatus.BAD_REQUEST.value());

    return ResponseEntity.status(apiError.getStatus()).body(apiError);
  }

  /**
   * Handler for DateTime exceptions.
   *
   * @param ex the exception thrown during a request to external API.
   * @return {@link ResponseEntity} with 400 status code .
   */
  @ExceptionHandler(DateTimeException.class)
  public ResponseEntity<ApiError> handleDateTimeException(final DateTimeException ex) {

    final ApiError apiError = new ApiError(
        "bad_request",
        ex.getMessage(),
        HttpStatus.BAD_REQUEST.value()
    );

    return ResponseEntity.status(apiError.getStatus()).body(apiError);
  }

  /**
   * Handler for Real Metrics exceptions.
   *
   * @param ex the exception thrown during a request to external API.
   * @return {@link ResponseEntity} with 404 status code and description indicating a no content.
   */
  @ExceptionHandler(RealMetricsException.class)
  public ResponseEntity<ApiError> handleRealMetricsNotFoundException(final RealMetricsException ex) {
    final ApiError apiError = new ApiError(
        "real_metrics_exception",
        ex.getMessage(),
        ex.getStatus()
    );

    return ResponseEntity.status(apiError.getStatus()).body(apiError);
  }

  @ExceptionHandler(InvalidDateRangeException.class)
  public ResponseEntity<ApiError> handleDateRangeException(final InvalidDateRangeException ex) {
    final ApiError apiError = new ApiError(
        "bad_request",
        ex.getMessage(),
        HttpStatus.BAD_REQUEST.value()
    );

    LOGGER.error(apiError.getError());

    return ResponseEntity.status(apiError.getStatus()).body(apiError);
  }

  @ExceptionHandler(InvalidateDateDurationRangeException.class)
  public ResponseEntity<ApiError> handleInvalidateDateDurationRangeException(final InvalidateDateDurationRangeException ex) {
    final ApiError apiError = new ApiError(
        "bad_request",
        ex.getMessage(),
        HttpStatus.BAD_REQUEST.value()
    );

    LOGGER.error(apiError.getError());

    return ResponseEntity.status(apiError.getStatus()).body(apiError);
  }


  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ApiError> handleConversionFailException(final MethodArgumentTypeMismatchException ex) {
    final ApiError apiError = new ApiError(
        "bad_request",
        ex.getMessage(),
        HttpStatus.BAD_REQUEST.value()
    );

    LOGGER.error(apiError.getError());

    return ResponseEntity.status(apiError.getStatus()).body(apiError);
  }

  /**
   * Handler for Total backlog projection exceptions.
   *
   * @param ex the exception thrown during a request to external API.
   * @return {@link ResponseEntity} status code and description.
   */
  @ExceptionHandler(TotalProjectionException.class)
  public ResponseEntity<ApiError> handleTotalProjectionException(final TotalProjectionException ex) {
    final ApiError apiError = new ApiError(
        "total_projection_exception",
        ex.getMessage(),
        ex.getStatus()
    );

    return ResponseEntity.status(apiError.getStatus()).body(apiError);
  }

  /**
   * Handler for when enums don't match.
   *
   * @param req the incoming request.
   * @return {@link ResponseEntity} with 400 status code .
   */
  @ExceptionHandler(FilterNotSupportedException.class)
  public ResponseEntity<ApiError> handleFilterNotSupportedException(final HttpServletRequest req) {
    final List<String> allowedValues = Arrays.stream(Filter.values())
        .map(Enum::name)
        .toList();

    final ApiError apiError = new ApiError(
        "bad_request",
        String.format("bad request %s. Allowed values are: %s", req.getRequestURI(), allowedValues),
        HttpStatus.BAD_REQUEST.value()
    );

    return ResponseEntity.status(apiError.getStatus()).body(apiError);
  }

}
