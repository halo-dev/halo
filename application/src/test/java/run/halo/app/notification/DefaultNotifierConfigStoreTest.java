package run.halo.app.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static run.halo.app.notification.DefaultNotifierConfigStore.RECEIVER_KEY;
import static run.halo.app.notification.DefaultNotifierConfigStore.SECRET_NAME;
import static run.halo.app.notification.DefaultNotifierConfigStore.SENDER_KEY;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Secret;
import run.halo.app.infra.utils.JsonUtils;

/**
 * Tests for {@link DefaultNotifierConfigStore}.
 *
 * @author guqing
 * @since 2.9.0
 */
@ExtendWith(MockitoExtension.class)
class DefaultNotifierConfigStoreTest {

    @Mock
    private ReactiveExtensionClient client;

    @InjectMocks
    DefaultNotifierConfigStore notifierConfigStore;

    @Test
    void fetchReceiverConfigTest() {
        var objectNode = mock(ObjectNode.class);
        var spyNotifierConfigStore = spy(notifierConfigStore);

        doReturn(Mono.just(objectNode)).when(spyNotifierConfigStore)
            .fetchConfig(eq("fake-notifier"));
        var receiverConfig = mock(ObjectNode.class);
        when(objectNode.get(eq(RECEIVER_KEY))).thenReturn(receiverConfig);

        spyNotifierConfigStore.fetchReceiverConfig("fake-notifier")
            .as(StepVerifier::create)
            .consumeNextWith(actual -> assertThat(actual).isEqualTo(receiverConfig))
            .verifyComplete();
        verify(objectNode).get(eq(RECEIVER_KEY));
    }

    @Test
    void fetchSenderConfigTest() {
        var objectNode = mock(ObjectNode.class);
        var spyNotifierConfigStore = spy(notifierConfigStore);

        doReturn(Mono.just(objectNode)).when(spyNotifierConfigStore)
            .fetchConfig(eq("fake-notifier"));
        var senderConfig = mock(ObjectNode.class);
        when(objectNode.get(eq(DefaultNotifierConfigStore.SENDER_KEY))).thenReturn(senderConfig);

        spyNotifierConfigStore.fetchSenderConfig("fake-notifier")
            .as(StepVerifier::create)
            .consumeNextWith(actual -> assertThat(actual).isEqualTo(senderConfig))
            .verifyComplete();
        verify(objectNode).get(eq(DefaultNotifierConfigStore.SENDER_KEY));
    }

    @Test
    void fetchConfigWhenSecretNotFound() {
        var spyNotifierConfigStore = spy(notifierConfigStore);

        var objectNode = JsonNodeFactory.instance.objectNode();
        doReturn(Mono.just(objectNode)).when(spyNotifierConfigStore)
            .fetchConfig(eq("fake-notifier"));

        spyNotifierConfigStore.fetchSenderConfig("fake-notifier")
            .as(StepVerifier::create)
            .consumeNextWith(actual -> assertThat(actual).isNotNull())
            .verifyComplete();

        spyNotifierConfigStore.fetchReceiverConfig("fake-notifier")
            .as(StepVerifier::create)
            .consumeNextWith(actual -> assertThat(actual).isNotNull())
            .verifyComplete();
    }

    @Test
    void saveReceiverConfigTest() {
        var receiverConfig = mock(ObjectNode.class);
        var spyNotifierConfigStore = spy(notifierConfigStore);

        doReturn(Mono.empty()).when(spyNotifierConfigStore)
            .saveConfig(eq("fake-notifier"), eq(RECEIVER_KEY), eq(receiverConfig));

        spyNotifierConfigStore.saveReceiverConfig("fake-notifier", receiverConfig)
            .as(StepVerifier::create)
            .verifyComplete();

        verify(spyNotifierConfigStore)
            .saveConfig(eq("fake-notifier"), eq(RECEIVER_KEY), eq(receiverConfig));
    }

    @Test
    void saveSenderConfigTest() {
        var senderConfig = mock(ObjectNode.class);
        var spyNotifierConfigStore = spy(notifierConfigStore);

        doReturn(Mono.empty()).when(spyNotifierConfigStore)
            .saveConfig(eq("fake-notifier"), eq(SENDER_KEY), eq(senderConfig));

        spyNotifierConfigStore.saveSenderConfig("fake-notifier", senderConfig)
            .as(StepVerifier::create)
            .verifyComplete();

        verify(spyNotifierConfigStore)
            .saveConfig(eq("fake-notifier"), eq(SENDER_KEY), eq(senderConfig));

    }

    @Test
    void saveConfigTest() {
        when(client.fetch(eq(Secret.class), eq(SECRET_NAME))).thenReturn(Mono.empty());

        when(client.create(any(Secret.class)))
            .thenAnswer(answer -> Mono.just(answer.getArgument(0, Secret.class)));
        when(client.update(any(Secret.class)))
            .thenAnswer(answer -> Mono.just(answer.getArgument(0, Secret.class)));

        var objectNode = JsonNodeFactory.instance.objectNode();
        objectNode.put("k1", "v1");
        notifierConfigStore.saveConfig("fake-notifier", "fake-key", objectNode)
            .as(StepVerifier::create)
            .verifyComplete();

        verify(client).fetch(eq(Secret.class), eq(SECRET_NAME));
        verify(client).create(assertArg(arg -> {
            assertThat(arg).isInstanceOf(Secret.class);
            var secret = (Secret) arg;
            assertThat(secret.getMetadata().getName()).isEqualTo(SECRET_NAME);
            assertThat(secret.getMetadata().getFinalizers())
                .contains(MetadataUtil.SYSTEM_FINALIZER);
            assertThat(secret.getStringData()).isNotNull();
        }));
        verify(client).update(assertArg(arg -> {
            assertThat(arg).isInstanceOf(Secret.class);
            var secret = (Secret) arg;
            assertThat(secret.getStringData().get("fake-notifier.json"))
                .isEqualTo("{\"fake-key\":{\"k1\":\"v1\"}}");
        }));
    }

    @Test
    void fetchConfigTest() {
        String s = "{\"fake-key\":{\"k1\":\"v1\"}}";
        var objectNode = JsonUtils.jsonToObject(s, ObjectNode.class);
        var secret = new Secret();
        secret.setStringData(Map.of("fake-notifier.json", s));
        when(client.fetch(eq(Secret.class), eq(SECRET_NAME)))
            .thenReturn(Mono.just(secret));
        notifierConfigStore.fetchConfig("fake-notifier")
            .as(StepVerifier::create)
            .consumeNextWith(actual -> assertThat(actual).isEqualTo(objectNode))
            .verifyComplete();
    }

    @Test
    void resolveKeyTest() {
        assertThat(notifierConfigStore.resolveKey("fake-notifier"))
            .isEqualTo("fake-notifier.json");
        assertThat(notifierConfigStore.resolveKey("other-notifier"))
            .isEqualTo("other-notifier.json");
    }
}