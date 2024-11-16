package run.halo.app.infra.exception.handlers;

import static run.halo.app.infra.exception.Exceptions.createErrorResponse;

import java.util.Map;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.context.MessageSource;
import org.springframework.web.reactive.function.server.ServerRequest;

/**
 * See {@link DefaultErrorAttributes} for more.
 *
 * @author johnn
 */
public class ProblemDetailErrorAttributes extends DefaultErrorAttributes {

    private final MessageSource messageSource;

    public ProblemDetailErrorAttributes(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request,
        ErrorAttributeOptions options) {
        final var errAttributes = super.getErrorAttributes(request, options);
        var error = getError(request);
        var errorResponse = createErrorResponse(error, null, request.exchange(), messageSource);
        errAttributes.put("error", errorResponse.getBody());
        return errAttributes;
    }

}
