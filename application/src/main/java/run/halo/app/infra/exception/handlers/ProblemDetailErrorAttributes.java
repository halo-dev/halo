package run.halo.app.infra.exception.handlers;

import static run.halo.app.infra.exception.Exceptions.createErrorResponse;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.MessageSource;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;

/**
 * See {@link DefaultErrorAttributes} for more.
 *
 * @author johnn
 */
public class ProblemDetailErrorAttributes implements ErrorAttributes {

    private static final String ERROR_INTERNAL_ATTRIBUTE =
        ProblemDetailErrorAttributes.class.getName() + ".ERROR";

    private final MessageSource messageSource;

    public ProblemDetailErrorAttributes(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request,
        ErrorAttributeOptions options) {
        final var errAttributes = new LinkedHashMap<String, Object>();
        var error = getError(request);
        var errorResponse = createErrorResponse(error, null, request.exchange(), messageSource);
        errAttributes.put("error", errorResponse.getBody());
        return errAttributes;
    }

    @Override
    public Throwable getError(ServerRequest request) {
        return (Throwable) request.attribute(ERROR_INTERNAL_ATTRIBUTE).stream()
            .peek(error -> request.attributes().putIfAbsent(ERROR_ATTRIBUTE, error))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                "Missing exception attribute in ServerWebExchange"));
    }

    @Override
    public void storeErrorInformation(Throwable error, ServerWebExchange exchange) {
        exchange.getAttributes().putIfAbsent(ERROR_INTERNAL_ATTRIBUTE, error);
    }


}
