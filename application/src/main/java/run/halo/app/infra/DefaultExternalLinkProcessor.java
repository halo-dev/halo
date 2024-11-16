package run.halo.app.infra;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.reactive.ServerWebExchangeContextFilter;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import run.halo.app.infra.utils.PathUtils;

/**
 * Default implementation of {@link ExternalLinkProcessor}.
 *
 * @author guqing
 * @since 2.9.0
 */
@Component
@RequiredArgsConstructor
public class DefaultExternalLinkProcessor implements ExternalLinkProcessor {
    private final ExternalUrlSupplier externalUrlSupplier;

    @Override
    public String processLink(String link) {
        var externalLink = externalUrlSupplier.getRaw();
        if (StringUtils.isBlank(link) || externalLink == null || PathUtils.isAbsoluteUri(link)) {
            return link;
        }
        return append(externalLink.toString(), link);
    }

    @Override
    public Mono<URI> processLink(URI uri) {
        if (uri.isAbsolute()) {
            return Mono.just(uri);
        }
        return Mono.deferContextual(contextView -> Mono.fromSupplier(
            () -> ServerWebExchangeContextFilter.getExchange(contextView)
                .map(exchange -> externalUrlSupplier.getURL(exchange.getRequest()))
                .or(() -> Optional.ofNullable(externalUrlSupplier.getRaw()))
                .map(externalUrl -> {
                    try {
                        var uriComponents = UriComponentsBuilder.fromUriString(uri.toASCIIString())
                            .build(true);
                        return UriComponentsBuilder.fromUri(externalUrl.toURI())
                            .pathSegment(uriComponents.getPathSegments().toArray(new String[0]))
                            .queryParams(uriComponents.getQueryParams())
                            .fragment(uriComponents.getFragment())
                            .build(true)
                            .toUri();
                    } catch (URISyntaxException e) {
                        // should never happen
                        return uri;
                    }
                })
                .orElse(uri)
        ));
    }

    String append(String externalLink, String link) {
        return StringUtils.removeEnd(externalLink, "/")
            + StringUtils.prependIfMissing(link, "/");
    }
}
