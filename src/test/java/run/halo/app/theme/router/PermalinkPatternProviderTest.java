package run.halo.app.theme.router;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.Metadata;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
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
    private SystemConfigurableEnvironmentFetcher environmentFetcher;

    @InjectMocks
    private PermalinkPatternProvider permalinkPatternProvider;

    @Test
    void getPatternThenDefault() {
        when(environmentFetcher.getConfigMapBlocking())
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

        SystemSetting.ThemeRouteRules themeRouteRules = new SystemSetting.ThemeRouteRules();
        themeRouteRules.setPost("/posts/{slug}");
        themeRouteRules.setCategories("c");
        themeRouteRules.setTags("t");
        themeRouteRules.setArchives("a");

        configMap.setData(Map.of("routeRules", JsonUtils.objectToJson(themeRouteRules)));

        when(environmentFetcher.getConfigMapBlocking())
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