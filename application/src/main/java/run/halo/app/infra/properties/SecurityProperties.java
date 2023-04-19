package run.halo.app.infra.properties;

import static org.springframework.security.web.server.header.ReferrerPolicyServerHttpHeadersWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.security.web.server.header.ReferrerPolicyServerHttpHeadersWriter.ReferrerPolicy;
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter.Mode;

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

        @NotBlank(message = "Super administrator username must not be blank")
        @Pattern(regexp = "^[a-z0-9]([-a-z0-9]*[a-z0-9])?(\\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*$",
            message = """
                Super administrator username must be a valid subdomain name, the name must:
                1. contain no more than 63 characters
                2. contain only lowercase alphanumeric characters, '-' or '.'
                3. start with an alphanumeric character
                4. end with an alphanumeric character
                """)
        @Size(max = 63, message = "Must be less than {max} characters")
        private String superAdminUsername = "admin";

        private String superAdminPassword;
    }

}
