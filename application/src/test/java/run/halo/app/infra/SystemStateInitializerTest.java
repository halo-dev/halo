package run.halo.app.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.User;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.PageRequest;
import run.halo.app.extension.ReactiveExtensionClient;

/**
 * Tests for {@link SystemStateInitializer}.
 *
 * @author johnniang
 * @since 2.28.0
 */
@ExtendWith(MockitoExtension.class)
class SystemStateInitializerTest {

    @Mock
    private ReactiveExtensionClient client;

    @InjectMocks
    private SystemStateInitializer systemStateInitializer;

    @Test
    void shouldBackfillSystemStateForLegacyInstallation() {
        when(client.fetch(eq(ConfigMap.class), eq(SystemState.SYSTEM_STATES_CONFIGMAP)))
                .thenReturn(Mono.empty());
        when(client.listBy(eq(User.class), any(), any(PageRequest.class)))
                .thenReturn(Mono.just(new ListResult<>(List.of(new User()))));
        when(client.create(any(ConfigMap.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(client.update(any(ConfigMap.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(systemStateInitializer.onApplicationEvent(mock(ExtensionInitializedEvent.class)))
                .verifyComplete();

        var configMapCaptor = ArgumentCaptor.forClass(ConfigMap.class);
        verify(client).update(configMapCaptor.capture());
        assertThat(SystemState.deserialize(configMapCaptor.getValue()).getIsSetup())
                .isTrue();
    }

    @Test
    void shouldNotBackfillForFreshInstallation() {
        when(client.fetch(eq(ConfigMap.class), eq(SystemState.SYSTEM_STATES_CONFIGMAP)))
                .thenReturn(Mono.empty());
        when(client.listBy(eq(User.class), any(), any(PageRequest.class))).thenReturn(Mono.empty());

        StepVerifier.create(systemStateInitializer.onApplicationEvent(mock(ExtensionInitializedEvent.class)))
                .verifyComplete();

        verify(client, never()).create(any(ConfigMap.class));
        verify(client, never()).update(any(ConfigMap.class));
    }

    @Test
    void shouldNotBackfillWhenStateWasSet() {
        var configMap = new ConfigMap();
        configMap.setMetadata(new Metadata());
        configMap.getMetadata().setName(SystemState.SYSTEM_STATES_CONFIGMAP);
        configMap.setData(new HashMap<>(Map.of("states", "{\"isSetup\":true}")));
        when(client.fetch(eq(ConfigMap.class), eq(SystemState.SYSTEM_STATES_CONFIGMAP)))
                .thenReturn(Mono.just(configMap));

        StepVerifier.create(systemStateInitializer.onApplicationEvent(mock(ExtensionInitializedEvent.class)))
                .verifyComplete();

        verify(client, never()).create(any(ConfigMap.class));
        verify(client, never()).update(any(ConfigMap.class));
    }

    @Test
    void shouldNotOverrideStateOfInterruptedSetup() {
        var existingState = new ConfigMap();
        existingState.setMetadata(new Metadata());
        existingState.getMetadata().setName(SystemState.SYSTEM_STATES_CONFIGMAP);
        existingState.setData(new HashMap<>(Map.of("states", "{\"isSetup\":false}")));
        when(client.fetch(eq(ConfigMap.class), eq(SystemState.SYSTEM_STATES_CONFIGMAP)))
                .thenReturn(Mono.empty(), Mono.just(existingState));
        when(client.listBy(eq(User.class), any(), any(PageRequest.class)))
                .thenReturn(Mono.just(new ListResult<>(List.of(new User()))));
        when(client.update(any(ConfigMap.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(systemStateInitializer.onApplicationEvent(mock(ExtensionInitializedEvent.class)))
                .verifyComplete();

        var configMapCaptor = ArgumentCaptor.forClass(ConfigMap.class);
        verify(client).update(configMapCaptor.capture());
        assertThat(SystemState.deserialize(configMapCaptor.getValue()).getIsSetup())
                .isFalse();
        verify(client, never()).create(any(ConfigMap.class));
    }
}
