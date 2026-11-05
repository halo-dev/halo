package run.halo.app.infra;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.Metadata;
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
    }

    @Test
    void setupRequiredWhenConfigMapHasNoSetupState() {
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
