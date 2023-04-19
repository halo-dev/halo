package run.halo.app.infra.exception.handlers;

import static org.springframework.core.annotation.MergedAnnotations.SearchStrategy;
import static org.springframework.core.annotation.MergedAnnotations.from;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.MessageSource;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ResponseStatus;
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
        var responseStatusAnno = from(error.getClass(), SearchStrategy.TYPE_HIERARCHY)
            .get(ResponseStatus.class);

        var status = determineHttpStatus(error, responseStatusAnno);
        final ErrorResponse errorResponse;
        if (error instanceof ErrorResponse er) {
            errorResponse = er;
        } else {
            var reason = Optional.of(status)
                .filter(HttpStatusCode::is5xxServerError)
                .map(s -> "Something went wrong, please try again later.")
                .orElseGet(() -> responseStatusAnno.getValue("reason", String.class)
                    .orElse(error.getMessage())
                );
            errorResponse = ErrorResponse.create(error, status, reason);
        }
        var problemDetail =
            errorResponse.updateAndGetBody(messageSource, getLocale(request.exchange()));

        problemDetail.setInstance(URI.create(request.path()));
        problemDetail.setProperty("requestId", request.exchange().getRequest().getId());
        problemDetail.setProperty("timestamp", Instant.now());

        // For backward compatibility(rendering view need)
        errAttributes.put("error", problemDetail);
        return errAttributes;
    }

    private HttpStatusCode determineHttpStatus(Throwable t,
        MergedAnnotation<ResponseStatus> responseStatusAnno) {
        if (t instanceof ErrorResponse rse) {
            return rse.getStatusCode();
        }
        return responseStatusAnno.getValue("code", HttpStatus.class)
            .orElse(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Locale getLocale(ServerWebExchange exchange) {
        var locale = exchange.getLocaleContext().getLocale();
        return locale != null ? locale : Locale.getDefault();
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
