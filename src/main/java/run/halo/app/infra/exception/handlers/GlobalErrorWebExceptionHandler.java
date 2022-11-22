package run.halo.app.infra.exception.handlers;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.lang.Nullable;
import org.springframework.web.ErrorResponse;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import run.halo.app.extension.exception.ExtensionConvertException;
import run.halo.app.extension.exception.ExtensionNotFoundException;
import run.halo.app.extension.exception.SchemaViolationException;
import run.halo.app.extension.exception.SchemeNotFoundException;
import run.halo.app.infra.exception.AccessDeniedException;
import run.halo.app.infra.exception.NotFoundException;
import run.halo.app.infra.exception.ThemeInstallationException;
import run.halo.app.infra.exception.ThemeUninstallException;

/**
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
public class GlobalErrorWebExceptionHandler extends DefaultErrorWebExceptionHandler {


    /**
     * Create a new {@code DefaultErrorWebExceptionHandler} instance.
     *
     * @param errorAttributes the error attributes
     * @param resources the resources configuration properties
     * @param errorProperties the error configuration properties
     * @param applicationContext the current application context
     * @since 2.4.0
     */
    public GlobalErrorWebExceptionHandler(ErrorAttributes errorAttributes,
        WebProperties.Resources resources,
        ErrorProperties errorProperties,
        ApplicationContext applicationContext) {
        super(errorAttributes, resources, errorProperties, applicationContext);
    }

    @Override
    protected Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        Throwable error = getError(request);
        log.error(error.getMessage(), error);

        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        if (error instanceof ErrorResponse errorResponse) {
            problemDetail = errorResponse.getBody();
        } else if (error instanceof SchemaViolationException schemaViolationException) {
            problemDetail =
                handleSchemaViolationException(schemaViolationException, request.exchange());
        } else if (error instanceof SchemeNotFoundException
            || error instanceof ExtensionNotFoundException
            || error instanceof NotFoundException) {
            problemDetail = handleException(error, HttpStatus.NOT_FOUND, request.exchange());
        } else if (error instanceof ExtensionConvertException
            || error instanceof ThemeInstallationException
            || error instanceof ThemeUninstallException
            || error instanceof IllegalArgumentException
            || error instanceof IllegalStateException) {
            problemDetail = handleException(error, HttpStatus.BAD_REQUEST, request.exchange());
        } else if (error instanceof AccessDeniedException accessDeniedException) {
            problemDetail = handleAccessDeniedException(accessDeniedException, request.exchange());
        } else if (error instanceof OptimisticLockingFailureException optimisticLockingException) {
            problemDetail = handleOptimisticLockingFailureException(optimisticLockingException,
                request.exchange());
        }

        return ServerResponse.status(problemDetail.getStatus())
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(problemDetail));
    }

    private ProblemDetail handleException(Throwable error, HttpStatus status,
        ServerWebExchange exchange) {
        return createProblemDetail(error, status,
            error.getMessage(), null,
            null,
            exchange);
    }

    private ProblemDetail handleSchemaViolationException(SchemaViolationException exception,
        ServerWebExchange exchange) {
        List<InvalidParam> invalidParams = exception.getErrors().items()
            .stream()
            .map(item -> new InvalidParam(item.dataCrumbs(), item.message()))
            .collect(Collectors.toList());
        ProblemDetail problemDetail = createProblemDetail(exception, HttpStatus.BAD_REQUEST,
            exception.getMessage(), null,
            null,
            exchange);
        problemDetail.setTitle("Your request parameters didn't validate.");
        problemDetail.setProperty("invalidParams", invalidParams);
        return problemDetail;
    }

    private ProblemDetail handleOptimisticLockingFailureException(
        OptimisticLockingFailureException exception,
        ServerWebExchange exchange) {
        return createProblemDetail(exception, HttpStatus.CONFLICT,
            exception.getMessage(), null,
            null,
            exchange);
    }

    record InvalidParam(String name, String reason) {
    }

    private ProblemDetail handleAccessDeniedException(
        AccessDeniedException error, ServerWebExchange exchange) {
        return createProblemDetail(error, HttpStatus.FORBIDDEN,
            error.getMessage(), null, null,
            exchange);
    }

    protected ProblemDetail createProblemDetail(
        Throwable ex, HttpStatusCode status, String defaultDetail,
        @Nullable String detailMessageCode,
        @Nullable Object[] detailMessageArguments, ServerWebExchange exchange) {

        ErrorResponse response = createFor(
            ex, status, null, defaultDetail, detailMessageCode, detailMessageArguments);

        ProblemDetail problemDetail = response.getBody();
        problemDetail.setInstance(URI.create(exchange.getRequest().getPath()
            .pathWithinApplication().value()));
        return problemDetail;
    }

    static ErrorResponse createFor(
        Throwable ex, HttpStatusCode status, @Nullable HttpHeaders headers,
        String defaultDetail, @Nullable String detailMessageCode,
        @Nullable Object[] detailMessageArguments) {

        if (detailMessageCode == null) {
            detailMessageCode = ErrorResponse.getDefaultDetailMessageCode(ex.getClass(), null);
        }

        ErrorResponseException errorResponse = new ErrorResponseException(
            status, ProblemDetail.forStatusAndDetail(status, defaultDetail), null,
            detailMessageCode, detailMessageArguments);

        if (headers != null) {
            errorResponse.getHeaders().putAll(headers);
        }

        return errorResponse;
    }

}
