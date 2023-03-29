package run.halo.app.infra.properties;

import lombok.Data;
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter.Mode;

@Data
public class SecurityProperties {

    private final Initializer initializer = new Initializer();

    private final FrameOptions frameOptions = new FrameOptions();

    @Data
    public static class FrameOptions {

        private boolean disabled;

        private Mode mode = Mode.SAMEORIGIN;
    }

    @Data
    public static class Initializer {

        private boolean disabled;

        private String superAdminUsername = "admin";

        private String superAdminPassword;

    }

}
