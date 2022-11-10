package run.halo.app.theme.finders;

/**
 * A finder for {@link run.halo.app.core.extension.Plugin}.
 *
 * @author guqing
 * @since 2.0.0
 */
public interface PluginFinder {

    boolean enabled(String pluginName);

    boolean hasInstalled(String pluginName);
}
