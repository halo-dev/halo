package run.halo.app.infra.properties;

import java.nio.file.Path;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Properties for Unix Domain Socket (UDS) support.
 *
 * When enabled, Halo will additionally listen on a Unix Domain Socket path,
 * allowing reverse proxies (e.g., Nginx) to forward requests via UDS,
 * without interfering with the existing TCP port listener.
 *
 * @author drin-love
 */
@Data
@ConfigurationProperties(prefix = "halo.uds")
@Validated
public class UdsProperties {

    /**
     * Whether to enable Unix Domain Socket support.
     */
    private boolean enabled = false;

    /**
     * The path of the Unix Domain Socket file.
     * If not specified, defaults to {@code ${halo.work-dir}/halo.sock}.
     */
    private Path path;

}
