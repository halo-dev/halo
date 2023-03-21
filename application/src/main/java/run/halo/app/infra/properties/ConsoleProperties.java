package run.halo.app.infra.properties;

import jakarta.validation.Valid;
import java.net.URI;
import lombok.Data;

@Data
public class ConsoleProperties {

    private String location = "classpath:/console/";

    @Valid
    private ProxyProperties proxy = new ProxyProperties();

    @Data
    public static class ProxyProperties {

        /**
         * Console endpoint in development environment to be proxied. e.g.: http://localhost:8090/
         */
        private URI endpoint;

        /**
         * Indicates if the proxy behaviour is enabled. Default is false
         */
        private boolean enabled = false;
    }
}
