package run.halo.app.theme.router;

import java.util.Map;
import org.springframework.stereotype.Component;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.theme.DefaultTemplateEnum;

/**
 * <p>The {@link PermalinkPatternProvider} used to obtain permalink rules according to specific
 * template names.</p>
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class PermalinkPatternProvider {

    private final ExtensionClient client;

    public PermalinkPatternProvider(ExtensionClient client) {
        this.client = client;
    }

    private SystemSetting.ThemeRouteRules getPermalinkRules() {
        return client.fetch(ConfigMap.class, SystemSetting.SYSTEM_CONFIG)
            .map(configMap -> {
                Map<String, String> data = configMap.getData();
                return data.get(SystemSetting.ThemeRouteRules.GROUP);
            })
            .map(routeRulesJson -> JsonUtils.jsonToObject(routeRulesJson,
                SystemSetting.ThemeRouteRules.class))
            .orElseGet(() -> {
                SystemSetting.ThemeRouteRules themeRouteRules = new SystemSetting.ThemeRouteRules();
                themeRouteRules.setArchives("archives");
                themeRouteRules.setPost("/archives/{slug}");
                themeRouteRules.setTags("tags");
                themeRouteRules.setCategories("categories");
                return themeRouteRules;
            });
    }

    /**
     * Get permalink pattern by template name.
     *
     * @param defaultTemplateEnum default templates
     * @return a pattern specified by the template name
     */
    public String getPattern(DefaultTemplateEnum defaultTemplateEnum) {
        SystemSetting.ThemeRouteRules permalinkRules = getPermalinkRules();
        return switch (defaultTemplateEnum) {
            case INDEX -> null;
            case POST -> permalinkRules.getPost();
            case ARCHIVES -> permalinkRules.getArchives();
            case CATEGORY, CATEGORIES -> permalinkRules.getCategories();
            case TAG, TAGS -> permalinkRules.getTags();
        };
    }
}
