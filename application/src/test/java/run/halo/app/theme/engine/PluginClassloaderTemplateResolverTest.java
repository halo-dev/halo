package run.halo.app.theme.engine;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.halo.app.plugin.HaloPluginManager;

/**
 * Tests for {@link PluginClassloaderTemplateResolver}.
 *
 * @author guqing
 * @since 2.11.0
 */
@ExtendWith(MockitoExtension.class)
class PluginClassloaderTemplateResolverTest {

    @Mock
    private HaloPluginManager haloPluginManager;

    @InjectMocks
    private PluginClassloaderTemplateResolver templateResolver;

    @Test
    void matchPluginTemplateWhenOwnerTemplateMatch() {
        var result =
            templateResolver.matchPluginTemplate("plugin:fake-plugin:doc", "modules/layout");
        assertThat(result.matches()).isTrue();
        assertThat(result.pluginName()).isEqualTo("fake-plugin");
        assertThat(result.templateName()).isEqualTo("modules/layout");
        assertThat(result.ownerTemplateName()).isEqualTo("doc");
    }

    @Test
    void matchPluginTemplateWhenDoesNotMatch() {
        var result =
            templateResolver.matchPluginTemplate("doc", "modules/layout");
        assertThat(result.matches()).isFalse();
    }

    @Test
    void matchPluginTemplateWhenTemplateMatch() {
        var result =
            templateResolver.matchPluginTemplate("doc", "plugin:fake-plugin:modules/layout");
        assertThat(result.matches()).isTrue();
        assertThat(result.pluginName()).isEqualTo("fake-plugin");
        assertThat(result.templateName()).isEqualTo("modules/layout");
        assertThat(result.ownerTemplateName()).isEqualTo("doc");
    }
}