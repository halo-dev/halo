package run.halo.app.theme.finders.impl;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
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
    public ThemeVo activation() {
        return environmentFetcher.fetch(SystemSetting.Theme.GROUP, SystemSetting.Theme.class)
            .map(SystemSetting.Theme::getActive)
            .flatMap(themeName -> client.fetch(Theme.class, themeName))
            .flatMap(theme -> themeWithConfig(ThemeVo.from(theme)))
            .block();
    }

    @Override
    public ThemeVo getByName(String themeName) {
        return client.fetch(Theme.class, themeName)
            .flatMap(theme -> themeWithConfig(ThemeVo.from(theme)))
            .block();
    }

    private Mono<ThemeVo> themeWithConfig(ThemeVo themeVo) {
        if (StringUtils.isBlank(themeVo.getSpec().getConfigMapName())) {
            return Mono.just(themeVo);
        }
        return client.fetch(ConfigMap.class, themeVo.getSpec().getConfigMapName())
            .map(configMap -> {
                Map<String, Object> config = new HashMap<>();
                configMap.getData().forEach((k, v) -> {
                    JsonNode jsonNode = JsonUtils.jsonToObject(v, JsonNode.class);
                    config.put(k, convert(jsonNode));
                });
                return themeVo.withConfig(config);
            })
            .switchIfEmpty(Mono.just(themeVo));
    }

    private Object convert(JsonNode jsonNode) {
        if (jsonNode.isObject()) {
            return JsonUtils.DEFAULT_JSON_MAPPER.convertValue(jsonNode, Map.class);
        }
        if (jsonNode.isArray()) {
            return JsonUtils.DEFAULT_JSON_MAPPER.convertValue(jsonNode, List.class);
        }
        return jsonNode.asText();
    }
}
