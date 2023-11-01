package run.halo.app.infra.properties;

import java.net.URI;
import lombok.Data;

@Data
public class ProxyProperties {

    /**
     * Console endpoint in development environment to be proxied. e.g.: http://localhost:8090/
     */
    private URI endpoint;

    /**
     * Indicates if the proxy behaviour is enabled. Default is false
     */
    private boolean enabled = false;
}
