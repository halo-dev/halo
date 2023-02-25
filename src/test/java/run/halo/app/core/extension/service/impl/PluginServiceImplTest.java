package run.halo.app.core.extension.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pf4j.PluginState;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class PluginServiceImplTest {

    @InjectMocks
    PluginServiceImpl pluginService;

    @Test
    void getPresetsTest() {
        var presets = pluginService.getPresets();
        StepVerifier.create(presets)
            .assertNext(plugin -> {
                assertEquals("fake-plugin", plugin.getMetadata().getName());
                assertEquals("0.0.2", plugin.getSpec().getVersion());
                assertEquals(PluginState.RESOLVED, plugin.getStatus().getPhase());
            })
            .verifyComplete();

    }
}