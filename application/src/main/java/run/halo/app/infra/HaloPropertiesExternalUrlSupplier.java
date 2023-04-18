package run.halo.app.infra;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxProperties;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import run.halo.app.infra.properties.HaloProperties;

/**
 * Default implementation for getting external url from halo properties.
 *
 * @author johnniang
 */
@Component
public class HaloPropertiesExternalUrlSupplier implements ExternalUrlSupplier {

    private final HaloProperties haloProperties;

    private final WebFluxProperties webFluxProperties;

    public HaloPropertiesExternalUrlSupplier(HaloProperties haloProperties,
        WebFluxProperties webFluxProperties) {
        this.haloProperties = haloProperties;
        this.webFluxProperties = webFluxProperties;
    }

    @Override
    public URI get() {
        if (!haloProperties.isUseAbsolutePermalink()) {
            return URI.create(getBasePath());
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
                externalUrl = request.getURI().resolve(getBasePath()).toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException("Cannot parse request URI to URL.", e);
            }
        }
        return externalUrl;
    }

    private String getBasePath() {
        var basePath = webFluxProperties.getBasePath();
        if (!StringUtils.hasText(basePath)) {
            basePath = "/";
        }
        return basePath;
    }
}
