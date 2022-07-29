package run.halo.app.core.extension;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/**
 * @author guqing
 * @since 2.0.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@GVK(group = "theme.halo.run", version = "v1alpha1", kind = "Theme",
    plural = "themes", singular = "theme")
public class Theme extends AbstractExtension {

    @Schema(required = true)
    private ThemeSpec spec;

    @Data
    @ToString
    public static class ThemeSpec {

        @Schema(required = true, minLength = 1)
        private String displayName;

        @Schema(required = true)
        private Author author;

        private String description;

        private String logo;

        private String website;

        private String repo;

        private String version = "*";

        private String require = "*";
    }

    @Data
    @ToString
    public static class Author {

        @Schema(required = true, minLength = 1)
        private String name;

        private String website;
    }
}
