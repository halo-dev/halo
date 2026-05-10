package run.halo.app.security.authentication.rememberme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.rememberme.CookieTheftException;
import org.springframework.security.web.authentication.rememberme.InvalidCookieException;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationException;
import org.springframework.security.web.server.WebFilterExchange;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Device;
import run.halo.app.core.extension.RememberMeToken;
import run.halo.app.extension.Metadata;
import run.halo.app.security.LoginParameterRequestCache;
import run.halo.app.security.device.DeviceService;

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

    @Mock
    private DeviceService deviceService;

    @Mock
    private LoginParameterRequestCache parameterRequestCache;

    @InjectMocks
    private PersistentTokenBasedRememberMeServices persistentTokenBasedRememberMeServices;

    private static final Instant NOW = Instant.parse("2024-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        persistentTokenBasedRememberMeServices.setClock(Clock.fixed(NOW, ZoneId.of("UTC")));
        lenient().when(rememberMeCookieResolver.getCookieMaxAge()).thenReturn(Duration.ofDays(14));
    }

    /** Creates a test RememberMeToken with the given properties. */
    private static RememberMeToken createTestToken(
            String series, String tokenValue, String username, Instant lastUsed) {
        var token = new RememberMeToken();
        token.setMetadata(new Metadata());
        token.getMetadata().setName("test-token-" + series);
        token.setSpec(new RememberMeToken.Spec());
        token.getSpec().setSeries(series);
        token.getSpec().setTokenValue(tokenValue);
        token.getSpec().setUsername(username);
        token.getSpec().setLastUsed(lastUsed);
        return token;
    }

    /** Creates a test Device with the given remember-me series ID. */
    private static Device createTestDevice(String seriesId) {
        var device = new Device();
        device.setMetadata(new Metadata());
        device.getMetadata().setName("test-device");
        device.setSpec(new Device.Spec());
        device.getSpec().setRememberMeSeriesId(seriesId);
        device.getSpec().setSessionId("test-session");
        device.getSpec().setPrincipalName("test-user");
        return device;
    }

    @Nested
    class ProcessAutoLoginCookieTest {
        @Test
        void shouldThrowInvalidCookieExceptionWhenCookieTokensLengthIsNot2() {
            var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"));
            assertThatThrownBy(() -> persistentTokenBasedRememberMeServices
                            .processAutoLoginCookie(new String[] {"test"}, exchange)
                            .block())
                    .isInstanceOf(InvalidCookieException.class)
                    .hasMessage("Cookie token did not contain 2 tokens, but contained '[test]'");
        }

        @Test
        void shouldThrowRememberMeAuthenticationExceptionWhenNoPersistentTokenFound() {
            var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"));
            when(tokenRepository.getTokenForSeries(eq("test-series"))).thenReturn(Mono.empty());

            assertThatThrownBy(() -> persistentTokenBasedRememberMeServices
                            .processAutoLoginCookie(new String[] {"test-series", "test-token"}, exchange)
                            .block())
                    .isInstanceOf(RememberMeAuthenticationException.class)
                    .hasMessage("No persistent token found for series id: test-series");
        }

        @Test
        void shouldThrowExceptionWhenDeviceCannotBeDetermined() {
            var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"));
            var storedToken = createTestToken("test-series", "stored-token", "test-user", NOW);
            when(tokenRepository.getTokenForSeries(eq("test-series"))).thenReturn(Mono.just(storedToken));
            when(deviceService.resolveCurrentDevice(exchange)).thenReturn(Mono.empty());

            assertThatThrownBy(() -> persistentTokenBasedRememberMeServices
                            .processAutoLoginCookie(new String[] {"test-series", "stored-token"}, exchange)
                            .block())
                    .isInstanceOf(RememberMeAuthenticationException.class)
                    .hasMessage("Unable to determine device for remember-me authentication");
        }

        @Test
        void shouldThrowExceptionWhenDeviceSeriesMismatch() {
            var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"));
            var storedToken = createTestToken("test-series", "stored-token", "test-user", NOW);
            var device = createTestDevice("different-series");
            when(tokenRepository.getTokenForSeries(eq("test-series"))).thenReturn(Mono.just(storedToken));
            when(deviceService.resolveCurrentDevice(exchange)).thenReturn(Mono.just(device));

            assertThatThrownBy(() -> persistentTokenBasedRememberMeServices
                            .processAutoLoginCookie(new String[] {"test-series", "stored-token"}, exchange)
                            .block())
                    .isInstanceOf(RememberMeAuthenticationException.class)
                    .hasMessage("Remember-me series ID does not match current device's series ID");
        }

        @Test
        void shouldDetectCookieTheftWhenOutsideGracePeriod() {
            var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"));
            var lastUsed = NOW.minusSeconds(11);
            var storedToken = createTestToken("test-series", "correct-token", "test-user", lastUsed);
            storedToken.getSpec().setPreviousTokenValue("previous-token");
            var device = createTestDevice("test-series");
            when(tokenRepository.getTokenForSeries(eq("test-series"))).thenReturn(Mono.just(storedToken));
            when(deviceService.resolveCurrentDevice(exchange)).thenReturn(Mono.just(device));
            when(tokenRepository.removeUserTokens(eq("test-user"))).thenReturn(Mono.empty());

            assertThatThrownBy(() -> persistentTokenBasedRememberMeServices
                            .processAutoLoginCookie(new String[] {"test-series", "wrong-token"}, exchange)
                            .block())
                    .isInstanceOf(CookieTheftException.class)
                    .hasMessageContaining("Invalid remember-me token (Series/token) mismatch");

            verify(tokenRepository).removeUserTokens(eq("test-user"));
        }

        @Test
        void shouldDetectCookieTheftWhenPreviousTokenDoesNotMatch() {
            var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"));
            var storedToken = createTestToken("test-series", "correct-token", "test-user", NOW);
            storedToken.getSpec().setPreviousTokenValue("different-previous-token");
            var device = createTestDevice("test-series");
            when(tokenRepository.getTokenForSeries(eq("test-series"))).thenReturn(Mono.just(storedToken));
            when(deviceService.resolveCurrentDevice(exchange)).thenReturn(Mono.just(device));
            when(tokenRepository.removeUserTokens(eq("test-user"))).thenReturn(Mono.empty());

            assertThatThrownBy(() -> persistentTokenBasedRememberMeServices
                            .processAutoLoginCookie(new String[] {"test-series", "presented-token"}, exchange)
                            .block())
                    .isInstanceOf(CookieTheftException.class)
                    .hasMessageContaining("Invalid remember-me token (Series/token) mismatch");

            verify(tokenRepository).removeUserTokens(eq("test-user"));
        }

        @Test
        void shouldAcceptTokenWithinGracePeriod() {
            var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"));
            var storedToken = createTestToken("test-series", "rotated-token", "test-user", NOW);
            storedToken.getSpec().setPreviousTokenValue("old-token");
            var device = createTestDevice("test-series");
            var userDetails = User.withUsername("test-user")
                    .password("password")
                    .roles("USER")
                    .build();
            when(tokenRepository.getTokenForSeries(eq("test-series"))).thenReturn(Mono.just(storedToken));
            when(deviceService.resolveCurrentDevice(exchange)).thenReturn(Mono.just(device));
            when(userDetailsService.findByUsername(eq("test-user"))).thenReturn(Mono.just(userDetails));

            var result = persistentTokenBasedRememberMeServices
                    .processAutoLoginCookie(new String[] {"test-series", "old-token"}, exchange)
                    .block();

            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("test-user");
            // Token should not be rotated when values don't match (grace period path)
            verify(tokenRepository, never()).updateToken(any());
        }

        @Test
        void shouldThrowExceptionWhenTokenExpired() {
            var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"));
            var lastUsed = NOW.minusSeconds(100);
            var storedToken = createTestToken("test-series", "test-token", "test-user", lastUsed);
            var device = createTestDevice("test-series");
            when(tokenRepository.getTokenForSeries(eq("test-series"))).thenReturn(Mono.just(storedToken));
            when(deviceService.resolveCurrentDevice(exchange)).thenReturn(Mono.just(device));
            // Override the default 14-day max age to a shorter duration
            when(rememberMeCookieResolver.getCookieMaxAge()).thenReturn(Duration.ofSeconds(60));

            assertThatThrownBy(() -> persistentTokenBasedRememberMeServices
                            .processAutoLoginCookie(new String[] {"test-series", "test-token"}, exchange)
                            .block())
                    .isInstanceOf(InvalidCookieException.class)
                    .hasMessage("Remember-me login has expired");
        }

        @Test
        void shouldRotateTokenAndLoadUserOnSuccess() {
            var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"));
            var storedToken = createTestToken("test-series", "test-token", "test-user", NOW);
            var device = createTestDevice("test-series");
            var userDetails = User.withUsername("test-user")
                    .password("password")
                    .roles("USER")
                    .build();
            when(tokenRepository.getTokenForSeries(eq("test-series"))).thenReturn(Mono.just(storedToken));
            when(deviceService.resolveCurrentDevice(exchange)).thenReturn(Mono.just(device));
            when(tokenRepository.updateToken(any(RememberMeToken.class)))
                    .thenAnswer(inv -> Mono.just(inv.getArgument(0)));
            when(userDetailsService.findByUsername(eq("test-user"))).thenReturn(Mono.just(userDetails));

            var result = persistentTokenBasedRememberMeServices
                    .processAutoLoginCookie(new String[] {"test-series", "test-token"}, exchange)
                    .block();

            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("test-user");
            verify(tokenRepository).updateToken(any(RememberMeToken.class));
            // Cookie should be set with new token values
            verify(rememberMeCookieResolver).setRememberMeCookie(eq(exchange), any());
        }

        @Test
        void shouldFallbackToExistingTokenWhenRotationFailsWithOptimisticLock() {
            var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"));
            var storedToken = createTestToken("test-series", "test-token", "test-user", NOW);
            var device = createTestDevice("test-series");
            var userDetails = User.withUsername("test-user")
                    .password("password")
                    .roles("USER")
                    .build();
            when(tokenRepository.getTokenForSeries(eq("test-series"))).thenReturn(Mono.just(storedToken));
            when(deviceService.resolveCurrentDevice(exchange)).thenReturn(Mono.just(device));
            when(tokenRepository.updateToken(any(RememberMeToken.class)))
                    .thenReturn(Mono.error(new OptimisticLockingFailureException("concurrent update")));
            when(userDetailsService.findByUsername(eq("test-user"))).thenReturn(Mono.just(userDetails));

            var result = persistentTokenBasedRememberMeServices
                    .processAutoLoginCookie(new String[] {"test-series", "test-token"}, exchange)
                    .block();

            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("test-user");
            verify(tokenRepository).updateToken(any(RememberMeToken.class));
            // Cookie should NOT be set because rotation failed
            verify(rememberMeCookieResolver, never()).setRememberMeCookie(any(), any());
        }
    }

    @Test
    void onLoginSuccessTest() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"));
        var authentication = new UsernamePasswordAuthenticationToken("test", "test");

        when(tokenRepository.createNewToken(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        persistentTokenBasedRememberMeServices
                .onLoginSuccess(exchange, authentication)
                .block();

        verify(tokenRepository).createNewToken(any());
        verify(rememberMeCookieResolver).setRememberMeCookie(eq(exchange), any());
    }

    @Test
    void onLogoutTest() {
        var authentication = new UsernamePasswordAuthenticationToken("test", "test");

        when(tokenRepository.removeUserTokens(eq("test"))).thenReturn(Mono.empty());

        var filterExchange = mock(WebFilterExchange.class);
        persistentTokenBasedRememberMeServices
                .onLogout(filterExchange, authentication)
                .block();

        verify(tokenRepository).removeUserTokens(eq("test"));
    }

    @Test
    void onLogoutWithNullAuthenticationShouldNotCallRemoveTokens() {
        var filterExchange = mock(WebFilterExchange.class);
        persistentTokenBasedRememberMeServices.onLogout(filterExchange, null).block();

        verify(tokenRepository, never()).removeUserTokens(any());
    }
}
