package run.halo.app.security.completion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.EncoderHttpMessageWriter;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.core.user.service.UserService;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;

class EmailCompletionFilterTest {

    UserService userService;
    SystemConfigFetcher systemConfigFetcher;
    ServerRequestCache requestCache;
    ServerResponse.Context responseContext;
    EmailCompletionFilter filter;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        systemConfigFetcher = mock(SystemConfigFetcher.class);
        requestCache = mock(ServerRequestCache.class);
        responseContext = mock(ServerResponse.Context.class);
        when(responseContext.messageWriters())
                .thenReturn(List.<HttpMessageWriter<?>>of(new EncoderHttpMessageWriter<>(new Jackson2JsonEncoder())));
        when(responseContext.viewResolvers()).thenReturn(List.of());
        filter = new EmailCompletionFilter(systemConfigFetcher, userService, requestCache, responseContext);
    }

    SystemSetting.User setting(boolean mustVerify) {
        var setting = new SystemSetting.User();
        setting.setMustVerifyEmailOnRegistration(mustVerify);
        return setting;
    }

    User user(boolean emailVerified) {
        var user = new User();
        user.setMetadata(new run.halo.app.extension.Metadata());
        user.getMetadata().setName("user");
        var spec = new User.UserSpec();
        spec.setEmail("user@example.com");
        spec.setEmailVerified(emailVerified);
        user.setSpec(spec);
        return user;
    }

    @Test
    void shouldRedirectHtmlRequestToCompleteProfile() {
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(setting(true)));
        when(userService.getUser("user")).thenReturn(Mono.just(user(false)));
        when(requestCache.saveRequest(any())).thenReturn(Mono.empty());

        var exchange =
                MockServerWebExchange.from(MockServerHttpRequest.get("/console").accept(MediaType.TEXT_HTML));
        var chain = mock(WebFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        filter.filter(exchange, chain)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(
                        new TestingAuthenticationToken("user", "password", "ROLE_authenticated")))
                .block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(exchange.getResponse().getHeaders().getLocation()).isEqualTo(URI.create("/complete-profile"));
        verify(chain, never()).filter(exchange);
    }

    @Test
    void shouldReturnForbiddenWithTypeForJsonRequest() {
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(setting(true)));
        when(userService.getUser("user")).thenReturn(Mono.just(user(false)));

        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/apis/test").accept(MediaType.APPLICATION_JSON));
        var chain = mock(WebFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        filter.filter(exchange, chain)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(
                        new TestingAuthenticationToken("user", "password", "ROLE_authenticated")))
                .block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(exchange.getResponse().getBodyAsString().block()).contains("\"type\":\"email-not-set\"");
        verify(chain, never()).filter(exchange);
    }

    @Test
    void shouldSkipWhenVerificationNotRequired() {
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(setting(false)));

        var exchange =
                MockServerWebExchange.from(MockServerHttpRequest.get("/console").accept(MediaType.TEXT_HTML));
        var chain = mock(WebFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        filter.filter(exchange, chain)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(
                        new TestingAuthenticationToken("user", "password", "ROLE_authenticated")))
                .block();

        verify(chain).filter(exchange);
    }

    @Test
    void shouldRedirectNonGetHtmlRequestToCompleteProfile() {
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(setting(true)));
        when(userService.getUser("user")).thenReturn(Mono.just(user(false)));
        when(requestCache.saveRequest(any())).thenReturn(Mono.empty());

        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/console").accept(MediaType.TEXT_HTML));
        var chain = mock(WebFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        filter.filter(exchange, chain)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(
                        new TestingAuthenticationToken("user", "password", "ROLE_authenticated")))
                .block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(exchange.getResponse().getHeaders().getLocation()).isEqualTo(URI.create("/complete-profile"));
        verify(chain, never()).filter(exchange);
    }

    @Test
    void shouldInterceptWhenUserLookupFails() {
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(setting(true)));
        when(userService.getUser("user")).thenReturn(Mono.error(new RuntimeException("boom")));
        when(requestCache.saveRequest(any())).thenReturn(Mono.empty());

        var exchange =
                MockServerWebExchange.from(MockServerHttpRequest.get("/console").accept(MediaType.TEXT_HTML));
        var chain = mock(WebFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        filter.filter(exchange, chain)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(
                        new TestingAuthenticationToken("user", "password", "ROLE_authenticated")))
                .block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(exchange.getResponse().getHeaders().getLocation()).isEqualTo(URI.create("/complete-profile"));
        verify(chain, never()).filter(exchange);
    }

    @Test
    void shouldSkipForSuperAdmin() {
        var exchange =
                MockServerWebExchange.from(MockServerHttpRequest.get("/console").accept(MediaType.TEXT_HTML));
        var chain = mock(WebFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        filter.filter(exchange, chain)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(
                        new TestingAuthenticationToken("admin", "password", "ROLE_super-role")))
                .block();

        verify(chain).filter(exchange);
        verify(systemConfigFetcher, never()).fetch(any(), any());
    }

    @Test
    void shouldSkipForExemptPath() {
        var exchange =
                MockServerWebExchange.from(MockServerHttpRequest.get("/logout").accept(MediaType.TEXT_HTML));
        var chain = mock(WebFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        filter.filter(exchange, chain)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(
                        new TestingAuthenticationToken("user", "password", "ROLE_authenticated")))
                .block();

        verify(chain).filter(exchange);
    }

    @Test
    void shouldSkipWhenEmailVerified() {
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(setting(true)));
        when(userService.getUser("user")).thenReturn(Mono.just(user(true)));

        var exchange =
                MockServerWebExchange.from(MockServerHttpRequest.get("/console").accept(MediaType.TEXT_HTML));
        var chain = mock(WebFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        filter.filter(exchange, chain)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(
                        new TestingAuthenticationToken("user", "password", "ROLE_authenticated")))
                .block();

        verify(chain).filter(exchange);
    }
}
