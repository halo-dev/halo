package run.halo.app.infra.exception;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.ProblemDetail;
import org.springframework.lang.Nullable;
import org.springframework.validation.BindingResult;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.util.BindErrorUtils;

public class RequestBodyValidationException extends ServerWebInputException {

    private final BindingResult bindingResult;

    public RequestBodyValidationException(BindingResult bindingResult) {
        super("Validation failure", null, null, null, null);
        this.bindingResult = bindingResult;
    }

    @Override
    public ProblemDetail updateAndGetBody(MessageSource messageSource, Locale locale) {
        var detail = super.updateAndGetBody(messageSource, locale);
        detail.setProperty("errors", collectAllErrors(messageSource, locale));
        return detail;
    }

    private List<String> collectAllErrors(MessageSource messageSource, Locale locale) {
        var globalErrors = resolveErrors(bindingResult.getGlobalErrors(), messageSource, locale);
        var fieldErrors = resolveErrors(bindingResult.getFieldErrors(), messageSource, locale);
        var errors = new ArrayList<String>(globalErrors.size() + fieldErrors.size());
        errors.addAll(globalErrors);
        errors.addAll(fieldErrors);
        return errors;
    }

    @Override
    public Object[] getDetailMessageArguments(MessageSource messageSource, Locale locale) {
        return new Object[] {
            resolveErrors(bindingResult.getGlobalErrors(), messageSource, locale),
            resolveErrors(bindingResult.getFieldErrors(), messageSource, locale)
        };
    }

    @Override
    public Object[] getDetailMessageArguments() {
        return new Object[] {
            resolveErrors(bindingResult.getGlobalErrors(), null, Locale.getDefault()),
            resolveErrors(bindingResult.getFieldErrors(), null, Locale.getDefault())
        };
    }

    private static List<String> resolveErrors(
        List<? extends MessageSourceResolvable> errors,
        @Nullable MessageSource messageSource,
        Locale locale) {
        return messageSource == null
            ? BindErrorUtils.resolve(errors).values().stream().toList()
            : BindErrorUtils.resolve(errors, messageSource, locale).values().stream().toList();
    }
}
