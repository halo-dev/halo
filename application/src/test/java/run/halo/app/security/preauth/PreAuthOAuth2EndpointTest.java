package run.halo.app.security.preauth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.result.view.View;
import org.springframework.web.reactive.result.view.ViewResolver;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.UserConnection;
import run.halo.app.core.user.service.UserConnectionService;
import run.halo.app.core.user.service.UserService;
import run.halo.app.extension.Metadata;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.exception.DuplicateNameException;
import run.halo.app.security.LoginHandlerEnhancer;
import run.halo.app.security.authentication.oauth2.OAuth2AuthenticationTokenCache;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PreAuthOAuth2EndpointTest {

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

    Validator validator;

    WebTestClient webClient;

    PreAuthOAuth2Endpoint endpoint;

    @BeforeEach
    void setUp() {
        var validatorFactory = new org.springframework.validation.beanvalidation.LocalValidatorFactoryBean();
        validatorFactory.afterPropertiesSet();
        validator = validatorFactory;

        endpoint = new PreAuthOAuth2Endpoint(
                authenticationCache,
                userService,
                connectionService,
                userDetailsService,
                securityContextRepository,
                loginHandlerEnhancer,
                systemConfigFetcher,
                validator,
                agreementPageFetcher);
        endpoint.setClock(Clock.fixed(Instant.parse("2026-07-31T10:00:00Z"), ZoneOffset.UTC));

        when(agreementPageFetcher.fetchAgreementPages()).thenReturn(Mono.just(List.of()));
        when(authenticationCache.getToken(any())).thenReturn(Mono.empty());
        when(userService.findUserByVerifiedEmail(anyString())).thenReturn(Mono.empty());
        when(connectionService.getByProviderUserId(anyString(), anyString())).thenReturn(Mono.empty());
        when(securityContextRepository.save(any(), any())).thenReturn(Mono.empty());
        when(loginHandlerEnhancer.onLoginSuccess(any(), any())).thenReturn(Mono.empty());
        when(authenticationCache.removeToken(any())).thenReturn(Mono.empty());

        var view = mock(View.class);
        when(view.render(anyMap(), any(), any())).thenReturn(Mono.empty());
        var viewResolver = mock(ViewResolver.class);
        when(viewResolver.resolveViewName(anyString(), any())).thenReturn(Mono.just(view));

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

    private HashMap<String, String> agreementPage(String title, String permalink) {
        var page = new HashMap<String, String>();
        page.put("title", title);
        page.put("permalink", permalink);
        return page;
    }

    @Test
    void shouldRedirectToLoginWhenNoTokenOnChoicePage() {
        webClient
                .get()
                .uri("/login/oauth2")
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .expectHeader()
                .valueEquals("Location", "/login");
    }

    @Test
    void shouldRedirectToLoginWhenNoTokenOnRegisterPage() {
        webClient
                .get()
                .uri("/login/oauth2/register")
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .expectHeader()
                .valueEquals("Location", "/login");
    }

    @Test
    void shouldRedirectToLoginWhenNoTokenOnRegisterSubmit() {
        webClient
                .post()
                .uri("/login/oauth2/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("username=johnniang&displayName=John+Niang")
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .expectHeader()
                .valueEquals("Location", "/login");

        verify(userService, never()).createUser(any(), anySet());
    }

    @Test
    void shouldRenderChoicePageWithToken() {
        when(authenticationCache.getToken(any())).thenReturn(Mono.just(token()));
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(userSetting(true)));

        webClient.get().uri("/login/oauth2").exchange().expectStatus().isOk();
    }

    @Test
    void shouldRenderRegisterPageWithPrefilledData() {
        when(authenticationCache.getToken(any())).thenReturn(Mono.just(token()));

        webClient.get().uri("/login/oauth2/register").exchange().expectStatus().isOk();
    }

    @Test
    void shouldRegisterUserAndBindConnection() {
        when(authenticationCache.getToken(any())).thenReturn(Mono.just(token()));
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(userSetting(true)));

        var createdUser = new User();
        createdUser.setMetadata(new Metadata());
        createdUser.getMetadata().setName("johnniang");
        when(userService.createUser(any(User.class), anySet())).thenReturn(Mono.just(createdUser));

        var connection = new UserConnection();
        connection.setMetadata(new Metadata());
        connection.getMetadata().setName("conn-1");
        var connectionSpec = new UserConnection.UserConnectionSpec();
        connectionSpec.setUsername("johnniang");
        connection.setSpec(connectionSpec);
        when(connectionService.createUserConnection(eq("johnniang"), eq("github"), any()))
                .thenReturn(Mono.just(connection));

        var userDetails = org.springframework.security.core.userdetails.User.withUsername("johnniang")
                .password("")
                .authorities("ROLE_test-role")
                .build();
        when(userDetailsService.findByUsername("johnniang")).thenReturn(Mono.just(userDetails));

        webClient
                .post()
                .uri("/login/oauth2/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("username=johnniang&displayName=John+Niang&email=john@example.com")
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .expectHeader()
                .valueEquals("Location", "/");

        var captor = ArgumentCaptor.forClass(User.class);
        verify(userService).createUser(captor.capture(), eq(Set.of("test-role")));
        var user = captor.getValue();
        org.assertj.core.api.Assertions.assertThat(user.getSpec().getPassword()).isEmpty();
        org.assertj.core.api.Assertions.assertThat(user.getSpec().getEmail()).isEqualTo("john@example.com");
        org.assertj.core.api.Assertions.assertThat(user.getSpec().isEmailVerified())
                .isTrue();
        org.assertj.core.api.Assertions.assertThat(user.getSpec().getDisplayName())
                .isEqualTo("John Niang");

        verify(connectionService).createUserConnection(eq("johnniang"), eq("github"), any());
        verify(securityContextRepository).save(any(), any());
        verify(loginHandlerEnhancer).onLoginSuccess(any(), any());
        verify(authenticationCache).removeToken(any());
    }

    @Test
    void shouldNotCreateUserWhenConnectionAlreadyBound() {
        when(authenticationCache.getToken(any())).thenReturn(Mono.just(token()));
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(userSetting(true)));

        var existingConnection = new UserConnection();
        existingConnection.setMetadata(new Metadata());
        existingConnection.getMetadata().setName("existing-conn");
        var spec = new UserConnection.UserConnectionSpec();
        spec.setUsername("other-user");
        spec.setProviderUserId("johnniang");
        spec.setRegistrationId("github");
        spec.setUpdatedAt(Instant.parse("2026-07-31T09:00:00Z"));
        existingConnection.setSpec(spec);
        when(connectionService.getByProviderUserId(eq("github"), eq("johnniang")))
                .thenReturn(Mono.just(existingConnection));

        var createdUser = new User();
        createdUser.setMetadata(new Metadata());
        createdUser.getMetadata().setName("johnniang");
        when(userService.createUser(any(User.class), anySet())).thenReturn(Mono.just(createdUser));

        var connection = new UserConnection();
        connection.setMetadata(new Metadata());
        connection.getMetadata().setName("conn-1");
        var connectionSpec = new UserConnection.UserConnectionSpec();
        connectionSpec.setUsername("johnniang");
        connection.setSpec(connectionSpec);
        when(connectionService.createUserConnection(eq("johnniang"), eq("github"), any()))
                .thenReturn(Mono.just(connection));

        var userDetails = org.springframework.security.core.userdetails.User.withUsername("johnniang")
                .password("")
                .authorities("ROLE_test-role")
                .build();
        when(userDetailsService.findByUsername("johnniang")).thenReturn(Mono.just(userDetails));

        webClient
                .post()
                .uri("/login/oauth2/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("username=johnniang&displayName=John+Niang")
                .exchange()
                .expectStatus()
                .isOk();

        verify(userService, never()).createUser(any(), anySet());
        verify(connectionService, never()).createUserConnection(anyString(), anyString(), any());
    }

    @Test
    void shouldRejectWhenRegistrationDisabled() {
        when(authenticationCache.getToken(any())).thenReturn(Mono.just(token()));
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(userSetting(false)));

        webClient
                .post()
                .uri("/login/oauth2/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("username=johnniang&displayName=John+Niang")
                .exchange()
                .expectStatus()
                .isOk();

        verify(userService, never()).createUser(any(), anySet());
    }

    @Test
    void shouldRejectUsernameNotMatchingPattern() {
        when(authenticationCache.getToken(any())).thenReturn(Mono.just(token()));
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(userSetting(true)));

        // "John Niang" contains uppercase and space
        webClient
                .post()
                .uri("/login/oauth2/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("username=John+Niang&displayName=John+Niang")
                .exchange()
                .expectStatus()
                .isOk();

        verify(userService, never()).createUser(any(), anySet());
    }

    @Test
    void shouldRejectDuplicateUsername() {
        when(authenticationCache.getToken(any())).thenReturn(Mono.just(token()));
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(userSetting(true)));
        when(userService.createUser(any(User.class), anySet()))
                .thenReturn(Mono.error(() -> new DuplicateNameException(
                        "User name is already in use", null, "problemDetail.user.duplicateName", new Object[] {
                            "johnniang"
                        })));

        webClient
                .post()
                .uri("/login/oauth2/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("username=johnniang&displayName=John+Niang")
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    void shouldRejectEmailAlreadyTaken() {
        when(authenticationCache.getToken(any())).thenReturn(Mono.just(token()));
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(userSetting(true)));

        var existing = new User();
        existing.setMetadata(new Metadata());
        existing.getMetadata().setName("other-user");
        when(userService.findUserByVerifiedEmail("john@example.com")).thenReturn(Mono.just(existing));

        webClient
                .post()
                .uri("/login/oauth2/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("username=johnniang&displayName=John+Niang&email=john@example.com")
                .exchange()
                .expectStatus()
                .isOk();

        verify(userService, never()).createUser(any(), anySet());
    }

    @Test
    void shouldRequireAgreementWhenPagesConfigured() {
        when(authenticationCache.getToken(any())).thenReturn(Mono.just(token()));
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(userSetting(true)));
        when(agreementPageFetcher.fetchAgreementPages())
                .thenReturn(Mono.just(List.of(agreementPage("Terms", "/terms"))));

        webClient
                .post()
                .uri("/login/oauth2/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("username=johnniang&displayName=John+Niang")
                .exchange()
                .expectStatus()
                .isOk();

        verify(userService, never()).createUser(any(), anySet());
    }

    @Test
    void shouldRejectRestrictedUsername() {
        when(authenticationCache.getToken(any())).thenReturn(Mono.just(token()));
        var setting = userSetting(true);
        setting.setProtectedUsernames("admin,administrator");
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(setting));

        webClient
                .post()
                .uri("/login/oauth2/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("username=admin&displayName=John+Niang")
                .exchange()
                .expectStatus()
                .isOk();

        verify(userService, never()).createUser(any(), anySet());
    }
}
