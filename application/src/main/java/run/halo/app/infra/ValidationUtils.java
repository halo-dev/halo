package run.halo.app.infra;

import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

@UtilityClass
public class ValidationUtils {
    public static final Pattern NAME_PATTERN =
        Pattern.compile("^[a-z0-9]([-a-z0-9]*[a-z0-9])?(\\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*$");

    public static final String NAME_VALIDATION_MESSAGE = """
        Super administrator username must be a valid subdomain name, the name must:
        1. contain no more than 63 characters
        2. contain only lowercase alphanumeric characters, '-' or '.'
        3. start with an alphanumeric character
        4. end with an alphanumeric character
        """;

    /**
     * Validates the name.
     *
     * @param name name for validation
     * @return true if the name is valid
     */
    public static boolean validateName(String name) {
        if (StringUtils.isBlank(name)) {
            return false;
        }
        boolean matches = NAME_PATTERN.matcher(name).matches();
        return matches && name.length() <= 63;
    }
}
