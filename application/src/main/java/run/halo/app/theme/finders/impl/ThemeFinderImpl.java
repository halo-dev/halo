package run.halo.app.theme.finders.impl;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Theme;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.theme.finders.Finder;
import run.halo.app.theme.finders.ThemeFinder;
import run.halo.app.theme.finders.vo.ThemeVo;

/**
 * A default implementation for {@link ThemeFinder}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Finder("themeFinder")
public class ThemeFinderImpl implements ThemeFinder {

    private final ReactiveExtensionClient client;
    private final SystemConfigurableEnvironmentFetcher environmentFetcher;

    public ThemeFinderImpl(ReactiveExtensionClient client,
        SystemConfigurableEnvironmentFetcher environmentFetcher) {
        this.client = client;
        this.environmentFetcher = environmentFetcher;
    }

    @Override
    public Mono<ThemeVo> activation() {
        return environmentFetcher.fetch(SystemSetting.Theme.GROUP, SystemSetting.Theme.class)
            .map(SystemSetting.Theme::getActive)
            .flatMap(themeName -> client.fetch(Theme.class, themeName))
            .flatMap(theme -> themeWithConfig(ThemeVo.from(theme)));
    }

    @Override
    public Mono<ThemeVo> getByName(String themeName) {
        return client.fetch(Theme.class, themeName)
            .flatMap(theme -> themeWithConfig(ThemeVo.from(theme)));
    }

    private Mono<ThemeVo> themeWithConfig(ThemeVo themeVo) {
        if (StringUtils.isBlank(themeVo.getSpec().getConfigMapName())) {
            return Mono.just(themeVo);
        }
        return client.fetch(ConfigMap.class, themeVo.getSpec().getConfigMapName())
            .map(configMap -> {
                Map<String, JsonNode> config = new HashMap<>();
                configMap.getData().forEach((k, v) -> {
                    JsonNode jsonNode = JsonUtils.jsonToObject(v, JsonNode.class);
                    config.put(k, jsonNode);
                });
                JsonNode configJson = JsonUtils.mapToObject(config, JsonNode.class);
                return themeVo.withConfig(configJson);
            })
            .defaultIfEmpty(themeVo);
    }
}
