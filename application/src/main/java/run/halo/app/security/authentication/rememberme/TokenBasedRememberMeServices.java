package run.halo.app.security.authentication.rememberme;

import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.apache.commons.lang3.BooleanUtils.toBoolean;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.codec.Utf8;
import org.springframework.security.web.authentication.rememberme.CookieTheftException;
import org.springframework.security.web.authentication.rememberme.InvalidCookieException;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * <p>An {@link org.springframework.security.core.userdetails.UserDetailsService} is required
 * by this implementation, so that it can construct a valid <code>Authentication</code>
 * from the returned {@link org.springframework.security.core.userdetails.UserDetails}.</p>
 * <p>This is also necessary so that the user's password is available and can be checked as
 * part of the encoded cookie.</p>
 * <p>The cookie encoded by this implementation adopts the following form:
 * <pre>
 * username + ":" + expiryTime + ":" + algorithmName + ":"
 *   + algorithmHex(username + ":" + expiryTime + ":" + password + ":" + key)
 * </pre>
 * </p>
 *
 * @see org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices
 */
@Slf4j
@Setter
@Getter
@Component
@RequiredArgsConstructor
public class TokenBasedRememberMeServices implements RememberMeServices {

    public static final int TWO_WEEKS_S = 1209600;

    public static final String DEFAULT_PARAMETER = "remember-me";

    public static final String DEFAULT_ALGORITHM = "SHA-256";

    private static final String DELIMITER = ":";

    private final CookieSignatureKeyResolver cookieSignatureKeyResolver;

    private final ReactiveUserDetailsService userDetailsService;

    private final RememberMeCookieResolver rememberMeCookieResolver;

    private UserDetailsChecker userDetailsChecker = new AccountStatusUserDetailsChecker();

    private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    private static boolean equals(String expected, String actual) {
        byte[] expectedBytes = bytesUtf8(expected);
        byte[] actualBytes = bytesUtf8(actual);
        return MessageDigest.isEqual(expectedBytes, actualBytes);
    }

    private static byte[] bytesUtf8(String s) {
        return (s != null) ? Utf8.encode(s) : null;
    }

    @Override
    public Mono<Authentication> autoLogin(ServerWebExchange exchange) {
        var rememberMeCookie = rememberMeCookieResolver.resolveRememberMeCookie(exchange);
        if (rememberMeCookie == null) {
            return Mono.empty();
        }
        log.debug("Remember-me cookie detected");
        return Mono.defer(
                () -> {
                    String[] cookieTokens = decodeCookie(rememberMeCookie.getValue());
                    return processAutoLoginCookie(cookieTokens, exchange);
                })
            .flatMap(user -> {
                this.userDetailsChecker.check(user);
                log.debug("Remember-me cookie accepted");
                return createSuccessfulAuthentication(exchange, user);
            })
            .onErrorResume(ex -> handleError(exchange, ex));
    }

    private Mono<Authentication> handleError(ServerWebExchange exchange, Throwable ex) {
        cancelCookie(exchange);
        if (ex instanceof CookieTheftException) {
            log.error("Cookie theft detected", ex);
            return Mono.error(ex);
        } else if (ex instanceof UsernameNotFoundException) {
            log.debug("Remember-me login was valid but corresponding user not found.", ex);
        } else if (ex instanceof InvalidCookieException) {
            log.debug("Invalid remember-me cookie: {}", ex.getMessage());
        } else if (ex instanceof AccountStatusException) {
            log.debug("Invalid UserDetails: {}", ex.getMessage());
        } else if (ex instanceof RememberMeAuthenticationException) {
            log.debug(ex.getMessage());
        }
        return Mono.empty();
    }

    protected void cancelCookie(ServerWebExchange exchange) {
        rememberMeCookieResolver.expireCookie(exchange);
    }

    protected Mono<UserDetails> processAutoLoginCookie(String[] cookieTokens,
        ServerWebExchange exchange) {
        if (!isValidCookieTokensLength(cookieTokens)) {
            throw new InvalidCookieException(
                "Cookie token did not contain 3 or 4 tokens, but contained '" + Arrays.asList(
                    cookieTokens) + "'");
        }

        long tokenExpiryTime = getTokenExpiryTime(cookieTokens);
        if (isTokenExpired(tokenExpiryTime)) {
            throw new InvalidCookieException(
                "Cookie token[1] has expired (expired on '" + new Date(tokenExpiryTime)
                    + "'; current time is '" + new Date() + "')");
        }

        // Check the user exists. Defer lookup until after expiry time checked, to
        // possibly avoid expensive database call.
        return getUserDetailsService().findByUsername(cookieTokens[0])
            .switchIfEmpty(Mono.error(new UsernameNotFoundException("User '" + cookieTokens[0]
                + "' not found")))
            .flatMap(userDetails -> {
                // Check signature of token matches remaining details. Must do this after user
                // lookup, as we need the DAO-derived password. If efficiency was a major issue,
                // just add in a UserCache implementation, but recall that this method is usually
                // only called once per HttpSession - if the token is valid, it will cause
                // SecurityContextHolder population, whilst if invalid, will cause the cookie to
                // be cancelled.
                String actualTokenSignature;
                String actualAlgorithm = DEFAULT_ALGORITHM;
                // If the cookie value contains the algorithm, we use that algorithm to check the
                // signature
                if (cookieTokens.length == 4) {
                    actualTokenSignature = cookieTokens[3];
                    actualAlgorithm = cookieTokens[2];
                } else {
                    actualTokenSignature = cookieTokens[2];
                }
                return makeTokenSignature(tokenExpiryTime, userDetails.getUsername(),
                    userDetails.getPassword(), actualAlgorithm)
                    .doOnNext(expectedTokenSignature -> {
                        if (!equals(expectedTokenSignature, actualTokenSignature)) {
                            throw new InvalidCookieException(
                                "Cookie contained signature '" + actualTokenSignature
                                    + "' but expected '"
                                    + expectedTokenSignature + "'");
                        }
                    })
                    .thenReturn(userDetails);
            });
    }

    protected boolean isTokenExpired(long tokenExpiryTime) {
        return tokenExpiryTime < System.currentTimeMillis();
    }

    private long getTokenExpiryTime(String[] cookieTokens) {
        try {
            return Long.parseLong(cookieTokens[1]);
        } catch (NumberFormatException nfe) {
            throw new InvalidCookieException(
                "Cookie token[1] did not contain a valid number (contained '" + cookieTokens[1]
                    + "')");
        }
    }

    protected Mono<Authentication> createSuccessfulAuthentication(ServerWebExchange exchange,
        UserDetails user) {
        return getKey()
            .map(key -> new RememberMeAuthenticationToken(key, user,
                this.authoritiesMapper.mapAuthorities(user.getAuthorities()))
            );
    }

    private boolean isValidCookieTokensLength(String[] cookieTokens) {
        return cookieTokens.length == 3 || cookieTokens.length == 4;
    }

    @Override
    public Mono<Void> loginFail(ServerWebExchange exchange) {
        log.debug("Interactive login attempt was unsuccessful.");
        cancelCookie(exchange);
        return Mono.empty();
    }

    @Override
    public Mono<Void> loginSuccess(ServerWebExchange exchange,
        Authentication successfulAuthentication) {
        if (!rememberMeRequested(exchange)) {
            log.debug("Remember-me login not requested.");
            return Mono.empty();
        }
        return Mono.defer(() -> retrieveUsernamePassword(successfulAuthentication))
            .flatMap(pair -> {
                var username = pair.username();
                var password = pair.password();
                var expiryTimeMs = calculateExpireTime(exchange, successfulAuthentication);
                return makeTokenSignature(expiryTimeMs, username, password, DEFAULT_ALGORITHM)
                    .doOnNext(signatureValue -> {
                        setCookie(
                            new String[] {username, Long.toString(expiryTimeMs), DEFAULT_ALGORITHM,
                                signatureValue},
                            exchange);
                        if (log.isDebugEnabled()) {
                            log.debug("Added remember-me cookie for user '{}', expiry: '{}'",
                                username,
                                new Date(expiryTimeMs));
                        }
                    });
            })
            .then();
    }

    private Mono<UsernamePassword> retrieveUsernamePassword(
        Authentication successfulAuthentication) {
        return Mono.defer(() -> {
            String username = retrieveUserName(successfulAuthentication);
            String password = retrievePassword(successfulAuthentication);
            // If unable to find a username and password, just abort as
            // TokenBasedRememberMeServices is
            // unable to construct a valid token in this case.
            if (!StringUtils.hasLength(username)) {
                log.debug("Unable to retrieve username");
                return Mono.empty();
            }
            if (!StringUtils.hasLength(password)) {
                return getUserDetailsService().findByUsername(username)
                    .flatMap(user -> {
                        String existingPassword = user.getPassword();
                        if (!StringUtils.hasLength(existingPassword)) {
                            log.debug("Unable to obtain password for user: {}", username);
                            return Mono.empty();
                        }
                        return Mono.just(new UsernamePassword(username, existingPassword));
                    });
            }
            return Mono.just(new UsernamePassword(username, password));
        });
    }

    void setCookie(String[] cookieTokens, ServerWebExchange exchange) {
        String cookieValue = encodeCookie(cookieTokens);
        rememberMeCookieResolver.setRememberMeCookie(exchange, cookieValue);
    }

    protected long calculateExpireTime(ServerWebExchange exchange,
        Authentication authentication) {
        var tokenLifetime = rememberMeCookieResolver.getCookieMaxAge().toSeconds();
        return Instant.now().plusSeconds(tokenLifetime).toEpochMilli();
    }

    protected boolean rememberMeRequested(ServerWebExchange exchange) {
        String rememberMe = exchange.getRequest().getQueryParams().getFirst(DEFAULT_PARAMETER);
        if (isTrue(toBoolean(rememberMe))) {
            return true;
        }
        if (log.isDebugEnabled()) {
            log.debug("Did not send remember-me cookie (principal did not set parameter '{}')",
                DEFAULT_PARAMETER);
        }
        return false;
    }

    protected String[] decodeCookie(String cookieValue) throws InvalidCookieException {
        int paddingCount = 4 - (cookieValue.length() % 4);
        if (paddingCount < 4) {
            char[] padding = new char[paddingCount];
            Arrays.fill(padding, '=');
            cookieValue += new String(padding);
        }
        String cookieAsPlainText;
        try {
            cookieAsPlainText = new String(Base64.getDecoder().decode(cookieValue.getBytes()));
        } catch (IllegalArgumentException ex) {
            throw new InvalidCookieException(
                "Cookie token was not Base64 encoded; value was '" + cookieValue + "'");
        }
        String[] tokens = StringUtils.delimitedListToStringArray(cookieAsPlainText, DELIMITER);
        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = URLDecoder.decode(tokens[i], StandardCharsets.UTF_8);
        }
        return tokens;
    }

    /**
     * Inverse operation of decodeCookie.
     *
     * @param cookieTokens the tokens to be encoded.
     * @return base64 encoding of the tokens concatenated with the ":" delimiter.
     */
    protected String encodeCookie(String[] cookieTokens) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cookieTokens.length; i++) {
            sb.append(URLEncoder.encode(cookieTokens[i], StandardCharsets.UTF_8));
            if (i < cookieTokens.length - 1) {
                sb.append(DELIMITER);
            }
        }
        String value = sb.toString();
        sb = new StringBuilder(new String(Base64.getEncoder().encode(value.getBytes())));
        while (sb.charAt(sb.length() - 1) == '=') {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    protected Mono<String> makeTokenSignature(long tokenExpiryTime, String username,
        String password, String algorithm) {
        return getKey()
            .handle((key, sink) -> {
                String data = username + ":" + tokenExpiryTime + ":" + password + ":" + key;
                try {
                    MessageDigest digest = MessageDigest.getInstance(algorithm);
                    sink.next(new String(Hex.encode(digest.digest(data.getBytes()))));
                } catch (NoSuchAlgorithmException ex) {
                    sink.error(
                        new IllegalStateException("No " + algorithm + " algorithm available!"));
                }
            });
    }

    protected String retrieveUserName(Authentication authentication) {
        if (isInstanceOfUserDetails(authentication)) {
            return ((UserDetails) authentication.getPrincipal()).getUsername();
        }
        return authentication.getPrincipal().toString();
    }

    protected String retrievePassword(Authentication authentication) {
        if (isInstanceOfUserDetails(authentication)) {
            return ((UserDetails) authentication.getPrincipal()).getPassword();
        }
        if (authentication.getCredentials() != null) {
            return authentication.getCredentials().toString();
        }
        return null;
    }

    private boolean isInstanceOfUserDetails(Authentication authentication) {
        return authentication.getPrincipal() instanceof UserDetails;
    }

    protected Mono<String> getKey() {
        return cookieSignatureKeyResolver.resolveSigningKey();
    }

    record UsernamePassword(String username, String password) {
    }
}
