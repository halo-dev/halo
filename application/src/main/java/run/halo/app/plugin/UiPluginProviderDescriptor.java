package run.halo.app.plugin;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.jspecify.annotations.Nullable;

/** Browser descriptor for the currently enabled plugin and theme UI providers. */
@Schema(description = "Browser descriptor for the currently enabled plugin and theme UI providers.")
public record UiPluginProviderDescriptor(
        @Schema(requiredMode = REQUIRED) List<Provider> providers,

        @Schema(
                description = "Legacy aggregate script URL, present when a legacy provider exists.",
                requiredMode = NOT_REQUIRED)
        @Nullable
        String legacyScript) {

    public UiPluginProviderDescriptor {
        providers = List.copyOf(providers);
    }

    @Schema(name = "UiPluginProvider", description = "One ordered UI provider classification.")
    public record Provider(
            @Schema(requiredMode = REQUIRED) String name,

            @Schema(
                    requiredMode = REQUIRED,
                    allowableValues = {"plugin", "theme"})
            String type,

            @Schema(requiredMode = REQUIRED) String version,

            @Schema(
                    description = "Provider classification and loading mode.",
                    requiredMode = REQUIRED,
                    allowableValues = {"legacy", "esm", "invalid"})
            String kind,

            @Schema(description = "ESM entry URL, required for ESM providers.", requiredMode = NOT_REQUIRED) @Nullable
            String entry,

            @Schema(description = "Optional startup stylesheet URL.", requiredMode = NOT_REQUIRED) @Nullable
            String style,

            @Schema(
                    description = "Discovery failure reason, required for invalid providers.",
                    requiredMode = NOT_REQUIRED)
            @Nullable
            String reason) {}
}
