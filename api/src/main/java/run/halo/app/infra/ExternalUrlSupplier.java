package run.halo.app.infra;

import java.net.URI;
import java.net.URL;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import org.springframework.http.HttpRequest;

/**
 * Represents a supplier of external url configuration.
 *
 * @author johnniang
 */
public interface ExternalUrlSupplier extends Supplier<URI> {

    /**
     * Gets URI according to external URL and use-absolute-permalink properties.
     *
     * @return URI "/" returned if use-absolute-permalink is false. Or external URL will be
     * returned.(never null)
     */
    @Override
    URI get();

    /**
     * Gets URL according to external URL and server request URL.
     *
     * @param request represents an HTTP request message, consisting of a method and a URI.
     * @return External URL will be return if it is provided, or request URI will be returned.
     * (never null)
     */
    URL getURL(HttpRequest request);

    /**
     * Gets user-configured external URL from <code>HaloProperties#getExternalUrl()</code>.
     *
     * @return user-configured external URL or null if it is not provided.
     */
    @Nullable
    URL getRaw();
}
