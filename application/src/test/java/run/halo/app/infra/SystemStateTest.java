package run.halo.app.infra;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import run.halo.app.extension.ConfigMap;

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
        assertThat(configMap.getData().get(SystemState.GROUP))
            .isEqualTo("{\"isSetup\":false,\"foo\":\"bar\"}");
    }
}
