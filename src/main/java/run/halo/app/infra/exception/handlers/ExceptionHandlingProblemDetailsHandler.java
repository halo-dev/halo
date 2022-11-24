package run.halo.app.infra.exception.handlers;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.lang.Nullable;
import org.springframework.web.ErrorResponse;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ServerWebExchange;
import run.halo.app.extension.exception.ExtensionConvertException;
import run.halo.app.extension.exception.ExtensionNotFoundException;
import run.halo.app.extension.exception.SchemaViolationException;
import run.halo.app.extension.exception.SchemeNotFoundException;
import run.halo.app.infra.exception.AccessDeniedException;
import run.halo.app.infra.exception.NotFoundException;
import run.halo.app.infra.exception.ThemeInstallationException;
import run.halo.app.infra.exception.ThemeUninstallException;

/**
 * Handle exceptions and convert them to {@link ProblemDetail}.
 *
 * @author guqing
 * @since 2.0.0
 */
public class ExceptionHandlingProblemDetailsHandler {

    @ExceptionHandler({SchemeNotFoundException.class, ExtensionNotFoundException.class,
        NotFoundException.class})
    public ProblemDetail handleNotFoundException(Throwable error,
        ServerWebExchange exchange) {
        return createProblemDetail(error, HttpStatus.NOT_FOUND,
            error.getMessage(), exchange);
    }

    @ExceptionHandler(SchemaViolationException.class)
    public ProblemDetail handleSchemaViolationException(SchemaViolationException exception,
        ServerWebExchange exchange) {
        List<InvalidParam> invalidParams =
            exception.getErrors().items()
                .stream()
                .map(item -> new InvalidParam(item.dataCrumbs(),
                    item.message())
                )
                .collect(Collectors.toList());
        ProblemDetail problemDetail = createProblemDetail(exception, HttpStatus.BAD_REQUEST,
            exception.getMessage(), exchange);
        problemDetail.setTitle("Your request parameters didn't validate.");
        problemDetail.setProperty("invalidParams", invalidParams);
        return problemDetail;
    }

    @ExceptionHandler({ExtensionConvertException.class, ThemeInstallationException.class,
        ThemeUninstallException.class, IllegalArgumentException.class, IllegalStateException.class})
    public ProblemDetail handleBadRequestException(Throwable error,
        ServerWebExchange exchange) {
        return createProblemDetail(error, HttpStatus.BAD_REQUEST,
            error.getMessage(), exchange);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDeniedException(AccessDeniedException e,
        ServerWebExchange exchange) {
        return createProblemDetail(e, HttpStatus.FORBIDDEN, e.getMessage(), exchange);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ProblemDetail handleOptimisticLockingFailureException(
        OptimisticLockingFailureException e, ServerWebExchange exchange) {
        return createProblemDetail(e, HttpStatus.CONFLICT,
            e.getMessage(), exchange);
    }

    public record InvalidParam(String name, String reason) {
    }

    protected ProblemDetail createProblemDetail(Throwable ex, HttpStatusCode status,
        String defaultDetail, ServerWebExchange exchange) {

        ErrorResponse response = createFor(
            ex, status, null, defaultDetail, null, null);

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
