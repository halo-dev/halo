package run.halo.app.infra.exception.handlers;

import java.util.Map;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public class HaloErrorWebExceptionHandler extends DefaultErrorWebExceptionHandler {

    /**
     * Create a new {@code DefaultErrorWebExceptionHandler} instance.
     *
     * @param errorAttributes the error attributes
     * @param resources the resources configuration properties
     * @param errorProperties the error configuration properties
     * @param applicationContext the current application context
     * @since 2.4.0
     */
    public HaloErrorWebExceptionHandler(
        ErrorAttributes errorAttributes,
        WebProperties.Resources resources,
        ErrorProperties errorProperties,
        ApplicationContext applicationContext) {
        super(errorAttributes, resources, errorProperties, applicationContext);
    }

    @Override
    protected int getHttpStatus(Map<String, Object> errorAttributes) {
        var problemDetail = (ProblemDetail) errorAttributes.get("error");
        return problemDetail.getStatus();
    }

    @Override
    protected Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        var errorAttributes =
            getErrorAttributes(request, getErrorAttributeOptions(request, MediaType.ALL));
        return ServerResponse.status(getHttpStatus(errorAttributes))
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .bodyValue(errorAttributes.get("error"));
    }
}
