package run.halo.app.theme.router;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Metadata;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.theme.DefaultTemplateEnum;

/**
 * Tests for {@link PermalinkPatternProvider}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class PermalinkPatternProviderTest {

    @Mock
    private ExtensionClient client;

    private PermalinkPatternProvider permalinkPatternProvider;

    @BeforeEach
    void setUp() {
        permalinkPatternProvider = new PermalinkPatternProvider(client);
    }

    @Test
    void getPatternThenDefault() {
        when(client.fetch(eq(ConfigMap.class), eq(SystemSetting.SYSTEM_CONFIG)))
            .thenReturn(Optional.empty());

        String pattern = permalinkPatternProvider.getPattern(DefaultTemplateEnum.POST);
        assertThat(pattern).isEqualTo("/archives/{slug}");

        pattern = permalinkPatternProvider.getPattern(DefaultTemplateEnum.TAG);
        assertThat(pattern).isEqualTo("tags");

        pattern = permalinkPatternProvider.getPattern(DefaultTemplateEnum.TAGS);
        assertThat(pattern).isEqualTo("tags");

        pattern = permalinkPatternProvider.getPattern(DefaultTemplateEnum.CATEGORY);
        assertThat(pattern).isEqualTo("categories");

        pattern = permalinkPatternProvider.getPattern(DefaultTemplateEnum.CATEGORIES);
        assertThat(pattern).isEqualTo("categories");

        pattern = permalinkPatternProvider.getPattern(DefaultTemplateEnum.ARCHIVES);
        assertThat(pattern).isEqualTo("archives");

        pattern = permalinkPatternProvider.getPattern(DefaultTemplateEnum.INDEX);
        assertThat(pattern).isNull();
    }

    @Test
    void getPattern() {
        ConfigMap configMap = new ConfigMap();
        Metadata metadata = new Metadata();
        metadata.setName("system");
        configMap.setMetadata(metadata);
        final SystemSetting.Theme theme = new SystemSetting.Theme();

        SystemSetting.ThemeRouteRules themeRouteRules = new SystemSetting.ThemeRouteRules();
        themeRouteRules.setPost("/posts/{slug}");
        themeRouteRules.setCategories("c");
        themeRouteRules.setTags("t");
        themeRouteRules.setArchives("a");

        theme.setRouteRules(themeRouteRules);
        configMap.setData(Map.of("theme", JsonUtils.objectToJson(theme)));

        when(client.fetch(eq(ConfigMap.class), eq(SystemSetting.SYSTEM_CONFIG)))
            .thenReturn(Optional.of(configMap));

        String pattern = permalinkPatternProvider.getPattern(DefaultTemplateEnum.POST);
        assertThat(pattern).isEqualTo(themeRouteRules.getPost());

        pattern = permalinkPatternProvider.getPattern(DefaultTemplateEnum.TAG);
        assertThat(pattern).isEqualTo(themeRouteRules.getTags());

        pattern = permalinkPatternProvider.getPattern(DefaultTemplateEnum.TAGS);
        assertThat(pattern).isEqualTo(themeRouteRules.getTags());

        pattern = permalinkPatternProvider.getPattern(DefaultTemplateEnum.CATEGORY);
        assertThat(pattern).isEqualTo(themeRouteRules.getCategories());

        pattern = permalinkPatternProvider.getPattern(DefaultTemplateEnum.CATEGORIES);
        assertThat(pattern).isEqualTo(themeRouteRules.getCategories());

        pattern = permalinkPatternProvider.getPattern(DefaultTemplateEnum.ARCHIVES);
        assertThat(pattern).isEqualTo(themeRouteRules.getArchives());

        pattern = permalinkPatternProvider.getPattern(DefaultTemplateEnum.INDEX);
        assertThat(pattern).isNull();
    }
}