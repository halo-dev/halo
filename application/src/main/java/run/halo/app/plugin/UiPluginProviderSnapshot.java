package run.halo.app.plugin;

import java.util.List;

/** Immutable browser descriptor for one generation of plugin and theme UI providers. */
public record UiPluginProviderSnapshot(
        String generation,
        LegacyResources legacy,
        List<Registration> registrations,
        List<EsmProvider> providers,
        List<InvalidProvider> invalid) {

    public UiPluginProviderSnapshot {
        registrations = List.copyOf(registrations);
        providers = List.copyOf(providers);
        invalid = List.copyOf(invalid);
    }

    public record LegacyResources(String script, String style) {}

    public record Registration(String name, String type, String version) {}

    public record EsmProvider(String name, String type, String version, String entry, List<String> styles) {
        public EsmProvider {
            styles = List.copyOf(styles);
        }
    }

    public record InvalidProvider(String name, String type, String version, String reason) {}
}
