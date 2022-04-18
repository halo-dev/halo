package run.halo.app.identity.authentication;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * A {@code Filter} that associates the {@link ProviderContext} to the
 * {@link ProviderContextHolder}.
 *
 * @author guqing
 * @see ProviderContext
 * @see ProviderContextHolder
 * @see ProviderSettings
 * @since 2.0.0
 */
public final class ProviderContextFilter extends OncePerRequestFilter {
    private final ProviderSettings providerSettings;

    /**
     * Constructs a {@code ProviderContextFilter} using the provided parameters.
     *
     * @param providerSettings the provider settings
     */
    public ProviderContextFilter(ProviderSettings providerSettings) {
        Assert.notNull(providerSettings, "providerSettings cannot be null");
        this.providerSettings = providerSettings;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain)
        throws ServletException, IOException {

        try {
            ProviderContext providerContext = new ProviderContext(
                this.providerSettings, () -> resolveIssuer(this.providerSettings, request));
            ProviderContextHolder.setProviderContext(providerContext);
            filterChain.doFilter(request, response);
        } finally {
            ProviderContextHolder.resetProviderContext();
        }
    }

    private static String resolveIssuer(ProviderSettings providerSettings,
        HttpServletRequest request) {
        return providerSettings.getIssuer() != null
            ? providerSettings.getIssuer() : getContextPath(request);
    }

    private static String getContextPath(HttpServletRequest request) {
        return UriComponentsBuilder.fromHttpUrl(UrlUtils.buildFullRequestUrl(request))
            .replacePath(request.getContextPath())
            .replaceQuery(null)
            .fragment(null)
            .build()
            .toUriString();
    }
}
