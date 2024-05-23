package run.halo.app.infra.properties;

import static org.springframework.security.web.server.header.ReferrerPolicyServerHttpHeadersWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN;

import java.time.Duration;
import lombok.Data;
import org.springframework.security.web.server.header.ReferrerPolicyServerHttpHeadersWriter.ReferrerPolicy;
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter.Mode;

@Data
public class SecurityProperties {

    private final FrameOptions frameOptions = new FrameOptions();

    private final ReferrerOptions referrerOptions = new ReferrerOptions();

    private final RememberMeOptions rememberMe = new RememberMeOptions();

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
    public static class RememberMeOptions {
        private Duration tokenValidity = Duration.ofDays(14);
    }
}
