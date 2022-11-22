package run.halo.app.infra.exception.handlers;

import java.util.Map;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
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
import run.halo.app.extension.exception.SchemaViolationException;

/**
 * @author guqing
 * @since 2.0.0
 */
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
        Map<String, Object> errorMap =
            getErrorAttributes(request, getErrorAttributeOptions(request, MediaType.ALL));
        Throwable error = getError(request);
        ProblemDetail problemDetail =
            ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        if (error instanceof SchemaViolationException) {
            problemDetail =
                createProblemDetail(error, HttpStatus.BAD_REQUEST, "Schema violation", null, null,
                    request.exchange());
        }
        return ServerResponse.status(getHttpStatus(errorMap))
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(problemDetail));
    }

    public ProblemDetail createProblemDetail(
        Throwable ex, HttpStatusCode status, String defaultDetail,
        @Nullable String detailMessageCode,
        @Nullable Object[] detailMessageArguments, ServerWebExchange exchange) {

        ErrorResponse response = createFor(
            ex, status, null, defaultDetail, detailMessageCode, detailMessageArguments);

        return response.getBody();
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
