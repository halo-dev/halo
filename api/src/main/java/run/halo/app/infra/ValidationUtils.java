package run.halo.app.infra;

import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.server.ServerWebExchange;

@UtilityClass
public class ValidationUtils {
    public static final String NAME_REGEX =
        "^[a-z0-9]([-a-z0-9]*[a-z0-9])?(\\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*$";
    public static final Pattern NAME_PATTERN = Pattern.compile(NAME_REGEX);

    /**
     * {@code A-Z, a-z, 0-9, !@#$%^&*} are allowed.
     */
    public static final String PASSWORD_REGEX = "^[A-Za-z0-9!@#$%^&*]+$";

    public static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);

    /**
     * Validate the target object with given locale context.
     */
    public static BindingResult validate(Object target, String objectName,
        Validator validator, ServerWebExchange exchange) {
        BindingResult bindingResult = new BeanPropertyBindingResult(target, objectName);
        try {
            LocaleContextHolder.setLocaleContext(exchange.getLocaleContext());
            validator.validate(target, bindingResult);
            return bindingResult;
        } finally {
            LocaleContextHolder.resetLocaleContext();
        }
    }

    public static BindingResult validate(Object target, Validator validator,
        ServerWebExchange exchange) {
        return validate(target, "form", validator, exchange);
    }
}
