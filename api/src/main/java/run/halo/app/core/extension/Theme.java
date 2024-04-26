package run.halo.app.core.extension;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.infra.ConditionList;
import run.halo.app.infra.model.License;

/**
 * <p>Theme extension.</p>
 *
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

    public static final String THEME_NAME_LABEL = "theme.halo.run/theme-name";

    @Schema(requiredMode = REQUIRED)
    private ThemeSpec spec;

    private ThemeStatus status;

    @Data
    @ToString
    public static class ThemeSpec {
        private static final String WILDCARD = "*";

        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String displayName;

        @Schema(requiredMode = REQUIRED)
        private Author author;

        private String description;

        private String logo;

        @Deprecated(forRemoval = true, since = "2.7.0")
        private String website;

        private String homepage;

        private String repo;

        private String issues;

        private String version;

        @Deprecated(forRemoval = true, since = "2.2.0")
        @Schema(description = "Deprecated, use `requires` instead.")
        private String require;

        @Schema(requiredMode = NOT_REQUIRED)
        private String requires;

        private String settingName;

        private String configMapName;

        private List<License> license;

        @Schema
        private CustomTemplates customTemplates;

        @NonNull
        public String getVersion() {
            return StringUtils.defaultString(this.version, WILDCARD);
        }

        /**
         * if requires is not empty, then return requires, else return require or {@code WILDCARD}.
         *
         * @return requires to satisfies system version
         */
        @NonNull
        public String getRequires() {
            if (StringUtils.isNotBlank(this.requires)) {
                return this.requires;
            }
            return StringUtils.defaultString(this.require, WILDCARD);
        }

        /**
         * Compatible with {@link #website} property.
         */
        public String getHomepage() {
            return StringUtils.defaultString(this.homepage, this.website);
        }
    }

    @Data
    public static class ThemeStatus {
        private ThemePhase phase;
        private ConditionList conditions;
        private String location;
    }

    /**
     * Null-safe get {@link ConditionList} from theme status.
     *
     * @param theme theme must not be null
     * @return condition list
     */
    public static ConditionList nullSafeConditionList(Theme theme) {
        Assert.notNull(theme, "The theme must not be null");
        ThemeStatus status = ObjectUtils.defaultIfNull(theme.getStatus(), new ThemeStatus());
        theme.setStatus(status);

        ConditionList conditions =
            ObjectUtils.defaultIfNull(status.getConditions(), new ConditionList());
        status.setConditions(conditions);
        return conditions;
    }

    public enum ThemePhase {
        READY,
        FAILED,
        UNKNOWN,
    }

    @Data
    @ToString
    public static class Author {

        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String name;

        private String website;
    }

    @Data
    public static class CustomTemplates {
        private List<TemplateDescriptor> post;
        private List<TemplateDescriptor> category;
        private List<TemplateDescriptor> page;
    }

    /**
     * Type used to describe custom template page.
     *
     * @author guqing
     * @since 2.0.0
     */
    @Data
    public static class TemplateDescriptor {

        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String name;

        private String description;

        private String screenshot;

        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String file;
    }

}
