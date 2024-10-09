package run.halo.app.infra;

import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ValidationUtils {
    public static final String NAME_REGEX =
        "^[a-z0-9]([-a-z0-9]*[a-z0-9])?(\\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*$";
    public static final Pattern NAME_PATTERN = Pattern.compile(NAME_REGEX);

    /**
     * A-Z, a-z, 0-9, !@#$%^&* are allowed.
     */
    public static final String PASSWORD_REGEX = "^[A-Za-z0-9!@#$%^&*]+$";

    public static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);
}
