package run.halo.app.notification.endpoint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.data.domain.Sort.Order.asc;
import static org.springframework.data.domain.Sort.Order.desc;

import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.notification.NotifierDescriptor;
import run.halo.app.core.extension.notification.ReasonType;
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.notification.UserNotificationPreferenceService;

/**
 * Tests for {@link UserNotificationPreferencesEndpoint}.
 *
 * @author guqing
 * @since 2.10.0
 */
@ExtendWith(MockitoExtension.class)
class UserNotificationPreferencesEndpointTest {

    @Mock
    private ReactiveExtensionClient client;

    @Mock
    private UserNotificationPreferenceService userNotificationPreferenceService;

    @InjectMocks
    private UserNotificationPreferencesEndpoint userNotificationPreferencesEndpoint;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToRouterFunction(userNotificationPreferencesEndpoint.endpoint())
                .build();

        when(client.listAll(
                        eq(ReasonType.class),
                        assertArg(option -> assertThat(option.toString())
                                .isEqualTo("NOT EXISTS metadata.labels['halo.run/hidden']")),
                        eq(ExtensionUtil.defaultSort())))
                .thenReturn(Flux.empty());
    }

    @Test
    void listNotificationPreferences() {
        when(client.listAll(eq(NotifierDescriptor.class), any(), any())).thenReturn(Flux.empty());
        when(userNotificationPreferenceService.getByUser(any())).thenReturn(Mono.empty());
        webTestClient
                .post()
                .uri("/userspaces/{username}/notification-preferences", "guqing")
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    void shouldListNotifiersOrderedByCreationTimeDescThenNameAsc() {
        var newer = notifierDescriptor("newer-notifier", Instant.parse("2024-01-02T00:00:00Z"));
        var older = notifierDescriptor("older-notifier", Instant.parse("2024-01-01T00:00:00Z"));
        when(client.listAll(
                        eq(NotifierDescriptor.class),
                        any(),
                        assertArg(sort -> assertThat(sort)
                                .isEqualTo(Sort.by(desc("metadata.creationTimestamp"), asc("metadata.name"))))))
                .thenReturn(Flux.just(newer, older));
        when(userNotificationPreferenceService.getByUser(any())).thenReturn(Mono.empty());

        webTestClient
                .get()
                .uri("/userspaces/{username}/notification-preferences", "guqing")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.notifiers[0].name")
                .isEqualTo("newer-notifier")
                .jsonPath("$.notifiers[1].name")
                .isEqualTo("older-notifier");
    }

    @Test
    void saveNotificationPreferences() {
        when(client.listAll(eq(NotifierDescriptor.class), any(), any())).thenReturn(Flux.empty());
        when(userNotificationPreferenceService.getByUser(any())).thenReturn(Mono.empty());
        webTestClient
                .post()
                .uri("/userspaces/{username}/notification-preferences", "guqing")
                .exchange()
                .expectStatus()
                .isOk();
    }

    private static NotifierDescriptor notifierDescriptor(String name, Instant creationTimestamp) {
        var notifier = new NotifierDescriptor();
        notifier.setMetadata(new Metadata());
        notifier.getMetadata().setName(name);
        notifier.getMetadata().setCreationTimestamp(creationTimestamp);
        notifier.setSpec(new NotifierDescriptor.Spec());
        notifier.getSpec().setDisplayName(name);
        return notifier;
    }
}
