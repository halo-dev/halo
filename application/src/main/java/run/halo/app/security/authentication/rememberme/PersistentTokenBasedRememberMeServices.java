package run.halo.app.security.authentication.rememberme;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.rememberme.CookieTheftException;
import org.springframework.security.web.authentication.rememberme.InvalidCookieException;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationException;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.RememberMeToken;
import run.halo.app.extension.Metadata;
import run.halo.app.security.LoginParameterRequestCache;
import run.halo.app.security.SecurityConstant;
import run.halo.app.security.device.DeviceService;

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

    /**
     * Grace period during which the previous token value is still accepted after rotation.
     * This prevents false-positive cookie theft detection when concurrent requests race
     * on token rotation.
     */
    private static final Duration TOKEN_GRACE_PERIOD = Duration.ofSeconds(10);

    private final SecureRandom random;

    private final int seriesLength = DEFAULT_SERIES_LENGTH;

    private final int tokenLength = DEFAULT_TOKEN_LENGTH;

    private final PersistentRememberMeTokenRepository tokenRepository;

    private final DeviceService deviceService;

    private Clock clock = Clock.systemUTC();

    public PersistentTokenBasedRememberMeServices(
        CookieSignatureKeyResolver cookieSignatureKeyResolver,
        ReactiveUserDetailsService userDetailsService,
        RememberMeCookieResolver rememberMeCookieResolver,
        PersistentRememberMeTokenRepository tokenRepository,
        LoginParameterRequestCache parameterRequestCache,
        DeviceService deviceService) {
        super(
            cookieSignatureKeyResolver,
            userDetailsService,
            rememberMeCookieResolver,
            parameterRequestCache
        );
        this.random = new SecureRandom();
        this.tokenRepository = tokenRepository;
        this.deviceService = deviceService;
        setParameterName(SecurityConstant.REMEMBER_ME_PARAMETER_NAME);
    }

    @Override
    protected Mono<UserDetails> processAutoLoginCookie(String[] cookieTokens,
        ServerWebExchange exchange) {
        if (cookieTokens.length != 2) {
            return Mono.error(new InvalidCookieException(
                "Cookie token did not contain %d tokens, but contained '%s'".formatted(
                    2, Arrays.asList(cookieTokens))
            ));
        }
        var presentedSeries = cookieTokens[0];
        var presentedToken = cookieTokens[1];
        return this.tokenRepository.getTokenForSeries(presentedSeries)
            .switchIfEmpty(Mono.error(() -> new RememberMeAuthenticationException(
                "No persistent token found for series id: " + presentedSeries))
            )
            .delayUntil(token -> validateDevice(exchange, token))
            .delayUntil(token -> {
                if (!Objects.equals(token.getSpec().getTokenValue(), presentedToken)) {
                    if (isTokenStolen(token, presentedToken)) {
                        log.error("Possible cookie theft detected for user '{}', series '{}'",
                            token.getSpec().getUsername(), token.getSpec().getSeries()
                        );
                        return this.tokenRepository.removeUserTokens(token.getSpec().getUsername())
                            .then(Mono.error(() -> new CookieTheftException("""
                                Invalid remember-me token (Series/token) mismatch. \
                                Implies previous cookie theft attack."""
                            )));
                    }
                    log.debug("Token mismatch but within grace period for user '{}', series '{}'",
                        token.getSpec().getUsername(), token.getSpec().getSeries()
                    );
                }
                if (isTokenExpired(token)) {
                    return Mono.error(new InvalidCookieException("Remember-me login has expired"));
                }
                return Mono.empty();
            })
            .flatMap(token -> {
                if (!Objects.equals(token.getSpec().getTokenValue(), presentedToken)) {
                    return Mono.just(token);
                }
                log.debug("Token value will be rotated for series '{}'",
                    token.getSpec().getSeries());
                token.getSpec().setPreviousTokenValue(presentedToken);
                token.getSpec().setTokenValue(generateTokenData());
                token.getSpec().setLastUsed(clock.instant());
                return tokenRepository.updateToken(token)
                    .doOnNext(updated -> {
                        log.debug("Remember me token {} rotated successfully",
                            updated.getSpec().getSeries());
                        addCookie(updated, exchange);
                    })
                    .onErrorReturn(OptimisticLockingFailureException.class, token);
            })
            .flatMap(t -> getUserDetailsService().findByUsername(t.getSpec().getUsername()));
    }

    private Mono<Void> validateDevice(ServerWebExchange exchange, RememberMeToken token) {
        return deviceService.resolveCurrentDevice(exchange)
            .switchIfEmpty(Mono.error(() -> new RememberMeAuthenticationException(
                "Unable to determine device for remember-me authentication"
            )))
            .filter(d -> Objects.equals(
                d.getSpec().getRememberMeSeriesId(),
                token.getSpec().getSeries()
            ))
            .switchIfEmpty(Mono.error(() -> new RememberMeAuthenticationException(
                "Remember-me series ID does not match current device's series ID"
            )))
            .then();
    }


    private boolean isTokenStolen(RememberMeToken token, String presentedToken) {
        var lastUsed = Optional.ofNullable(token.getSpec().getLastUsed())
            .orElseGet(() -> token.getMetadata().getCreationTimestamp());
        var now = clock.instant();
        return now.isAfter(lastUsed.plus(TOKEN_GRACE_PERIOD))
            || !Objects.equals(presentedToken, token.getSpec().getPreviousTokenValue());
    }

    private boolean isTokenExpired(RememberMeToken token) {
        var lastUsed = Optional.ofNullable(token.getSpec().getLastUsed())
            .orElseGet(() -> token.getMetadata().getCreationTimestamp());
        var now = clock.instant();
        return now.isAfter(lastUsed.plus(rememberMeCookieResolver.getCookieMaxAge()));
    }

    /**
     * Creates a new persistent login token with a new series number, stores the data in
     * the persistent token repository and adds the corresponding cookie to the response.
     */
    @Override
    protected Mono<Void> onLoginSuccess(ServerWebExchange exchange,
        Authentication successfulAuthentication) {
        var username = successfulAuthentication.getName();
        log.debug("Creating new persistent login for user {}", username);
        var t = new RememberMeToken();
        t.setMetadata(new Metadata());
        t.setSpec(new RememberMeToken.Spec());
        var seriesId = generateSeriesData();
        var tokenValue = generateTokenData();
        t.getMetadata().setGenerateName(username + '-');
        t.getSpec().setLastUsed(clock.instant());
        t.getSpec().setSeries(seriesId);
        t.getSpec().setTokenValue(tokenValue);
        t.getSpec().setUsername(username);
        return this.tokenRepository.createNewToken(t)
            .doOnNext(created -> {
                log.debug("Remember-me token {} created successfully",
                    created.getSpec().getSeries());
                addCookie(created, exchange);
            })
            .doOnError(e -> log.error("Remember-me token {} could not be created", seriesId, e))
            .onErrorComplete()
            .then();
    }

    @Override
    protected Mono<Void> onLogout(WebFilterExchange exchange, Authentication authentication) {
        if (authentication != null) {
            return this.tokenRepository.removeUserTokens(authentication.getName());
        }
        return Mono.empty();
    }

    private void addCookie(RememberMeToken token, ServerWebExchange exchange) {
        var spec = token.getSpec();
        setCookie(new String[] {spec.getSeries(), spec.getTokenValue()}, exchange);
        exchange.getAttributes().put(REMEMBER_ME_SERIES_REQUEST_NAME, spec.getSeries());
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

}
