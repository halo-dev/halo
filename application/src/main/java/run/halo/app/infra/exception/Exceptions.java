package run.halo.app.infra.exception;

import static org.springframework.core.annotation.MergedAnnotations.SearchStrategy.TYPE_HIERARCHY;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import java.net.URI;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ServerWebExchange;

@Slf4j
public enum Exceptions {
    ;

    public static final String DEFAULT_TYPE = "about:blank";

    public static final String THEME_ALREADY_EXISTS_TYPE =
        "https://halo.run/probs/theme-alreay-exists";

    public static final String INVALID_CREDENTIAL_TYPE =
        "https://halo.run/probs/invalid-credential";

    public static final String REQUEST_NOT_PERMITTED_TYPE =
        "https://halo.run/probs/request-not-permitted";

    /**
     * Non-ErrorResponse exception to type map.
     */
    public static final Map<Class<? extends Throwable>, String> EXCEPTION_TYPE_MAP = Map.of(
        RequestNotPermitted.class, REQUEST_NOT_PERMITTED_TYPE,
        BadCredentialsException.class, INVALID_CREDENTIAL_TYPE
    );

    public static ErrorResponse createErrorResponse(Throwable t, @Nullable HttpStatusCode status,
        ServerWebExchange exchange, MessageSource messageSource) {
        final ErrorResponse errorResponse;
        if (t instanceof ErrorResponse er) {
            errorResponse = er;
        } else {
            var responseStatusAnno =
                MergedAnnotations.from(t.getClass(), TYPE_HIERARCHY).get(ResponseStatus.class);
            if (status == null) {
                status = responseStatusAnno.getValue("code", HttpStatus.class)
                    .orElse(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            var type = EXCEPTION_TYPE_MAP.getOrDefault(t.getClass(), DEFAULT_TYPE);
            var detail = responseStatusAnno.getValue("reason", String.class)
                .orElseGet(t::getMessage);
            var builder = ErrorResponse.builder(t, status, detail)
                .type(URI.create(type));
            if (status.is5xxServerError()) {
                builder.detailMessageCode("problemDetail.internalServerError")
                    .titleMessageCode("problemDetail.title.internalServerError");
            }
            errorResponse = builder.build();
        }
        var problemDetail = errorResponse.updateAndGetBody(messageSource, getLocale(exchange));
        problemDetail.setInstance(exchange.getRequest().getURI());
        problemDetail.setProperty("requestId", exchange.getRequest().getId());
        problemDetail.setProperty("timestamp", Instant.now());
        return errorResponse;
    }

    public static Locale getLocale(ServerWebExchange exchange) {
        var locale = exchange.getLocaleContext().getLocale();
        return locale == null ? Locale.getDefault() : locale;
    }
}
