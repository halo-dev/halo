package run.halo.app.infra.exception.handlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import run.halo.app.core.endpoint.console.PluginEndpoint;
import run.halo.app.infra.exception.AgreementNotAcceptedException;
import run.halo.app.infra.exception.EmailAlreadyTakenException;
import run.halo.app.infra.exception.Exceptions;
import run.halo.app.infra.exception.RestrictedNameException;
import run.halo.app.infra.exception.UnsatisfiedAttributeValueException;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.security.LoginHandlerEnhancer;
import run.halo.app.security.authentication.LoginFailureHandler;
import run.halo.app.security.authentication.UserAccountStatusChecker;
import run.halo.app.security.authentication.exception.TooManyRequestsException;
import run.halo.app.security.authentication.twofactor.TwoFactorAuthRequiredException;
import run.halo.app.theme.endpoint.ThemeEndpoint;

class UserFacingExceptionI18nTest {

    @ParameterizedTest
    @ValueSource(
            strings = {
                "problemDetail.attachment.settingsMissing",
                "problemDetail.authentication.required",
                "problemDetail.comment.content.empty",
                "problemDetail.comment.content.invalid",
                "problemDetail.comment.unavailable",
                "problemDetail.content.snapshot.first",
                "problemDetail.hierarchy.beforeMissing",
                "problemDetail.hierarchy.cycle",
                "problemDetail.hierarchy.descendant",
                "problemDetail.hierarchy.notSibling",
                "problemDetail.hierarchy.parentMissing",
                "problemDetail.hierarchy.self",
                "problemDetail.pat.alreadyRevoked",
                "problemDetail.pat.insufficientRoles",
                "problemDetail.pat.invalidExpiry",
                "problemDetail.pat.notRevoked",
                "problemDetail.plugin.archiveType",
                "problemDetail.theme.archiveType",
                "problemDetail.theme.notActivated",
                "problemDetail.user.cannotDisableSelf",
                "problemDetail.user.email.alreadyVerified",
                "problemDetail.user.email.missing",
                "problemDetail.user.notFoundOrDisabled",
                "problemDetail.user.notFoundOrEnabled",
                "problemDetail.user.password.notMatch",
                "problemDetail.user.signUpFailed.disallowed",
                "problemDetail.user.signup.defaultRoleMissing",
                "problemDetail.user.twoFactor.code.invalid",
                "problemDetail.user.twoFactor.code.required"
            })
    void shouldFallBackToEnglishForCodeOnlyExceptions(String code) throws IOException {
        var english = new Properties();
        try (var reader = Files.newBufferedReader(Path.of("src/main/resources/config/i18n/messages.properties"))) {
            english.load(reader);
        }
        var expected = english.getProperty(code);
        assertThat(expected).isNotBlank();
        var messages = new ReloadableResourceBundleMessageSource();
        messages.setBasename("file:src/main/resources/config/i18n/messages");
        messages.setDefaultEncoding("UTF-8");
        messages.setFallbackToSystemLocale(false);
        for (var locale : new Locale[] {Locale.ENGLISH, Locale.FRENCH}) {
            var exchange = MockServerWebExchange.from(
                    MockServerHttpRequest.get("/apis/test").acceptLanguageAsLocales(locale));
            var body = Exceptions.createErrorResponse(
                            new UnsatisfiedAttributeValueException(code), null, exchange, messages)
                    .getBody();
            assertThat(body.getStatus()).isEqualTo(400);
            assertThat(body.getDetail()).isEqualTo(expected);
        }
    }

    @ParameterizedTest
    @CsvSource({"en, Please select a file to upload.", "zh, 请选择要上传的文件。", "es, Selecciona un archivo para subir."})
    void shouldTranslateMissingAndNonFileUploads(String language, String detail) {
        var messages = new ReloadableResourceBundleMessageSource();
        messages.setBasename("file:src/main/resources/config/i18n/messages");
        messages.setDefaultEncoding("UTF-8");
        messages.setFallbackToSystemLocale(false);
        var plugins = mock(PluginEndpoint.class, CALLS_REAL_METHODS);
        var themes = mock(ThemeEndpoint.class, CALLS_REAL_METHODS);
        var webClient = WebTestClient.bindToRouterFunction(plugins.endpoint().and(themes.endpoint()))
                .webFilter((exchange, chain) -> chain.filter(exchange)
                        .onErrorResume(ServerWebInputException.class, error -> {
                            var body = Exceptions.createErrorResponse(error, null, exchange, messages)
                                    .getBody();
                            var response = exchange.getResponse();
                            response.setStatusCode(error.getStatusCode());
                            response.getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);
                            return response.writeWith(Mono.just(response.bufferFactory()
                                    .wrap(JsonUtils.objectToJson(body).getBytes(StandardCharsets.UTF_8))));
                        }))
                .build();
        for (var path :
                new String[] {"/plugins/install", "/plugins/test/upgrade", "/themes/install", "/themes/test/upgrade"}) {
            for (var nonFilePart : new boolean[] {false, true}) {
                var multipart = new MultipartBodyBuilder();
                multipart.part("source", "file");
                if (nonFilePart) {
                    multipart.part("file", "not a file");
                }
                webClient
                        .post()
                        .uri(path)
                        .header("Accept-Language", language)
                        .body(BodyInserters.fromMultipartData(multipart.build()))
                        .exchange()
                        .expectStatus()
                        .isBadRequest()
                        .expectBody()
                        .jsonPath("$.detail")
                        .isEqualTo(detail);
            }
        }
    }

    @ParameterizedTest
    @CsvSource({
        "en, Email Already Taken, Email address is already taken., Two-factor authentication required., Please accept the required agreements., The username alice is reserved. Please choose another username.",
        "zh, 邮箱已被使用, 邮箱已被使用，请更换邮箱。, 请先完成双重验证。, 请先同意必需的协议。, 用户名 alice 为保留名称，请更换后重试。",
        "es, Correo electrónico en uso, La dirección de correo electrónico ya está en uso., Se requiere autenticación de dos factores., Acepta los acuerdos obligatorios., El nombre de usuario alice está reservado. Elige otro nombre de usuario."
    })
    void shouldTranslateBusinessExceptions(
            String language, String title, String email, String twoFactor, String agreement, String username) {
        var messages = new ReloadableResourceBundleMessageSource();
        messages.setBasename("file:src/main/resources/config/i18n/messages");
        messages.setFallbackToSystemLocale(false);
        messages.setDefaultEncoding("UTF-8");
        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/apis/test").acceptLanguageAsLocales(Locale.forLanguageTag(language)));
        var body = Exceptions.createErrorResponse(
                        new EmailAlreadyTakenException("Email already taken."), null, exchange, messages)
                .getBody();
        assertThat(body.getTitle()).isEqualTo(title);
        assertThat(body.getDetail()).isEqualTo(email);
        assertThat(body.getType()).isEqualTo(EmailAlreadyTakenException.TYPE);
        assertThat(Exceptions.createErrorResponse(
                                new TwoFactorAuthRequiredException(URI.create("/login/2fa")), null, exchange, messages)
                        .getBody()
                        .getDetail())
                .isEqualTo(twoFactor);
        assertThat(Exceptions.createErrorResponse(
                                new AgreementNotAcceptedException(
                                        "Agreement not accepted.",
                                        "problemDetail.user.signup.agreement-not-accepted",
                                        null),
                                null,
                                exchange,
                                messages)
                        .getBody()
                        .getDetail())
                .isEqualTo(agreement);
        assertThat(Exceptions.createErrorResponse(
                                new RestrictedNameException(
                                        "The username is restricted.",
                                        "problemDetail.user.username.restricted",
                                        new Object[] {"alice"}),
                                null,
                                exchange,
                                messages)
                        .getBody()
                        .getDetail())
                .isEqualTo(username);
    }

    @ParameterizedTest
    @CsvSource({
        "locked, 账户已锁定，请联系管理员。",
        "disabled, 账户或访问令牌已停用或不可用。",
        "expired, 账户已过期，请联系管理员。",
        "credentials, 登录凭据已过期，请重置密码。",
        "rate, 请求过于频繁，请稍后重试。"
    })
    void shouldTranslateAuthenticationJson(String state, String detail) {
        var user = User.withUsername("alice")
                .password("password")
                .authorities("ROLE_authenticated")
                .accountLocked(state.equals("locked"))
                .disabled(state.equals("disabled"))
                .accountExpired(state.equals("expired"))
                .credentialsExpired(state.equals("credentials"))
                .build();
        var failure = state.equals("rate")
                ? new TooManyRequestsException(null)
                : (AuthenticationException) UserAccountStatusChecker.check(user)
                        .materialize()
                        .block()
                        .getThrowable();
        var messages = new ReloadableResourceBundleMessageSource();
        messages.setBasename("file:src/main/resources/config/i18n/messages");
        messages.setDefaultEncoding("UTF-8");
        var enhancer = mock(LoginHandlerEnhancer.class);
        var context = mock(ServerResponse.Context.class);
        when(context.messageWriters())
                .thenReturn(HandlerStrategies.withDefaults().messageWriters());
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/login")
                .accept(MediaType.APPLICATION_JSON)
                .acceptLanguageAsLocales(Locale.CHINESE));
        when(enhancer.onLoginFailure(exchange, failure)).thenReturn(Mono.empty());
        var handler = new LoginFailureHandler("password", context, messages, enhancer);
        handler.onAuthenticationFailure(new WebFilterExchange(exchange, e -> Mono.empty()), failure)
                .block();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exchange.getResponse().getBodyAsString().block()).contains("\"detail\":\"" + detail + "\"");
    }
}
