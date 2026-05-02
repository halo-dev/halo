package run.halo.app.security.authentication.oauth2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.UserConnection;
import run.halo.app.core.user.service.UserConnectionService;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.security.LoginHandlerEnhancer;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MapOAuth2AuthenticationFilterTest {

    @Mock
    ServerSecurityContextRepository securityContextRepository;

    @Mock
    UserConnectionService connectionService;

    @Mock
    ReactiveUserDetailsService userDetailsService;

    @Mock
    LoginHandlerEnhancer loginHandlerEnhancer;

    @Mock
    ReactiveExtensionClient client;

    @Mock
    SystemConfigFetcher systemConfigFetcher;

    @Mock
    AuthenticationTrustResolver authenticationTrustResolver;

    MapOAuth2AuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new MapOAuth2AuthenticationFilter(
            securityContextRepository, connectionService, userDetailsService,
            loginHandlerEnhancer, client, systemConfigFetcher
        );
        filter.setAuthenticationTrustResolver(authenticationTrustResolver);
    }

    @Test
    void shouldAutoRegisterWhenNoConnectionAndNoPreAuth() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/login").build());
        var chain = org.mockito.Mockito.mock(WebFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        var oauth2User = new DefaultOAuth2User(
            List.of(),
            Map.of("name", "octocat", "email", "octocat@github.com"),
            "name"
        );
        var oauth2Token = new OAuth2AuthenticationToken(oauth2User, List.of(), "github");

        when(authenticationTrustResolver.isAuthenticated(any(Authentication.class)))
            .thenReturn(true);

        var createdUser = new run.halo.app.core.extension.User();
        var createdMetadata = new Metadata();
        createdMetadata.setName("user-abc123");
        createdUser.setMetadata(createdMetadata);

        var connection = new UserConnection();
        var connectionMetadata = new Metadata();
        connectionMetadata.setName("conn-1");
        connection.setMetadata(connectionMetadata);
        var connectionSpec = new UserConnection.UserConnectionSpec();
        connectionSpec.setUsername("user-abc123");
        connection.setSpec(connectionSpec);

        var userSetting = new SystemSetting.User();
        userSetting.setDefaultRole("test-role");

        when(connectionService.updateUserConnectionIfPresent("github", oauth2User))
            .thenReturn(Mono.empty());
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
            .thenReturn(Mono.just(userSetting));
        when(client.create(any(run.halo.app.core.extension.User.class)))
            .thenReturn(Mono.just(createdUser));
        when(client.create(any(run.halo.app.core.extension.RoleBinding.class)))
            .thenReturn(Mono.just(new run.halo.app.core.extension.RoleBinding()));
        when(connectionService.createUserConnection("user-abc123", "github", oauth2User))
            .thenReturn(Mono.just(connection));

        var userDetails = User.withUsername("user-abc123").password("").roles("test-role")
            .build();
        when(userDetailsService.findByUsername("user-abc123"))
            .thenReturn(Mono.just(userDetails));

        when(securityContextRepository.save(any(), any())).thenReturn(Mono.empty());
        when(loginHandlerEnhancer.onLoginSuccess(any(), any())).thenReturn(Mono.empty());

        var securityContext = new SecurityContextImpl(oauth2Token);

        StepVerifier.create(
            filter.filter(exchange, chain)
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(
                    Mono.just(securityContext)))
        ).verifyComplete();

        verify(userDetailsService).findByUsername("user-abc123");
        verify(connectionService).createUserConnection("user-abc123", "github", oauth2User);

        var userCaptor = ArgumentCaptor.forClass(run.halo.app.core.extension.User.class);
        verify(client).create(userCaptor.capture());
        var capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getMetadata().getGenerateName()).isEqualTo("user-");
        assertThat(capturedUser.getSpec().getDisplayName()).isEqualTo("octocat");
        assertThat(capturedUser.getSpec().getEmail()).isEqualTo("octocat@github.com");
        assertThat(capturedUser.getSpec().isEmailVerified()).isTrue();
    }

    @Test
    void shouldAutoRegisterWithFallbackDisplayNameWhenNoNameAttribute() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/login").build());
        var chain = org.mockito.Mockito.mock(WebFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        var oauth2User = new DefaultOAuth2User(
            List.of(),
            Map.of("login", "octocat", "email", "octocat@github.com"),
            "login"
        );
        var oauth2Token = new OAuth2AuthenticationToken(oauth2User, List.of(), "github");

        when(authenticationTrustResolver.isAuthenticated(any(Authentication.class)))
            .thenReturn(true);

        var createdUser = new run.halo.app.core.extension.User();
        var createdMetadata = new Metadata();
        createdMetadata.setName("user-def456");
        createdUser.setMetadata(createdMetadata);

        var connection = new UserConnection();
        var connectionMetadata = new Metadata();
        connectionMetadata.setName("conn-2");
        connection.setMetadata(connectionMetadata);
        var connectionSpec = new UserConnection.UserConnectionSpec();
        connectionSpec.setUsername("user-def456");
        connection.setSpec(connectionSpec);

        var userSetting = new SystemSetting.User();
        userSetting.setDefaultRole("test-role");

        when(connectionService.updateUserConnectionIfPresent("github", oauth2User))
            .thenReturn(Mono.empty());
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
            .thenReturn(Mono.just(userSetting));
        when(client.create(any(run.halo.app.core.extension.User.class)))
            .thenReturn(Mono.just(createdUser));
        when(client.create(any(run.halo.app.core.extension.RoleBinding.class)))
            .thenReturn(Mono.just(new run.halo.app.core.extension.RoleBinding()));
        when(connectionService.createUserConnection("user-def456", "github", oauth2User))
            .thenReturn(Mono.just(connection));

        var userDetails = User.withUsername("user-def456").password("").roles("test-role")
            .build();
        when(userDetailsService.findByUsername("user-def456"))
            .thenReturn(Mono.just(userDetails));
        when(securityContextRepository.save(any(), any())).thenReturn(Mono.empty());
        when(loginHandlerEnhancer.onLoginSuccess(any(), any())).thenReturn(Mono.empty());

        var securityContext = new SecurityContextImpl(oauth2Token);

        StepVerifier.create(
            filter.filter(exchange, chain)
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(
                    Mono.just(securityContext)))
        ).verifyComplete();

        var userCaptor = ArgumentCaptor.forClass(run.halo.app.core.extension.User.class);
        verify(client).create(userCaptor.capture());
        assertThat(userCaptor.getValue().getSpec().getDisplayName()).isEqualTo("octocat");
    }

    @Test
    void shouldAutoRegisterWithNullEmailWhenNoEmailAttribute() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/login").build());
        var chain = org.mockito.Mockito.mock(WebFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        var oauth2User = new DefaultOAuth2User(
            List.of(),
            Map.of("name", "noemailuser"),
            "name"
        );
        var oauth2Token = new OAuth2AuthenticationToken(oauth2User, List.of(), "github");

        when(authenticationTrustResolver.isAuthenticated(any(Authentication.class)))
            .thenReturn(true);

        var createdUser = new run.halo.app.core.extension.User();
        var createdMetadata = new Metadata();
        createdMetadata.setName("user-ghi789");
        createdUser.setMetadata(createdMetadata);

        var connection = new UserConnection();
        var connectionMetadata = new Metadata();
        connectionMetadata.setName("conn-3");
        connection.setMetadata(connectionMetadata);
        var connectionSpec = new UserConnection.UserConnectionSpec();
        connectionSpec.setUsername("user-ghi789");
        connection.setSpec(connectionSpec);

        var userSetting = new SystemSetting.User();
        userSetting.setDefaultRole("test-role");

        when(connectionService.updateUserConnectionIfPresent("github", oauth2User))
            .thenReturn(Mono.empty());
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
            .thenReturn(Mono.just(userSetting));
        when(client.create(any(run.halo.app.core.extension.User.class)))
            .thenReturn(Mono.just(createdUser));
        when(client.create(any(run.halo.app.core.extension.RoleBinding.class)))
            .thenReturn(Mono.just(new run.halo.app.core.extension.RoleBinding()));
        when(connectionService.createUserConnection("user-ghi789", "github", oauth2User))
            .thenReturn(Mono.just(connection));

        var userDetails = User.withUsername("user-ghi789").password("").roles("test-role")
            .build();
        when(userDetailsService.findByUsername("user-ghi789"))
            .thenReturn(Mono.just(userDetails));
        when(securityContextRepository.save(any(), any())).thenReturn(Mono.empty());
        when(loginHandlerEnhancer.onLoginSuccess(any(), any())).thenReturn(Mono.empty());

        var securityContext = new SecurityContextImpl(oauth2Token);

        StepVerifier.create(
            filter.filter(exchange, chain)
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(
                    Mono.just(securityContext)))
        ).verifyComplete();

        var userCaptor = ArgumentCaptor.forClass(run.halo.app.core.extension.User.class);
        verify(client).create(userCaptor.capture());
        var capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getSpec().getEmail()).isNull();
        assertThat(capturedUser.getSpec().isEmailVerified()).isFalse();
    }

    @Test
    void shouldErrorWhenNoDefaultRoleConfigured() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/login").build());
        var chain = org.mockito.Mockito.mock(WebFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        var oauth2User = new DefaultOAuth2User(
            List.of(),
            Map.of("name", "octocat"),
            "name"
        );
        var oauth2Token = new OAuth2AuthenticationToken(oauth2User, List.of(), "github");

        when(authenticationTrustResolver.isAuthenticated(any(Authentication.class)))
            .thenReturn(true);

        var userSetting = new SystemSetting.User();
        userSetting.setDefaultRole(null);

        when(connectionService.updateUserConnectionIfPresent("github", oauth2User))
            .thenReturn(Mono.empty());
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
            .thenReturn(Mono.just(userSetting));

        var securityContext = new SecurityContextImpl(oauth2Token);

        StepVerifier.create(
            filter.filter(exchange, chain)
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(
                    Mono.just(securityContext)))
        ).verifyError(IllegalStateException.class);
    }

    @Test
    void shouldNotAutoRegisterWhenExistingConnectionFound() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/login").build());
        var chain = org.mockito.Mockito.mock(WebFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        var oauth2User = new DefaultOAuth2User(
            List.of(),
            Map.of("name", "octocat"),
            "name"
        );
        var oauth2Token = new OAuth2AuthenticationToken(oauth2User, List.of(), "github");

        var existingConnection = new UserConnection();
        var connectionMetadata = new Metadata();
        connectionMetadata.setName("existing-conn");
        existingConnection.setMetadata(connectionMetadata);
        var connectionSpec = new UserConnection.UserConnectionSpec();
        connectionSpec.setUsername("existing-user");
        existingConnection.setSpec(connectionSpec);

        when(connectionService.updateUserConnectionIfPresent("github", oauth2User))
            .thenReturn(Mono.just(existingConnection));

        var userDetails = User.withUsername("existing-user").password("").roles("user").build();
        when(userDetailsService.findByUsername("existing-user"))
            .thenReturn(Mono.just(userDetails));
        when(securityContextRepository.save(any(), any())).thenReturn(Mono.empty());
        when(loginHandlerEnhancer.onLoginSuccess(any(), any())).thenReturn(Mono.empty());

        var securityContext = new SecurityContextImpl(oauth2Token);

        StepVerifier.create(
            filter.filter(exchange, chain)
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(
                    Mono.just(securityContext)))
        ).verifyComplete();

        verify(client, never()).create(any());
        verify(connectionService, never()).createUserConnection(any(), any(), any());
    }
}
