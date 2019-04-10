package run.halo.app.handler.theme;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import run.halo.app.handler.theme.impl.YamlThemeConfigResolverImpl;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Theme configuration resolver manager.
 *
 * @author johnniang
 * @date 4/10/19
 */
@Component
public class ThemeConfigResolvers {

    private final Map<ConfigType, ThemeConfigResolver> resolverMap = new ConcurrentHashMap<>(2);

    public ThemeConfigResolvers() {
        resolverMap.put(ConfigType.YAML, new YamlThemeConfigResolverImpl());
        // TODO Add another theme config resolver
    }

    /**
     * Config type enum.
     */
    public enum ConfigType {

        YAML,

        PROPERTY
    }

    /**
     * Resolves the content.
     *
     * @param content content must not be blank
     * @param type    config type
     * @return a list of group
     * @throws IOException throws when content conversion fails
     */
    public List<Group> resolve(@NonNull String content, @Nullable ConfigType type) throws IOException {
        ThemeConfigResolver resolver = getResolver(type);

        if (resolver == null) {
            throw new UnsupportedOperationException("Unsupported theme config type: " + type);
        }

        return resolver.resolve(content);
    }

    /**
     * Resolves the content.
     *
     * @param content content must not be blank
     * @return a list of group
     * @throws IOException throws when content conversion fails
     */
    public List<Group> resolve(String content) throws IOException {
        return resolve(content, ConfigType.YAML);
    }

    private ThemeConfigResolver getResolver(@Nullable ConfigType type) {
        return type == null ? null : resolverMap.get(type);
    }
}
