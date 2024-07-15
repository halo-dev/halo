package run.halo.app.security.authentication.rememberme;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.rememberme.CookieTheftException;
import org.springframework.security.web.authentication.rememberme.InvalidCookieException;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationException;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * <p>{@link RememberMeServices} implementation based on Barry Jaspan's <a href=
 * "https://web.archive.org/web/20180819014446/http://jaspan
 * .com/improved_persistent_login_cookie_best_practice">Improved
 * Persistent Login Cookie Best Practice</a>.</p>
 * <p>There is a slight modification to the described approach, in that the username is not
 * stored as part of the cookie but obtained from the persistent store via an
 * implementation of {@link PersistentTokenRepository}. The latter should place a unique
 * constraint on the series identifier, so that it is impossible for the same identifier
 * to be allocated to two different users.</p>
 * <p>User management such as changing passwords, removing users and setting user status
 * should be combined with maintenance of the user's persistent tokens.</p>
 * <p>
 * Note that while this class will use the date a token was created to check whether a
 * presented cookie is older than the configured <tt>tokenValiditySeconds</tt> property
 * and deny authentication in this case, it will not delete these tokens from storage. A
 * suitable batch process should be run periodically to remove expired tokens from the
 * database.
 * </p>
 *
 * @author guqing
 * @see
 * <a href="https://github.com/spring-projects/spring-security/blob/902aff451f2f4d3f2ce44659bbef3645bf320ece/web/src/main/java/org/springframework/security/web/authentication/rememberme/PersistentTokenBasedRememberMeServices.java#L61">PersistentTokenBasedRememberMeServices</a>
 * @since 2.17.0
 */
@Slf4j
@Setter
@Component
public class PersistentTokenBasedRememberMeServices extends TokenBasedRememberMeServices
    implements RememberMeServices {

    public static final String REMEMBER_ME_SERIES_REQUEST_NAME = "remember-me-series";

    public static final int DEFAULT_SERIES_LENGTH = 16;

    public static final int DEFAULT_TOKEN_LENGTH = 16;

    private final SecureRandom random;

    private final int seriesLength = DEFAULT_SERIES_LENGTH;

    private final int tokenLength = DEFAULT_TOKEN_LENGTH;

    private final PersistentRememberMeTokenRepository tokenRepository;

    public PersistentTokenBasedRememberMeServices(
        CookieSignatureKeyResolver cookieSignatureKeyResolver,
        ReactiveUserDetailsService userDetailsService,
        RememberMeCookieResolver rememberMeCookieResolver,
        PersistentRememberMeTokenRepository tokenRepository) {
        super(cookieSignatureKeyResolver, userDetailsService, rememberMeCookieResolver);
        this.random = new SecureRandom();
        this.tokenRepository = tokenRepository;
    }

    @Override
    protected Mono<UserDetails> processAutoLoginCookie(String[] cookieTokens,
        ServerWebExchange exchange) {
        if (cookieTokens.length != 2) {
            throw new InvalidCookieException(
                "Cookie token did not contain " + 2 + " tokens, but contained '"
                    + Arrays.asList(cookieTokens) + "'");
        }
        String presentedSeries = cookieTokens[0];
        String presentedToken = cookieTokens[1];
        return this.tokenRepository.getTokenForSeries(presentedSeries)
            // No series match, so we can't authenticate using this cookie
            .switchIfEmpty(Mono.error(new RememberMeAuthenticationException(
                "No persistent token found for series id: " + presentedSeries))
            )
            .flatMap(token -> {
                // We have a match for this user/series combination
                if (!presentedToken.equals(token.getTokenValue())) {
                    // Token doesn't match series value. Delete all logins for this user and throw
                    // an exception to warn them.
                    return this.tokenRepository.removeUserTokens(token.getUsername())
                        .then(Mono.error(new CookieTheftException(
                            "Invalid remember-me token (Series/token) mismatch. Implies previous "
                                + "cookie theft"
                                + " attack.")));
                }

                if (isTokenExpired(token)) {
                    return Mono.error(
                        new RememberMeAuthenticationException("Remember-me login has expired"));
                }

                // Token also matches, so login is valid. Update the token value, keeping the
                // *same* series number.
                log.debug("Refreshing persistent login token for user '{}', series '{}'",
                    token.getUsername(), token.getSeries());
                var newToken = new PersistentRememberMeToken(token.getUsername(), token.getSeries(),
                    token.getTokenValue(), new Date());
                return Mono.just(newToken);
            })
            .flatMap(newToken -> updateToken(newToken)
                .doOnSuccess(unused -> addCookie(newToken, exchange))
                .onErrorMap(ex -> {
                    log.error("Failed to update token: ", ex);
                    return new RememberMeAuthenticationException(
                        "Autologin failed due to data access problem");
                })
                .then(getUserDetailsService().findByUsername(newToken.getUsername()))
            );
    }

    private boolean isTokenExpired(PersistentRememberMeToken token) {
        return isTokenExpired(token.getDate().getTime() + getTokenValidityMillis());
    }

    private Mono<Void> updateToken(PersistentRememberMeToken newToken) {
        return this.tokenRepository.updateToken(newToken.getSeries(),
            newToken.getTokenValue(), dateToInstant(newToken.getDate()));
    }

    Instant dateToInstant(Date date) {
        return Instant.ofEpochMilli(date.getTime());
    }

    /**
     * Creates a new persistent login token with a new series number, stores the data in
     * the persistent token repository and adds the corresponding cookie to the response.
     */
    @Override
    protected Mono<Void> onLoginSuccess(ServerWebExchange exchange,
        Authentication successfulAuthentication) {
        String username = successfulAuthentication.getName();
        log.debug("Creating new persistent login for user {}", username);
        PersistentRememberMeToken persistentToken =
            new PersistentRememberMeToken(username, generateSeriesData(),
                generateTokenData(), new Date());
        return this.tokenRepository.createNewToken(persistentToken)
            .doOnSuccess(unused -> addCookie(persistentToken, exchange))
            .onErrorResume(Throwable.class, ex -> {
                log.error("Failed to save persistent token ", ex);
                return Mono.empty();
            });
    }

    @Override
    protected Mono<Void> onLogout(WebFilterExchange exchange, Authentication authentication) {
        if (authentication != null) {
            return this.tokenRepository.removeUserTokens(authentication.getName());
        }
        return Mono.empty();
    }

    private void addCookie(PersistentRememberMeToken token, ServerWebExchange exchange) {
        setCookie(new String[] {token.getSeries(), token.getTokenValue()}, exchange);
        exchange.getAttributes().put(REMEMBER_ME_SERIES_REQUEST_NAME, token.getSeries());
    }

    protected String generateSeriesData() {
        byte[] newSeries = new byte[this.seriesLength];
        this.random.nextBytes(newSeries);
        return new String(Base64.getEncoder().encode(newSeries));
    }

    protected String generateTokenData() {
        byte[] newToken = new byte[this.tokenLength];
        this.random.nextBytes(newToken);
        return new String(Base64.getEncoder().encode(newToken));
    }

    private long getTokenValidityMillis() {
        return rememberMeCookieResolver.getCookieMaxAge().toMillis();
    }
}
