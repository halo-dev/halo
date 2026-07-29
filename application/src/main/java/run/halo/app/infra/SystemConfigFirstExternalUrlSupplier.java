package run.halo.app.infra;

import java.net.IDN;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.webflux.autoconfigure.WebFluxProperties;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpRequest;
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

    private final SystemConfigFetcher systemConfigFetcher;

    @Nullable
    private URL externalUrl;

    public SystemConfigFirstExternalUrlSupplier(
            HaloProperties haloProperties,
            WebFluxProperties webFluxProperties,
            SystemConfigFetcher systemConfigFetcher) {
        this.haloProperties = haloProperties;
        this.webFluxProperties = webFluxProperties;
        this.systemConfigFetcher = systemConfigFetcher;
        this.externalUrl = toSafeUrl(haloProperties.getExternalUrl());
    }

    @EventListener
    void onExtensionInitialized(ExtensionInitializedEvent ignored) {
        refetchExternalUrl().ifPresent(externalUrl -> this.externalUrl = toSafeUrl(externalUrl));
    }

    @EventListener
    void onExternalUrlChanged(ExternalUrlChangedEvent event) {
        this.externalUrl = toSafeUrl(event.getExternalUrl());
    }

    Optional<URL> refetchExternalUrl() {
        return systemConfigFetcher
                .getBasic()
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
            return toSafeUrl(externalUrl);
        }
        try {
            return toSafeUrl(request.getURI().resolve(getBasePath()).toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException("Cannot parse request URI to URL.", e);
        }
    }

    @Nullable
    @Override
    public URL getRaw() {
        var raw = externalUrl != null ? externalUrl : haloProperties.getExternalUrl();
        return toSafeUrl(raw);
    }

    @Nullable
    private URL toSafeUrl(@Nullable URL url) {
        if (url == null) {
            return null;
        }
        var host = url.getHost();
        if (host == null) {
            return url;
        }
        // Some JVMs percent-encode non-ASCII hosts; decode first, then convert to Punycode
        String asciiHost;
        try {
            var decodedHost = URLDecoder.decode(host, StandardCharsets.UTF_8);
            asciiHost = IDN.toASCII(decodedHost);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to normalize URL host {}", url, e);
            return url;
        }
        if (asciiHost.equals(host)) {
            return url;
        }
        // Rebuild via URI to preserve fragment and userinfo
        try {
            return new URI(
                            url.getProtocol(),
                            url.getUserInfo(),
                            asciiHost,
                            url.getPort(),
                            url.getPath(),
                            url.getQuery(),
                            url.getRef())
                    .toURL();
        } catch (URISyntaxException | MalformedURLException e) {
            log.warn("Failed to rebuild normalized URL {}", url, e);
            return url;
        }
    }

    private String getBasePath() {
        var basePath = webFluxProperties.getBasePath();
        if (!StringUtils.hasText(basePath)) {
            basePath = "/";
        }
        return basePath;
    }
}
