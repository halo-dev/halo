package run.halo.app.security.preauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.result.view.View;
import org.thymeleaf.messageresolver.StandardMessageResolver;
import org.thymeleaf.spring6.SpringWebFluxTemplateEngine;
import org.thymeleaf.spring6.view.reactive.ThymeleafReactiveView;
import org.thymeleaf.spring6.view.reactive.ThymeleafReactiveViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import reactor.core.publisher.Mono;
import run.halo.app.core.user.service.UserConnectionService;
import run.halo.app.core.user.service.UserService;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.security.LoginHandlerEnhancer;
import run.halo.app.security.authentication.oauth2.OAuth2AuthenticationTokenCache;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OAuth2RegisterPageRenderTest {

    @Mock
    OAuth2AuthenticationTokenCache authenticationCache;

    @Mock
    UserService userService;

    @Mock
    UserConnectionService connectionService;

    @Mock
    ReactiveUserDetailsService userDetailsService;

    @Mock
    ServerSecurityContextRepository securityContextRepository;

    @Mock
    LoginHandlerEnhancer loginHandlerEnhancer;

    @Mock
    SystemConfigFetcher systemConfigFetcher;

    @Mock
    AgreementPageFetcher agreementPageFetcher;

    WebTestClient webClient;

    @BeforeEach
    void setUp() {
        var validatorFactory = new org.springframework.validation.beanvalidation.LocalValidatorFactoryBean();
        validatorFactory.afterPropertiesSet();

        var endpoint = new PreAuthOAuth2Endpoint(
                authenticationCache,
                userService,
                connectionService,
                userDetailsService,
                securityContextRepository,
                loginHandlerEnhancer,
                systemConfigFetcher,
                validatorFactory,
                agreementPageFetcher);
        endpoint.setClock(Clock.fixed(Instant.parse("2026-07-31T10:00:00Z"), ZoneOffset.UTC));

        when(authenticationCache.getToken(any())).thenReturn(Mono.empty());
        when(agreementPageFetcher.fetchAgreementPages()).thenReturn(Mono.just(List.of()));
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(userSetting(false)));
        when(userService.findUserByVerifiedEmail(anyString())).thenReturn(Mono.empty());
        when(connectionService.getByProviderUserId(anyString(), anyString())).thenReturn(Mono.empty());
        when(securityContextRepository.save(any(), any())).thenReturn(Mono.empty());
        when(loginHandlerEnhancer.onLoginSuccess(any(), any())).thenReturn(Mono.empty());
        when(authenticationCache.removeToken(any())).thenReturn(Mono.empty());

        var appContext = new StaticApplicationContext();
        appContext.refresh();

        var templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("HTML");

        var engine = new SpringWebFluxTemplateEngine();
        engine.setTemplateResolver(templateResolver);
        engine.setMessageResolver(new StandardMessageResolver());

        var viewResolver = new TestViewResolver();
        viewResolver.setTemplateEngine(engine);
        viewResolver.setApplicationContext(appContext);

        var handlerStrategies =
                HandlerStrategies.builder().viewResolver(viewResolver).build();
        webClient = WebTestClient.bindToRouterFunction(endpoint.preAuthOAuth2Endpoints())
                .handlerStrategies(handlerStrategies)
                .build();
    }

    private OAuth2AuthenticationToken token() {
        var oauth2User = new DefaultOAuth2User(
                List.of(),
                Map.of(
                        "login", "johnniang",
                        "name", "John Niang",
                        "email", "john@example.com"),
                "login");
        return new OAuth2AuthenticationToken(oauth2User, List.of(), "github");
    }

    private SystemSetting.User userSetting(boolean allowRegistration) {
        var setting = new SystemSetting.User();
        setting.setAllowRegistration(allowRegistration);
        setting.setDefaultRole("test-role");
        return setting;
    }

    @Test
    void shouldRenderRegisterPageWithPrefilledUsername() {
        when(authenticationCache.getToken(any())).thenReturn(Mono.just(token()));

        webClient
                .get()
                .uri("/login/oauth2/register")
                .header("Accept-Language", "zh-CN")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .value(body -> {
                    assertThat(body).contains("注册账号");
                    assertThat(body).contains("value=\"johnniang\"");
                });
    }

    @Test
    void shouldRenderAgreementPagesAsLinks() {
        when(authenticationCache.getToken(any())).thenReturn(Mono.just(token()));
        var page = new HashMap<String, String>();
        page.put("title", "Terms of Service");
        page.put("permalink", "https://example.com/terms");
        when(agreementPageFetcher.fetchAgreementPages()).thenReturn(Mono.just(List.of(page)));

        webClient
                .get()
                .uri("/login/oauth2/register")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .value(body -> {
                    assertThat(body).contains("Terms of Service");
                    assertThat(body).contains("https://example.com/terms");
                });
    }

    @Test
    void shouldRenderRegistrationDisabledErrorInSpanish() {
        when(authenticationCache.getToken(any())).thenReturn(Mono.just(token()));

        webClient
                .post()
                .uri("/login/oauth2/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header("Accept-Language", "es")
                .bodyValue("username=johnniang&displayName=John+Niang")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("El registro ha sido deshabilitado por el administrador."));
    }

    static class TestViewResolver extends ThymeleafReactiveViewResolver {

        @Override
        protected Mono<View> loadView(String viewName, Locale locale) {
            return super.loadView(viewName, locale)
                    .cast(ThymeleafReactiveView.class)
                    .map(view -> {
                        view.addStaticVariable("site", Map.of("title", "Halo", "version", "1.0", "favicon", ""));
                        view.addStaticVariable("publicKey", "");
                        view.addStaticVariable("fragmentTemplateName", "");
                        return view;
                    });
        }
    }
}
