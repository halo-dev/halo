package run.halo.app.infra;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxProperties;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpRequest;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.Exceptions;
import run.halo.app.infra.properties.HaloProperties;

/**
 * Default implementation for getting external url from system config first, halo properties second.
 *
 * @author johnniang
 */
@Slf4j
@Component
class SystemConfigFirstExternalUrlSupplier implements ExternalUrlSupplier {

    private final HaloProperties haloProperties;

    private final WebFluxProperties webFluxProperties;

    private final SystemConfigurableEnvironmentFetcher systemConfigFetcher;

    @Nullable
    private URL externalUrl;

    public SystemConfigFirstExternalUrlSupplier(HaloProperties haloProperties,
        WebFluxProperties webFluxProperties,
        SystemConfigurableEnvironmentFetcher systemConfigFetcher) {
        this.haloProperties = haloProperties;
        this.webFluxProperties = webFluxProperties;
        this.systemConfigFetcher = systemConfigFetcher;
    }

    @EventListener
    void onExtensionInitialized(ExtensionInitializedEvent ignored) {
        refetchExternalUrl().ifPresent(externalUrl -> this.externalUrl = externalUrl);
    }

    @EventListener
    void onExternalUrlChanged(ExternalUrlChangedEvent event) {
        this.externalUrl = event.getExternalUrl();
    }

    Optional<URL> refetchExternalUrl() {
        return systemConfigFetcher.getBasic()
            .mapNotNull(SystemSetting.Basic::getExternalUrl)
            .filter(StringUtils::hasText)
            .mapNotNull(externalUrlString -> {
                try {
                    return URI.create(externalUrlString).toURL();
                } catch (MalformedURLException e) {
                    log.error("""
                        Cannot parse external URL {} from system config. Fallback to default \
                        external URL supplier from properties.\
                        """, externalUrlString, e);
                    // For continuing the application startup, we need to return null here.
                    return null;
                }
            })
            .blockOptional(Duration.ofSeconds(10));
    }

    @Override
    public URI get() {
        try {
            if (!haloProperties.isUseAbsolutePermalink()) {
                return URI.create(getBasePath());
            }
            if (externalUrl != null) {
                return externalUrl.toURI();
            }
            return haloProperties.getExternalUrl().toURI();
        } catch (URISyntaxException e) {
            throw Exceptions.propagate(e);
        }
    }

    @Override
    public URL getURL(HttpRequest request) {
        if (this.externalUrl != null) {
            return this.externalUrl;
        }
        var externalUrl = haloProperties.getExternalUrl();
        if (externalUrl != null) {
            return externalUrl;
        }
        try {
            externalUrl = request.getURI().resolve(getBasePath()).toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException("Cannot parse request URI to URL.", e);
        }
        return externalUrl;
    }

    @Nullable
    @Override
    public URL getRaw() {
        return externalUrl != null ? externalUrl : haloProperties.getExternalUrl();
    }

    private String getBasePath() {
        var basePath = webFluxProperties.getBasePath();
        if (!StringUtils.hasText(basePath)) {
            basePath = "/";
        }
        return basePath;
    }
}
