package run.halo.app.infra.properties;

import static org.springframework.security.web.server.header.ReferrerPolicyServerHttpHeadersWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN;

import lombok.Data;
import org.springframework.security.web.server.header.ReferrerPolicyServerHttpHeadersWriter.ReferrerPolicy;

@Data
public class SecurityProperties {

    private final Initializer initializer = new Initializer();

    private final ReferrerOptions referrerOptions = new ReferrerOptions();

    @Data
    public static class ReferrerOptions {

        private ReferrerPolicy policy = STRICT_ORIGIN_WHEN_CROSS_ORIGIN;

    }

    @Data
    public static class Initializer {

        private boolean disabled;

        private String superAdminUsername = "admin";

        private String superAdminPassword;

    }

}
