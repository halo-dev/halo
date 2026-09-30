package run.halo.app.plugin;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.Nullable;

/** UI resources included in an installed provider, independent of browser registration. */
public record UiPluginResources(
        @Schema(
                requiredMode = REQUIRED,
                allowableValues = {"none", "legacy", "esm", "invalid"})
        String kind,

        @Nullable String entry,
        @Nullable String style,
        @Nullable String reason) {}
