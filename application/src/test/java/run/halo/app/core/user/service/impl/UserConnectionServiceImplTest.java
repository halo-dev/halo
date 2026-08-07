package run.halo.app.core.user.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.UserConnection;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.exception.DuplicateNameException;

@ExtendWith(MockitoExtension.class)
class UserConnectionServiceImplTest {

    @Mock
    ReactiveExtensionClient client;

    @Mock
    ApplicationEventPublisher eventPublisher;

    UserConnectionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserConnectionServiceImpl(client, eventPublisher);
        when(client.listAll(eq(UserConnection.class), any(), any())).thenReturn(Flux.empty());
    }

    @Test
    void shouldRetryCreatingConnectionWhilePreviousObjectIsDeleting() {
        var duplicate = new DuplicateNameException("connection is still being deleted");
        var attempts = new AtomicInteger();
        when(client.create(any(UserConnection.class)))
                .thenAnswer(invocation -> attempts.getAndIncrement() < 2
                        ? Mono.error(duplicate)
                        : Mono.just(invocation.getArgument(0, UserConnection.class)));
        when(client.fetch(eq(UserConnection.class), anyString())).thenReturn(Mono.just(deletingConnection()));

        StepVerifier.withVirtualTime(() -> service.createUserConnection("alice", "github", oauth2User()))
                .thenAwait(Duration.ofMillis(200))
                .assertNext(connection -> {
                    assertThat(connection.getSpec().getUsername()).isEqualTo("alice");
                    assertThat(connection.getSpec().getRegistrationId()).isEqualTo("github");
                    assertThat(connection.getSpec().getProviderUserId()).isEqualTo("external-user");
                })
                .verifyComplete();

        verify(client, times(3)).create(any(UserConnection.class));
    }

    @Test
    void shouldReturnOriginalDuplicateWhenDeletingObjectOutlivesRetryWindow() {
        var duplicate = new DuplicateNameException("connection is still being deleted");
        when(client.create(any(UserConnection.class))).thenReturn(Mono.error(duplicate));
        when(client.fetch(eq(UserConnection.class), anyString())).thenReturn(Mono.just(deletingConnection()));

        StepVerifier.withVirtualTime(() -> service.createUserConnection("alice", "github", oauth2User()))
                .thenAwait(Duration.ofSeconds(2))
                .expectErrorSatisfies(error -> assertThat(error).isSameAs(duplicate))
                .verify();

        verify(client, times(21)).create(any(UserConnection.class));
    }

    private static DefaultOAuth2User oauth2User() {
        return new DefaultOAuth2User(List.of(), Map.of("id", "external-user"), "id");
    }

    private static UserConnection deletingConnection() {
        var connection = new UserConnection();
        var metadata = new Metadata();
        metadata.setName("oauth2-deleting");
        metadata.setDeletionTimestamp(Instant.parse("2026-08-07T00:00:00Z"));
        connection.setMetadata(metadata);
        return connection;
    }
}
