package run.halo.app.security;

import static org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers;

import java.net.URI;
import java.util.Collections;
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
        return super.removeMatchingRequest(exchange);
    }

    private Mono<Void> saveRedirectUri(ServerWebExchange exchange, URI redirectUri) {
        var requestPath = exchange.getRequest().getPath();
        var redirectPath = RequestPath.parse(redirectUri, requestPath.contextPath().value());
        var query = redirectUri.getRawQuery();
        var finalRedirect =
            redirectPath.pathWithinApplication() + (query == null ? "" : "?" + query);
        return exchange.getSession()
            .map(WebSession::getAttributes)
            .doOnNext(attributes -> attributes.put(this.sessionAttrName, finalRedirect))
            .then();
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
