package run.halo.app.security.sudo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.infra.exception.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SudoModeWebFilterTest {

    @Mock
    SudoService sudoService;

    @Mock
    WebFilterChain chain;

    SudoModeWebFilter filter;

    @BeforeEach
    void setUp() {
        filter = new SudoModeWebFilter(sudoService, responseContext());
        when(chain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    void shouldWriteStatusAndSkipChain() {
        var exchange = exchange(HttpMethod.GET, "/sudo");
        when(sudoService.status(exchange))
                .thenReturn(Mono.just(new SudoStatus(false, null, List.of(new SudoMethod("totp", false, null)))));

        StepVerifier.create(filter.filter(exchange, chain)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(sessionAuth())))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(chain, never()).filter(exchange);
        verify(sudoService).status(exchange);
        verify(sudoService, never()).requireSudo(any(), any());
    }

    @Test
    void shouldConfirmAndSkipChain() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/sudo/confirm")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("method=totp&code=123456"));
        when(sudoService.confirm(eq("totp"), eq("123456"), eq(exchange))).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(sessionAuth())))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(chain, never()).filter(exchange);
        verify(sudoService).confirm("totp", "123456", exchange);
    }

    @Test
    void shouldSendCodeAndSkipChain() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/sudo/code")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("method=email"));
        when(sudoService.sendCode(eq("email"), eq(exchange))).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(sessionAuth())))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(chain, never()).filter(exchange);
        verify(sudoService).sendCode("email", exchange);
    }

    @Test
    void shouldRejectEmptyConfirmBody() {
        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/sudo/confirm").contentType(MediaType.APPLICATION_FORM_URLENCODED));

        StepVerifier.create(filter.filter(exchange, chain)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(sessionAuth())))
                .expectError(ServerWebInputException.class)
                .verify();
        verify(chain, never()).filter(exchange);
        verify(sudoService, never()).confirm(any(), any(), any());
    }

    @Test
    void shouldPropagateAccessDeniedOnSudoStatus() {
        var exchange = exchange(HttpMethod.GET, "/sudo");
        when(sudoService.status(exchange))
                .thenReturn(Mono.error(new AccessDeniedException("Sudo APIs require a browser session")));

        StepVerifier.create(filter.filter(exchange, chain)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(jwtAuth())))
                .expectError(AccessDeniedException.class)
                .verify();
        verify(chain, never()).filter(exchange);
    }

    @Test
    void shouldSkipSudoForJwtOnSensitiveApi() {
        var exchange = exchange(HttpMethod.PUT, "/apis/uc.api.halo.run/v1alpha1/users/-/password");
        StepVerifier.create(filter.filter(exchange, chain)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(jwtAuth())))
                .verifyComplete();
        verify(chain).filter(exchange);
        verify(sudoService, never()).requireSudo(any(), any());
    }

    @Test
    void shouldNotSkipSudoWhenSessionHasFakeBearer() {
        var exchange = exchange(HttpMethod.PUT, "/apis/uc.api.halo.run/v1alpha1/users/-/password");
        when(sudoService.requireSudo(eq(exchange), eq("alice"))).thenReturn(Mono.empty());
        StepVerifier.create(filter.filter(exchange, chain)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(sessionAuth())))
                .verifyComplete();
        verify(sudoService).requireSudo(exchange, "alice");
    }

    @ParameterizedTest
    @CsvSource({
        "PUT,/apis/uc.api.halo.run/v1alpha1/users/-/password",
        "PUT,/apis/api.console.halo.run/v1alpha1/users/-/password",
        "PUT,/apis/api.console.halo.run/v1alpha1/users/bob/password",
        "POST,/apis/uc.api.security.halo.run/v1alpha1/personalaccesstokens",
        "DELETE,/apis/uc.api.security.halo.run/v1alpha1/personalaccesstokens/pat-1",
        "PUT,/apis/uc.api.security.halo.run/v1alpha1/personalaccesstokens/pat-1/actions/revocation",
        "PUT,/apis/uc.api.security.halo.run/v1alpha1/personalaccesstokens/pat-1/actions/restoration"
    })
    void shouldMatchSensitivePaths(HttpMethod method, String path) {
        var exchange = exchange(method, path);
        when(sudoService.requireSudo(eq(exchange), eq("alice")))
                .thenReturn(Mono.error(new SudoRequiredException(List.of("totp"))));
        StepVerifier.create(filter.filter(exchange, chain)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(sessionAuth())))
                .expectError(SudoRequiredException.class)
                .verify();
        verify(chain, never()).filter(exchange);
    }

    @Test
    void shouldNotMatchPatGet() {
        var exchange = exchange(HttpMethod.GET, "/apis/uc.api.security.halo.run/v1alpha1/personalaccesstokens");
        StepVerifier.create(filter.filter(exchange, chain)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(sessionAuth())))
                .verifyComplete();
        verify(sudoService, never()).requireSudo(any(), any());
        verify(chain).filter(exchange);
    }

    @Test
    void shouldNotTreatOldSudoApiAsProtocol() {
        var exchange = exchange(HttpMethod.GET, "/apis/uc.api.security.halo.run/v1alpha1/authentications/sudo");
        StepVerifier.create(filter.filter(exchange, chain)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(sessionAuth())))
                .verifyComplete();
        verify(chain).filter(exchange);
        verify(sudoService, never()).status(any());
        verify(sudoService, never()).requireSudo(any(), any());
    }

    private static MockServerWebExchange exchange(HttpMethod method, String path) {
        return MockServerWebExchange.from(MockServerHttpRequest.method(method, path));
    }

    private static UsernamePasswordAuthenticationToken sessionAuth() {
        return UsernamePasswordAuthenticationToken.authenticated(
                "alice", "n/a", AuthorityUtils.createAuthorityList("ROLE_authenticated"));
    }

    private static JwtAuthenticationToken jwtAuth() {
        var jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("alice")
                .build();
        return new JwtAuthenticationToken(jwt);
    }

    private static ServerResponse.Context responseContext() {
        var strategies = HandlerStrategies.withDefaults();
        return new ServerResponse.Context() {
            @Override
            public List<org.springframework.http.codec.HttpMessageWriter<?>> messageWriters() {
                return strategies.messageWriters();
            }

            @Override
            public List<org.springframework.web.reactive.result.view.ViewResolver> viewResolvers() {
                return strategies.viewResolvers();
            }
        };
    }
}
