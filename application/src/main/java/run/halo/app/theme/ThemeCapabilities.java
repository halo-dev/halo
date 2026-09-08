package run.halo.app.theme;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.jspecify.annotations.Nullable;
import run.halo.app.core.extension.Theme.ThemeStatus.PageLayout;
import run.halo.app.plugin.UiPluginResources;

/** File capabilities of an installed theme; these do not imply successful rendering or UI registration. */
public record ThemeCapabilities(
        @Schema(requiredMode = REQUIRED) List<ThemeTemplateFile> templates,

        @Schema(requiredMode = REQUIRED, description = "Whether the entire template directory was inspected.")
        boolean complete,

        @Schema(requiredMode = REQUIRED) PageLayout pageLayout,
        @Schema(requiredMode = REQUIRED) UiPluginResources ui) {

    public record ThemeTemplateFile(
            @Schema(requiredMode = REQUIRED, description = "Path relative to the templates directory.")
            String path,

            @Schema(
                    requiredMode = REQUIRED,
                    allowableValues = {"available", "missing", "unreadable", "invalid"})
            String state,

            @Schema(requiredMode = REQUIRED) List<ThemeTemplateUsage> usages) {}

    public record ThemeTemplateUsage(
            @Schema(
                    requiredMode = REQUIRED,
                    allowableValues = {"system", "custom", "layout", "other"})
            String type,

            @Nullable String contentType,
            @Nullable String name,
            @Nullable String description) {}
}
