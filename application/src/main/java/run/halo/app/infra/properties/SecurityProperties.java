package run.halo.app.infra.properties;

import static org.springframework.security.web.server.header.ReferrerPolicyServerHttpHeadersWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN;

import lombok.Data;
import org.springframework.lang.NonNull;
import org.springframework.security.web.server.header.ReferrerPolicyServerHttpHeadersWriter.ReferrerPolicy;
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter.Mode;
import org.springframework.validation.Errors;
import run.halo.app.infra.ValidationUtils;

@Data
public class SecurityProperties {

    private final Initializer initializer = new Initializer();

    private final FrameOptions frameOptions = new FrameOptions();

    private final ReferrerOptions referrerOptions = new ReferrerOptions();

    @Data
    public static class FrameOptions {

        private boolean disabled;

        private Mode mode = Mode.SAMEORIGIN;
    }

    @Data
    public static class ReferrerOptions {

        private ReferrerPolicy policy = STRICT_ORIGIN_WHEN_CROSS_ORIGIN;

    }

    @Data
    public static class Initializer {

        private boolean disabled;

        private String superAdminUsername = "admin";

        private String superAdminPassword;

        static void validateUsername(@NonNull Initializer initializer, @NonNull Errors errors) {
            if (initializer.isDisabled() || ValidationUtils.validateName(
                initializer.getSuperAdminUsername())) {
                return;
            }
            errors.rejectValue("security.initializer.superAdminUsername",
                "initializer.superAdminUsername.invalid",
                ValidationUtils.NAME_VALIDATION_MESSAGE);
        }
    }
}
