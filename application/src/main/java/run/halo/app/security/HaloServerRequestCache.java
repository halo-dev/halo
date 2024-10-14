package run.halo.app.security;

import static org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers;

import java.net.URI;
import java.util.Collections;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.web.server.savedrequest.WebSessionServerRequestCache;
import org.springframework.security.web.server.util.matcher.AndServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.MediaTypeServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

/**
 * Halo server request cache implementation for saving redirect URI from query.
 *
 * @author johnniang
 */
public class HaloServerRequestCache extends WebSessionServerRequestCache {

    /**
     * Currently, we have no idea to customize the sessionAttributeName in
     * WebSessionServerRequestCache, so we have to copy the attr into here.
     */
    private static final String DEFAULT_SAVED_REQUEST_ATTR = "SPRING_SECURITY_SAVED_REQUEST";

    private static final String REDIRECT_URI_QUERY = "redirect_uri";

    private final String sessionAttrName = DEFAULT_SAVED_REQUEST_ATTR;

    public HaloServerRequestCache() {
        super();
        setSaveRequestMatcher(createDefaultRequestMatcher());
    }

    @Override
    public Mono<Void> saveRequest(ServerWebExchange exchange) {
        var redirectUriQuery = exchange.getRequest().getQueryParams().getFirst(REDIRECT_URI_QUERY);
        if (StringUtils.isNotBlank(redirectUriQuery)) {
            // the query value is decoded, so we don't need to decode it again
            var redirectUri = URI.create(redirectUriQuery);
            return saveRedirectUri(exchange, redirectUri);
        }
        return super.saveRequest(exchange);
    }

    @Override
    public Mono<URI> getRedirectUri(ServerWebExchange exchange) {
        return super.getRedirectUri(exchange);
    }

    @Override
    public Mono<ServerHttpRequest> removeMatchingRequest(ServerWebExchange exchange) {
        return getRedirectUri(exchange)
            .flatMap(redirectUri -> {
                if (redirectUri.getFragment() != null) {
                    var redirectUriInApplication =
                        uriInApplication(exchange.getRequest(), redirectUri, false);
                    var uriInApplication =
                        uriInApplication(exchange.getRequest(), exchange.getRequest().getURI());
                    // compare the path and query only
                    if (!Objects.equals(redirectUriInApplication, uriInApplication)) {
                        return Mono.empty();
                    }
                    // remove the exchange
                    return exchange.getSession().map(WebSession::getAttributes)
                        .doOnNext(attributes -> attributes.remove(this.sessionAttrName))
                        .thenReturn(exchange.getRequest());
                }
                return super.removeMatchingRequest(exchange);
            });
    }

    private Mono<Void> saveRedirectUri(ServerWebExchange exchange, URI redirectUri) {
        var redirectUriInApplication = uriInApplication(exchange.getRequest(), redirectUri);
        return exchange.getSession()
            .map(WebSession::getAttributes)
            .doOnNext(attributes -> attributes.put(this.sessionAttrName, redirectUriInApplication))
            .then();
    }

    private static String uriInApplication(ServerHttpRequest request, URI uri) {
        return uriInApplication(request, uri, true);
    }

    private static String uriInApplication(
        ServerHttpRequest request, URI uri, boolean appendFragment
    ) {
        var path = RequestPath.parse(uri, request.getPath().contextPath().value());
        var query = uri.getRawQuery();
        var fragment = uri.getRawFragment();
        return path.pathWithinApplication().value()
            + (query == null ? "" : "?" + query)
            + (fragment == null || !appendFragment ? "" : "#" + fragment);
    }

    private static ServerWebExchangeMatcher createDefaultRequestMatcher() {
        var get = pathMatchers(HttpMethod.GET, "/**");
        var notFavicon = new NegatedServerWebExchangeMatcher(
            pathMatchers(
                "/favicon.*", "/login/**", "/signup/**", "/password-reset/**", "/challenges/**"
            ));
        var html = new MediaTypeServerWebExchangeMatcher(MediaType.TEXT_HTML);
        html.setIgnoredMediaTypes(Collections.singleton(MediaType.ALL));
        return new AndServerWebExchangeMatcher(get, notFavicon, html);
    }

}
