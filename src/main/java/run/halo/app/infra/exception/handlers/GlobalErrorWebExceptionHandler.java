package run.halo.app.infra.exception.handlers;

import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponse;
import org.springframework.web.method.annotation.ExceptionHandlerMethodResolver;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.method.InvocableHandlerMethod;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

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
    private final ExceptionHandlingProblemDetailsHandler exceptionHandler =
        new ExceptionHandlingProblemDetailsHandler();
    private final ExceptionHandlerMethodResolver handlerMethodResolver =
        new ExceptionHandlerMethodResolver(ExceptionHandlingProblemDetailsHandler.class);

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
