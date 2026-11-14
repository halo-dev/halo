package run.halo.app.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.exception.DuplicateNameException;

/**
 * Tests for {@link SystemState}.
 *
 * @author guqing
 * @since 2.8.0
 */
class SystemStateTest {

    @Test
    void deserialize() {
        ConfigMap configMap = new ConfigMap();
        SystemState systemState = SystemState.deserialize(configMap);
        assertThat(systemState).isNotNull();

        configMap.setData(Map.of(SystemState.GROUP, "{\"isSetup\":true}"));
        systemState = SystemState.deserialize(configMap);
        assertThat(systemState.getIsSetup()).isTrue();
    }

    @Test
    void update() {
        SystemState newSystemState = new SystemState();
        newSystemState.setIsSetup(true);

        ConfigMap configMap = new ConfigMap();
        SystemState.update(newSystemState, configMap);
        assertThat(configMap.getData().get(SystemState.GROUP)).isEqualTo("{\"isSetup\":true}");

        var data = new LinkedHashMap<String, String>();
        configMap.setData(data);
        data.put(SystemState.GROUP, "{\"isSetup\":false}");
        SystemState.update(newSystemState, configMap);
        assertThat(configMap.getData().get(SystemState.GROUP)).isEqualTo("{\"isSetup\":true}");

        data.clear();
        data.put(SystemState.GROUP, "{\"isSetup\":true, \"foo\":\"bar\"}");
        newSystemState.setIsSetup(false);
        SystemState.update(newSystemState, configMap);
        assertThat(configMap.getData().get(SystemState.GROUP)).isEqualTo("{\"isSetup\":false,\"foo\":\"bar\"}");
    }

    @Test
    void shouldRetryWhenConfigMapIsCreatedConcurrently() {
        var client = mock(ReactiveExtensionClient.class);
        var configMap = new ConfigMap();
        configMap.setMetadata(new Metadata());
        configMap.getMetadata().setName(SystemState.SYSTEM_STATES_CONFIGMAP);
        configMap.setData(new LinkedHashMap<>());
        when(client.fetch(ConfigMap.class, SystemState.SYSTEM_STATES_CONFIGMAP))
                .thenReturn(Mono.empty(), Mono.just(configMap));
        when(client.create(any(ConfigMap.class))).thenReturn(Mono.error(new DuplicateNameException("Duplicate name")));
        when(client.update(any(ConfigMap.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(SystemState.upsetSystemState(client, state -> state.setIsSetup(true)))
                .verifyComplete();

        assertThat(SystemState.deserialize(configMap).getIsSetup()).isTrue();
    }

    @Test
    void shouldKeepSetupUsername() {
        var configMap = new ConfigMap();
        var systemState = new SystemState();
        systemState.setIsSetup(false);
        systemState.setSetupUsername("admin");

        SystemState.update(systemState, configMap);

        var deserialized = SystemState.deserialize(configMap);
        assertThat(deserialized.getIsSetup()).isFalse();
        assertThat(deserialized.getSetupUsername()).isEqualTo("admin");
    }
}
