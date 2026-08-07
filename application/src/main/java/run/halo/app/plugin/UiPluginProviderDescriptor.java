package run.halo.app.plugin;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** Browser descriptor for the currently enabled plugin and theme UI providers. */
@Schema(description = "Browser descriptor for the currently enabled plugin and theme UI providers.")
public record UiPluginProviderDescriptor(
        @Schema(requiredMode = REQUIRED) String version,
        String style,
        @Schema(requiredMode = REQUIRED) LegacyResources legacy,
        @Schema(requiredMode = REQUIRED) List<Registration> registrations,
        @Schema(requiredMode = REQUIRED) List<EsmProvider> providers,
        @Schema(requiredMode = REQUIRED) List<InvalidProvider> invalid) {

    public UiPluginProviderDescriptor {
        registrations = List.copyOf(registrations);
        providers = List.copyOf(providers);
        invalid = List.copyOf(invalid);
    }

    @Schema(name = "UiPluginLegacyResources")
    public record LegacyResources(
            @Schema(requiredMode = REQUIRED) String script) {}

    @Schema(name = "UiPluginProviderRegistration")
    public record Registration(
            @Schema(requiredMode = REQUIRED) String name,

            @Schema(
                    requiredMode = REQUIRED,
                    allowableValues = {"plugin", "theme"})
            String type,

            @Schema(requiredMode = REQUIRED) String version) {}

    @Schema(name = "UiPluginEsmProvider")
    public record EsmProvider(
            @Schema(requiredMode = REQUIRED) String name,

            @Schema(
                    requiredMode = REQUIRED,
                    allowableValues = {"plugin", "theme"})
            String type,

            @Schema(requiredMode = REQUIRED) String version,
            @Schema(requiredMode = REQUIRED) String entry) {}

    @Schema(name = "UiPluginInvalidProvider")
    public record InvalidProvider(
            @Schema(requiredMode = REQUIRED) String name,

            @Schema(
                    requiredMode = REQUIRED,
                    allowableValues = {"plugin", "theme"})
            String type,

            @Schema(requiredMode = REQUIRED) String version,
            @Schema(requiredMode = REQUIRED) String reason) {}
}
