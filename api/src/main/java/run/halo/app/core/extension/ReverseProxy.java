package run.halo.app.core.extension;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/**
 * The reverse proxy custom resource is used to configure a path to proxy it to a directory or file.
 *
 * <p>HTTP proxy may be added in the future.
 *
 * @author guqing
 * @since 2.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@GVK(
        group = "plugin.halo.run",
        kind = "ReverseProxy",
        version = "v1alpha1",
        plural = "reverseproxies",
        singular = "reverseproxy")
public class ReverseProxy extends AbstractExtension {
    /** Path mapping rules served by this reverse proxy resource. */
    private List<ReverseProxyRule> rules;

    /** A path mapping from a public request path to a file provider. */
    public record ReverseProxyRule(String path, FileReverseProxyProvider file) {}

    /** File provider that resolves a request to a plugin or theme file location. */
    public record FileReverseProxyProvider(String directory, String filename) {}
}
