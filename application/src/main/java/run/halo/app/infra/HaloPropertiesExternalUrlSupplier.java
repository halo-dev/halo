package run.halo.app.infra;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Component;
import run.halo.app.infra.properties.HaloProperties;

/**
 * Default implementation for getting external url from halo properties.
 *
 * @author johnniang
 */
@Component
public class HaloPropertiesExternalUrlSupplier implements ExternalUrlSupplier {

    private final HaloProperties haloProperties;

    public HaloPropertiesExternalUrlSupplier(HaloProperties haloProperties) {
        this.haloProperties = haloProperties;
    }

    @Override
    public URI get() {
        if (!haloProperties.isUseAbsolutePermalink()) {
            return URI.create("/");
        }

        try {
            return haloProperties.getExternalUrl().toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public URL getURL(HttpRequest request) {
        var externalUrl = haloProperties.getExternalUrl();
        if (externalUrl == null) {
            try {
                externalUrl = request.getURI().toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException("Cannot parse request URI to URL.", e);
            }
        }
        return externalUrl;
    }
}
