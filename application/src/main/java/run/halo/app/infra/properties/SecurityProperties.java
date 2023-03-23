package run.halo.app.infra.properties;

import lombok.Data;

@Data
public class SecurityProperties {

    private final Initializer initializer = new Initializer();

    @Data
    public static class Initializer {

        private boolean disabled;

        private String superAdminUsername = "admin";

        private String superAdminPassword;

    }

}
