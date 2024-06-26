package run.halo.app.security.authentication.rememberme;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.web.authentication.rememberme.CookieTheftException;
import org.springframework.security.web.authentication.rememberme.InvalidCookieException;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationException;
import org.springframework.security.web.server.WebFilterExchange;
import reactor.core.publisher.Mono;

/**
 * Tests for {@link PersistentTokenBasedRememberMeServices}.
 *
 * @author guqing
 * @since 2.17.0
 */
@ExtendWith(MockitoExtension.class)
class PersistentTokenBasedRememberMeServicesTest {
    @Mock
    private CookieSignatureKeyResolver cookieSignatureKeyResolver;

    @Mock
    private ReactiveUserDetailsService userDetailsService;

    @Mock
    private RememberMeCookieResolver rememberMeCookieResolver;

    @Mock
    private PersistentRememberMeTokenRepository tokenRepository;

    @InjectMocks
    private PersistentTokenBasedRememberMeServices persistentTokenBasedRememberMeServices;

    @Nested
    class ProcessAutoLoginCookieTest {
        @Test
        void invalidCookieTest() {
            var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"));
            assertThatThrownBy(() -> persistentTokenBasedRememberMeServices.processAutoLoginCookie(
                new String[] {"test"},
                exchange).block())
                .isInstanceOf(InvalidCookieException.class)
                .hasMessage("Cookie token did not contain 2 tokens, but contained '[test]'");
        }

        @Test
        void noPersistentTokenFoundTest() {
            var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"));
            when(tokenRepository.getTokenForSeries(eq("test-series")))
                .thenReturn(Mono.empty());

            assertThatThrownBy(() -> persistentTokenBasedRememberMeServices.processAutoLoginCookie(
                new String[] {"test-series", "test"},
                exchange).block()
            ).isInstanceOf(RememberMeAuthenticationException.class)
                .hasMessage("No persistent token found for series id: test-series");
        }

        @Test
        void tokenMismatchTest() {
            var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"));
            when(tokenRepository.getTokenForSeries(eq("fake-series")))
                .thenReturn(Mono.just(
                    new PersistentRememberMeToken("test", "fake-series", "other-token-value",
                        new Date()))
                );
            when(tokenRepository.removeUserTokens(eq("test"))).thenReturn(Mono.empty());
            assertThatThrownBy(() -> persistentTokenBasedRememberMeServices.processAutoLoginCookie(
                new String[] {"fake-series", "token-value"},
                exchange).block())
                .isInstanceOf(CookieTheftException.class)
                .hasMessage(
                    "Invalid remember-me token (Series/token) mismatch. Implies previous cookie "
                        + "theft attack.");
        }

        @Test
        void rememberMeLoginExpiredTest() {
            var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"));
            when(tokenRepository.getTokenForSeries(eq("fake-series")))
                .thenReturn(Mono.just(
                    new PersistentRememberMeToken("test", "fake-series", "token-value",
                        new Date(Instant.now().minusSeconds(10).toEpochMilli())))
                );
            when(rememberMeCookieResolver.getCookieMaxAge()).thenReturn(Duration.ofSeconds(5));
            assertThatThrownBy(() -> persistentTokenBasedRememberMeServices.processAutoLoginCookie(
                new String[] {"fake-series", "token-value"},
                exchange).block())
                .isInstanceOf(RememberMeAuthenticationException.class)
                .hasMessage("Remember-me login has expired");
        }

        @Test
        void successfulTest() {
            var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"));
            when(tokenRepository.getTokenForSeries(eq("fake-series")))
                .thenReturn(Mono.just(
                    new PersistentRememberMeToken("test", "fake-series", "token-value",
                        new Date()))
                );
            when(rememberMeCookieResolver.getCookieMaxAge()).thenReturn(Duration.ofSeconds(5));

            var generatedTokenValue = new AtomicReference<String>();
            when(tokenRepository.updateToken(eq("fake-series"), any(), any()))
                .thenAnswer(invocation -> {
                    var tokenValue = (String) invocation.getArgument(1);
                    generatedTokenValue.compareAndSet(null, tokenValue);
                    return Mono.empty();
                });

            when(userDetailsService.findByUsername(eq("test"))).thenReturn(Mono.empty());

            persistentTokenBasedRememberMeServices.processAutoLoginCookie(
                    new String[] {"fake-series", "token-value"}, exchange)
                .block();

            verify(rememberMeCookieResolver).setRememberMeCookie(eq(exchange),
                eq(persistentTokenBasedRememberMeServices.encodeCookie(
                    new String[] {"fake-series", generatedTokenValue.get()})));
        }
    }

    @Test
    void onLoginSuccessTest() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"));
        var authentication = new UsernamePasswordAuthenticationToken("test", "test");

        when(tokenRepository.createNewToken(any())).thenReturn(Mono.empty());
        persistentTokenBasedRememberMeServices.onLoginSuccess(exchange, authentication).block();

        verify(rememberMeCookieResolver).setRememberMeCookie(eq(exchange), any());
    }

    @Test
    void onLogoutTest() {
        var authentication = new UsernamePasswordAuthenticationToken("test", "test");

        when(tokenRepository.removeUserTokens(eq("test"))).thenReturn(Mono.empty());

        var filterExchange = mock(WebFilterExchange.class);
        persistentTokenBasedRememberMeServices.onLogout(filterExchange, authentication).block();
    }
}