package run.halo.app.security.authentication.twofactor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.validation.Validator;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.core.user.service.UserService;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.ExternalUrlSupplier;
import run.halo.app.infra.exception.Exceptions;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.security.authentication.twofactor.totp.TotpAuthService;

class TwoFactorAuthEndpointTest {

    @ParameterizedTest
    @CsvSource({
        "zh, wrong, 123456, 密码不匹配。",
        "zh, correct, '', 请输入双重验证码。",
        "zh, correct, abc, 双重验证码无效，请重试。",
        "zh, correct, 123456, 双重验证码无效，请重试。",
        "en, correct, abc, Invalid two-factor authentication code.",
        "es, correct, abc, El código de autenticación de dos factores no es válido. Inténtalo de nuevo."
    })
    void shouldTranslateValidationErrorsWithoutUpdatingUser(
            String language, String password, String code, String detail) {
        var client = mock(ReactiveExtensionClient.class);
        var users = mock(UserService.class);
        var totp = mock(TotpAuthService.class);
        var encoder = mock(PasswordEncoder.class);
        var user = new User();
        user.setSpec(new User.UserSpec());
        user.getSpec().setPassword("encoded");
        user.getSpec().setTotpEncryptedSecret("encrypted");
        when(users.getUser("alice")).thenReturn(Mono.just(user));
        when(encoder.matches(password, "encoded")).thenReturn(password.equals("correct"));
        when(totp.decryptSecret("encrypted")).thenReturn("secret");
        var endpoint = new TwoFactorAuthEndpoint(
                client, users, totp, mock(Validator.class), encoder, mock(ExternalUrlSupplier.class));
        var messages = new ReloadableResourceBundleMessageSource();
        messages.setBasename("file:src/main/resources/config/i18n/messages");
        messages.setDefaultEncoding("UTF-8");
        messages.setFallbackToSystemLocale(false);
        var webClient = WebTestClient.bindToRouterFunction(endpoint.endpoint())
                .webFilter((exchange, chain) -> chain.filter(exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(
                                new UsernamePasswordAuthenticationToken("alice", "password")))
                        .onErrorResume(ServerWebInputException.class, error -> {
                            var response = exchange.getResponse();
                            var body = Exceptions.createErrorResponse(error, null, exchange, messages)
                                    .getBody();
                            response.setStatusCode(error.getStatusCode());
                            response.getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);
                            return response.writeWith(Mono.just(response.bufferFactory()
                                    .wrap(JsonUtils.objectToJson(body).getBytes(StandardCharsets.UTF_8))));
                        }))
                .build();
        webClient
                .put()
                .uri("/authentications/two-factor/settings/disabled")
                .header("Accept-Language", language)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("password", password, "totpCode", code))
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody()
                .jsonPath("$.detail")
                .isEqualTo(detail);
        verify(client, never()).update(any());
    }
}
