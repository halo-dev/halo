package run.halo.app.infra.exception.handlers;

import java.lang.reflect.Method;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.util.StringUtils;
import org.springframework.web.ErrorResponse;
import org.springframework.web.method.annotation.ExceptionHandlerMethodResolver;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.method.InvocableHandlerMethod;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.theme.ThemeResolver;

/**
 * Global error web exception handler.
 *
 * @author guqing
 * @see DefaultErrorWebExceptionHandler
 * @see ExceptionHandlingProblemDetailsHandler
 * @see ExceptionHandlerMethodResolver
 * @since 2.0.0
 */
@Slf4j
public class GlobalErrorWebExceptionHandler extends DefaultErrorWebExceptionHandler {
    private static final MediaType TEXT_HTML_UTF8 =
        new MediaType("text", "html", StandardCharsets.UTF_8);

    private static final Map<HttpStatus.Series, String> SERIES_VIEWS;

    private final ExceptionHandlingProblemDetailsHandler exceptionHandler =
        new ExceptionHandlingProblemDetailsHandler();
    private final ExceptionHandlerMethodResolver handlerMethodResolver =
        new ExceptionHandlerMethodResolver(ExceptionHandlingProblemDetailsHandler.class);

    private final ErrorProperties errorProperties;

    private final ThemeResolver themeResolver;

    static {
        Map<HttpStatus.Series, String> views = new EnumMap<>(HttpStatus.Series.class);
        views.put(HttpStatus.Series.CLIENT_ERROR, "4xx");
        views.put(HttpStatus.Series.SERVER_ERROR, "5xx");
        SERIES_VIEWS = Collections.unmodifiableMap(views);
    }

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
        this.errorProperties = errorProperties;
        this.themeResolver = applicationContext.getBean(ThemeResolver.class);
    }

    @Override
    protected Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        Throwable error = getError(request);

        if (error instanceof ErrorResponse errorResponse) {
            return ServerResponse.status(errorResponse.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(errorResponse.getBody()));
        }
        Method exceptionHandlerMethod = handlerMethodResolver.resolveMethodByThrowable(error);
        if (exceptionHandlerMethod == null) {
            return noMatchExceptionHandler(error);
        }

        InvocableHandlerMethod invocable =
            new InvocableHandlerMethod(exceptionHandler, exceptionHandlerMethod);
        BindingContext bindingContext = new BindingContext();
        ServerWebExchange exchange = request.exchange();
        return invocable.invoke(exchange, bindingContext, error, exchange)
            .mapNotNull(handleResult -> (ProblemDetail) handleResult.getReturnValue())
            .flatMap(problemDetail -> ServerResponse.status(problemDetail.getStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(problemDetail)))
            .switchIfEmpty(Mono.defer(() -> noMatchExceptionHandler(error)));
    }

    protected Mono<ServerResponse> renderErrorView(ServerRequest request) {
        Map<String, Object> errorAttributes =
            getErrorAttributes(request, getErrorAttributeOptions(request, MediaType.TEXT_HTML));
        int errorStatus = getHttpStatus(errorAttributes);

        ProblemDetail problemDetail =
            ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(errorStatus),
                (String) errorAttributes.get("message"));
        problemDetail.setInstance(URI.create(request.path()));
        Map<String, Object> error = Map.of("error", problemDetail);

        ServerResponse.BodyBuilder responseBody =
            ServerResponse.status(errorStatus).contentType(TEXT_HTML_UTF8);
        return Flux.just(getData(errorStatus).toArray(new String[] {}))
            .flatMap((viewName) -> renderErrorViewBy(request, viewName, responseBody, error))
            .switchIfEmpty(this.errorProperties.getWhitelabel().isEnabled()
                ? renderDefaultErrorView(responseBody, error) : Mono.error(getError(request)))
            .next();
    }

    protected void logError(ServerRequest request, ServerResponse response, Throwable throwable) {
        if (log.isDebugEnabled()) {
            log.debug(request.exchange().getLogPrefix() + formatError(throwable, request),
                throwable);
        }
        if (HttpStatus.resolve(response.statusCode().value()) != null
            && response.statusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
            log.error("{} 500 Server Error for {}",
                request.exchange().getLogPrefix(), formatRequest(request), throwable);
        }
    }

    private String formatRequest(ServerRequest request) {
        String rawQuery = request.uri().getRawQuery();
        String query = StringUtils.hasText(rawQuery) ? "?" + rawQuery : "";
        return "HTTP " + request.method() + " \"" + request.path() + query + "\"";
    }

    private String formatError(Throwable ex, ServerRequest request) {
        String reason = ex.getClass().getSimpleName() + ": " + ex.getMessage();
        return "Resolved [" + reason + "] for HTTP " + request.method() + " " + request.path();
    }

    private Mono<ServerResponse> renderErrorViewBy(ServerRequest request, String viewName,
        ServerResponse.BodyBuilder responseBody,
        Map<String, Object> error) {
        return themeResolver.isTemplateAvailable(request.exchange().getRequest(), viewName)
            .flatMap(isAvailable -> {
                if (isAvailable) {
                    return responseBody.render(viewName, error);
                }
                return super.renderErrorView(viewName, responseBody, error);
            });
    }

    private List<String> getData(int errorStatus) {
        List<String> data = new ArrayList<>();
        data.add("error/" + errorStatus);
        HttpStatus.Series series = HttpStatus.Series.resolve(errorStatus);
        if (series != null) {
            data.add("error/" + SERIES_VIEWS.get(series));
        }
        data.add("error/error");
        return data;
    }

    Mono<ServerResponse> noMatchExceptionHandler(Throwable error) {
        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(
                    ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                        error.getMessage())
                )
            );
    }
}
