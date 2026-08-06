package run.halo.app.plugin;

import java.util.List;

/** Browser descriptor for the currently enabled plugin and theme UI providers. */
public record UiPluginProviderDescriptor(
        String version,
        String style,
        LegacyResources legacy,
        List<Registration> registrations,
        List<EsmProvider> providers,
        List<InvalidProvider> invalid) {

    public UiPluginProviderDescriptor {
        registrations = List.copyOf(registrations);
        providers = List.copyOf(providers);
        invalid = List.copyOf(invalid);
    }

    public record LegacyResources(String script) {}

    public record Registration(String name, String type, String version) {}

    public record EsmProvider(String name, String type, String version, String entry) {}

    public record InvalidProvider(String name, String type, String version, String reason) {}
}
