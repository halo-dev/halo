package run.halo.app.core.extension;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/**
 * @author guqing
 * @since 2.0.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@GVK(group = "theme.halo.run", version = "v1alpha1", kind = Theme.KIND,
    plural = "themes", singular = "theme")
public class Theme extends AbstractExtension {

    public static final String KIND = "Theme";

    @Schema(required = true)
    private ThemeSpec spec;

    private ThemeStatus status;

    @Data
    @ToString
    public static class ThemeSpec {
        private static final String WILDCARD = "*";

        @Schema(required = true, minLength = 1)
        private String displayName;

        @Schema(required = true)
        private Author author;

        private String description;

        private String logo;

        private String website;

        private String repo;

        private String version;

        private String require;

        private String settingName;

        private String configMapName;

        @NonNull
        public String getVersion() {
            if (StringUtils.isBlank(this.version)) {
                return WILDCARD;
            }
            return version;
        }

        @NonNull
        public String getRequire() {
            if (StringUtils.isBlank(this.require)) {
                return WILDCARD;
            }
            return require;
        }
    }

    @Data
    public static class ThemeStatus {
        private String location;
    }

    @Data
    @ToString
    public static class Author {

        @Schema(required = true, minLength = 1)
        private String name;

        private String website;
    }
}
