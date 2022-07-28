package run.halo.app.core.extension;

import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/**
 * @author guqing
 * @since 2.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@GVK(group = "theme.halo.run", version = "v1alpha1", kind = "Theme",
    plural = "themes", singular = "theme")
public class Theme extends AbstractExtension {

    private ThemeSpec spec;

    @Data
    public static class ThemeSpec {
        private String displayName;
        private Author author;
        private String description;
        private String logo;
        private String website;
        private String repo;
        private String version = "*";
        private String require = "*";
    }

    @Data
    public static class Author {
        private String name;
        private String website;
    }
}
