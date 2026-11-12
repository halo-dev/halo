package run.halo.app.infra;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
 * Tests for {@link InitializationStateGetter}.
 *
 * @author guqing
 * @since 2.9.0
 */
@ExtendWith(MockitoExtension.class)
class InitializationStateGetterTest {
    @Mock
    private ReactiveExtensionClient client;

    @InjectMocks
    private DefaultInitializationStateGetter initializationStateGetter;

    @Test
    void setupRequiredForFreshInstallation() {
        when(client.listBy(eq(User.class), any(), any(PageRequest.class)))
                .thenReturn(Mono.just(new ListResult<>(List.of())));
        when(client.fetch(eq(ConfigMap.class), eq(SystemState.SYSTEM_STATES_CONFIGMAP)))
                .thenReturn(Mono.empty());

        initializationStateGetter
                .setupRequired()
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void setupRequiredForInterruptedSetup() {
        ConfigMap configMap = new ConfigMap();
        configMap.setMetadata(new Metadata());
        configMap.getMetadata().setName(SystemState.SYSTEM_STATES_CONFIGMAP);
        configMap.setData(Map.of("states", "{\"isSetup\":false,\"setupUsername\":\"admin\"}"));
        when(client.fetch(eq(ConfigMap.class), eq(SystemState.SYSTEM_STATES_CONFIGMAP)))
                .thenReturn(Mono.just(configMap));

        initializationStateGetter
                .setupRequired()
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();

        // an interrupted setup must not be cached as completed
        initializationStateGetter
                .setupRequired()
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();
        verify(client, times(2)).fetch(eq(ConfigMap.class), eq(SystemState.SYSTEM_STATES_CONFIGMAP));
        verify(client, never()).listBy(eq(User.class), any(), any(PageRequest.class));
    }

    @Test
    void setupRequiredWhenConfigMapHasNoSetupState() {
        when(client.listBy(eq(User.class), any(), any(PageRequest.class)))
                .thenReturn(Mono.just(new ListResult<>(List.of())));
        ConfigMap configMap = new ConfigMap();
        configMap.setMetadata(new Metadata());
        configMap.getMetadata().setName(SystemState.SYSTEM_STATES_CONFIGMAP);
        configMap.setData(Map.of());
        when(client.fetch(eq(ConfigMap.class), eq(SystemState.SYSTEM_STATES_CONFIGMAP)))
                .thenReturn(Mono.just(configMap));

        initializationStateGetter
                .setupRequired()
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void setupNotRequiredForLegacyInstallationWithoutState() {
        when(client.fetch(ConfigMap.class, SystemState.SYSTEM_STATES_CONFIGMAP)).thenReturn(Mono.empty());
        when(client.listBy(eq(User.class), any(), any(PageRequest.class)))
                .thenReturn(Mono.just(new ListResult<>(List.of(new User()))));

        StepVerifier.create(initializationStateGetter.setupRequired())
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void setupNotRequiredForLegacyInstallationWithUnsetState() {
        var configMap = new ConfigMap();
        configMap.setData(Map.of("states", "{}"));
        when(client.fetch(ConfigMap.class, SystemState.SYSTEM_STATES_CONFIGMAP)).thenReturn(Mono.just(configMap));
        when(client.listBy(eq(User.class), any(), any(PageRequest.class)))
                .thenReturn(Mono.just(new ListResult<>(List.of(new User()))));

        StepVerifier.create(initializationStateGetter.setupRequired())
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void shouldPropagateUserLookupFailureWithoutAllowingSetup() {
        when(client.fetch(ConfigMap.class, SystemState.SYSTEM_STATES_CONFIGMAP)).thenReturn(Mono.empty());
        when(client.listBy(eq(User.class), any(), any(PageRequest.class)))
                .thenReturn(Mono.error(new IllegalStateException("Unavailable")));

        StepVerifier.create(initializationStateGetter.setupRequired()).verifyError(IllegalStateException.class);
    }

    @Test
    void setupNotRequiredForCompletedSetup() {
        ConfigMap configMap = new ConfigMap();
        configMap.setMetadata(new Metadata());
        configMap.getMetadata().setName(SystemState.SYSTEM_STATES_CONFIGMAP);
        configMap.setData(Map.of("states", "{\"isSetup\":true,\"setupUsername\":\"admin\"}"));
        when(client.fetch(eq(ConfigMap.class), eq(SystemState.SYSTEM_STATES_CONFIGMAP)))
                .thenReturn(Mono.just(configMap));

        initializationStateGetter
                .setupRequired()
                .as(StepVerifier::create)
                .expectNext(false)
                .verifyComplete();

        // the result should be cached
        initializationStateGetter
                .setupRequired()
                .as(StepVerifier::create)
                .expectNext(false)
                .verifyComplete();
        verify(client, times(1)).fetch(eq(ConfigMap.class), eq(SystemState.SYSTEM_STATES_CONFIGMAP));
    }
}
