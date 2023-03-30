package run.halo.app.theme.endpoint;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.service.UserService;
import run.halo.app.extension.Metadata;

/**
 * Tests for {@link PublicUserEndpoint}.
 *
 * @author guqing
 * @since 2.4.0
 */
@ExtendWith(MockitoExtension.class)
class PublicUserEndpointTest {
    @Mock
    private UserService userService;
    @Mock
    private ServerSecurityContextRepository securityContextRepository;
    @Mock
    private ReactiveUserDetailsService reactiveUserDetailsService;

    @InjectMocks
    private PublicUserEndpoint publicUserEndpoint;

    private WebTestClient webClient;

    @BeforeEach
    void setUp() {
        webClient = WebTestClient.bindToRouterFunction(publicUserEndpoint.endpoint())
            .build();
    }

    @Test
    void signUp() {
        User user = new User();
        user.setMetadata(new Metadata());
        user.getMetadata().setName("fake-user");
        user.setSpec(new User.UserSpec());
        user.getSpec().setDisplayName("hello");
        user.getSpec().setBio("bio");

        when(userService.signUp(any(User.class), anyString())).thenReturn(Mono.just(user));
        when(securityContextRepository.save(any(), any())).thenReturn(Mono.empty());
        when(reactiveUserDetailsService.findByUsername(anyString())).thenReturn(Mono.just(
            org.springframework.security.core.userdetails.User.withUsername("fake-user")
                .password("123456")
                .authorities("test-role")
                .build()));

        webClient.post()
            .uri("/users/-/signup")
            .bodyValue(new PublicUserEndpoint.SignUpRequest(user, "fake-password"))
            .exchange()
            .expectStatus().isOk();

        verify(userService).signUp(any(User.class), anyString());
        verify(securityContextRepository).save(any(), any());
        verify(reactiveUserDetailsService).findByUsername(eq("fake-user"));
    }
}