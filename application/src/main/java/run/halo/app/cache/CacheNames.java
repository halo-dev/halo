package run.halo.app.cache;

import java.util.Set;
import run.halo.app.core.extension.service.DefaultRoleService;
import run.halo.app.extension.ReactiveExtensionClientImpl;
import run.halo.app.plugin.extensionpoint.DefaultExtensionGetter;

/**
 * Defines constant cache names used in the application.
 *
 * @author sergei
 */
public final class CacheNames {

    /**
     * Cache name for dependencies roles.
     *
     * @see DefaultRoleService#listDependenciesFlux(Set)
     * @see ReactiveExtensionClientImpl
     */
    public static final String ROLE_DEPENDENCIES = "ROLE_DEPENDENCIES";

    /**
     * Cache name for plugin extensions.
     *
     * @see DefaultExtensionGetter#getExtensions(Class)
     * @see ReactiveExtensionClientImpl
     */
    public static final String PLUGIN_EXTENSIONS = "PLUGIN_EXTENSIONS";

    /**
     * Cache name for plugin extensions.
     *
     * @see DefaultExtensionGetter#getEnabledExtensionByDefinition(Class)
     * @see ReactiveExtensionClientImpl
     */
    public static final String EXTENSION_POINT_DEFINITIONS = "EXTENSION_POINT_DEFINITIONS";

    private CacheNames() {
    }
}
