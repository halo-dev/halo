package run.halo.app.cache;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import run.halo.app.extension.ReactiveExtensionClientImpl;
import run.halo.app.infra.properties.HaloProperties;

/**
 * Provides methods to determine if specific types of caches are evictable or enabled.
 *
 * <p>Uses {@link HaloProperties} to check the configuration for cache settings.
 *
 * @author sergei
 * @see HaloProperties#getCaches()
 * @see Cacheable#condition()
 * @see CacheEvict#condition()
 * @see ReactiveExtensionClientImpl
 */
@Component
@RequiredArgsConstructor
public class CacheConditionProvider {
    private static final String ROLE_KIND = "Role";
    private static final String PLUGIN_KIND = "Plugin";
    private static final String EXTENSION_POINT_DEFINITION_KIND = "ExtensionPointDefinition";

    private final HaloProperties properties;

    /**
     * Determines if the role dependencies cache is evictable based on the provided kind.
     *
     * @param kind the kind to check against.
     * @return {@code true} if the role dependencies cache is evictable, {@code false} otherwise.
     */
    public boolean isRoleDependenciesCacheEvictableByKind(String kind) {
        return isRoleCacheEnabled() && Objects.equals(kind, ROLE_KIND);
    }

    /**
     * Determines if the plugin extension cache is evictable based on the provided kind.
     *
     * @param kind the kind to check against.
     * @return {@code true} if the plugin extension cache is evictable, {@code false} otherwise.
     */
    public boolean isPluginExtensionCacheEvictableByKind(String kind) {
        return isPluginExtensionCacheEnabled() && Objects.equals(kind, PLUGIN_KIND);
    }

    /**
     * Determines if the extension point definition cache is evictable based on the provided kind.
     *
     * @param kind the kind to check against.
     * @return {@code true} if the extension point definition cache is evictable, {@code false}
     * otherwise.
     */
    public boolean isExtensionPointDefinitionCacheEvictableByKind(String kind) {
        return isExtensionPointDefinitionCacheEnabled() && Objects.equals(kind,
            EXTENSION_POINT_DEFINITION_KIND);
    }

    /**
     * Checks if the role dependencies cache is enabled.
     *
     * @return {@code true} if the role dependencies cache is enabled, {@code false} otherwise
     */
    public boolean isRoleCacheEnabled() {
        return isCacheEnabled("role-dependencies");
    }

    /**
     * Checks if the plugin extension cache is enabled.
     *
     * @return {@code true} if the plugin extension cache is enabled, {@code false} otherwise.
     */
    public boolean isPluginExtensionCacheEnabled() {
        return isCacheEnabled("plugin-extension");
    }

    /**
     * Checks if the extension point definition cache is enabled.
     *
     * @return {@code true} if the extension point definition cache is enabled, {@code false}
     * otherwise.
     */
    public boolean isExtensionPointDefinitionCacheEnabled() {
        return isCacheEnabled("extension-point-definition");
    }

    private boolean isCacheEnabled(String cache) {
        var cacheProperties = properties.getCaches().get(cache);

        if (cacheProperties != null) {
            return !cacheProperties.isDisabled();
        }

        return false;
    }
}
