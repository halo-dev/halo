package run.halo.app.core.extension;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Objects;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.util.Assert;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.infra.ConditionList;
import run.halo.app.infra.model.License;

/**
 * Theme extension that describes an installable frontend theme and its runtime state.
 *
 * @author guqing
 * @since 2.0.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@GVK(group = "theme.halo.run", version = "v1alpha1", kind = Theme.KIND, plural = "themes", singular = "theme")
public class Theme extends AbstractExtension {

    public static final String KIND = "Theme";

    public static final String THEME_NAME_LABEL = "theme.halo.run/theme-name";

    public static final String REQUEST_RELOAD_ANNOTATION = "theme.halo.run/request-reload";

    /** Desired theme metadata, settings references, compatibility, and template declarations. */
    @Schema(requiredMode = REQUIRED)
    private ThemeSpec spec;

    /** Observed theme lifecycle state reported by the theme manager. */
    private ThemeStatus status;

    /** Desired theme metadata and configuration references. */
    @Data
    @ToString
    public static class ThemeSpec {
        private static final String WILDCARD = "*";

        /** Display name shown for the theme. */
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String displayName;

        /** Theme author information. */
        @Schema(requiredMode = REQUIRED)
        private Author author;

        /** Human-readable theme description. */
        private String description;

        /** Logo URL or attachment URI for the theme. */
        private String logo;

        /** Theme homepage URL. */
        private String homepage;

        /** Source repository URL. */
        private String repo;

        /** Issue tracker URL. */
        private String issues;

        /** Theme version. The wildcard value means any version. */
        private String version = WILDCARD;

        /** Required Halo version range. The wildcard value means any Halo version. */
        @Schema(requiredMode = NOT_REQUIRED)
        private String requires = WILDCARD;

        /** Setting metadata.name used to render the theme configuration form. */
        private String settingName;

        /** ConfigMap metadata.name storing the theme configuration values. */
        private String configMapName;

        /** Licenses declared by the theme. */
        private List<License> license;

        /** Custom template descriptors exposed by the theme. */
        @Schema
        private CustomTemplates customTemplates;
    }

    /** Observed theme lifecycle and installation state. */
    @Data
    public static class ThemeStatus {
        /** Current theme lifecycle phase. */
        private ThemePhase phase;

        /** Reconciliation conditions for the theme. */
        private ConditionList conditions;

        /** Local filesystem location where the theme is loaded from. */
        private String location;

        /** Whether the theme appears to be a local development workspace. */
        private Boolean inDevelopment;

        /** Resolved preview screenshot URL served from the theme root. */
        private String screenshot;

        /** Resolved Console/User Center UI JavaScript entry URL served from the theme root. */
        private String entry;

        /** Resolved Console/User Center UI stylesheet URL served from the theme root. */
        private String stylesheet;

        /** Observed page layout contract compatibility. */
        private PageLayout pageLayout;

        /** Page layout contract compatibility details. */
        @Data
        public static class PageLayout {
            /** Compatibility state of the page layout contract. */
            private PageLayoutState state;

            /** Contract template path relative to the theme root. */
            private String template;

            /** Stable diagnostic reason. */
            private String reason;

            /** Human-readable diagnostic message. */
            private String message;
        }
    }

    /**
     * Null-safe get {@link ConditionList} from theme status.
     *
     * @param theme theme must not be null
     * @return condition list
     */
    public static ConditionList nullSafeConditionList(Theme theme) {
        Assert.notNull(theme, "The theme must not be null");
        var status = Objects.requireNonNullElseGet(theme.getStatus(), ThemeStatus::new);
        theme.setStatus(status);

        var conditions = Objects.requireNonNullElseGet(status.getConditions(), ConditionList::new);
        status.setConditions(conditions);
        return conditions;
    }

    public enum ThemePhase {
        READY,
        FAILED,
        UNKNOWN,
    }

    public enum PageLayoutState {
        SUPPORTED,
        MISSING,
        INVALID,
    }

    /** Theme author metadata. */
    @Data
    @ToString
    public static class Author {

        /** Author display name. */
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String name;

        /** Author website URL. */
        private String website;
    }

    /** Custom template descriptors grouped by the content type they render. */
    @Data
    public static class CustomTemplates {
        /** Custom templates available for posts. */
        private List<TemplateDescriptor> post;

        /** Custom templates available for categories. */
        private List<TemplateDescriptor> category;

        /** Custom templates available for single pages. */
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

        /** Template display name shown to users. */
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String name;

        /** Human-readable description of when to use this template. */
        private String description;

        /** Screenshot URL or attachment URI for previewing the template. */
        private String screenshot;

        /** Template file path relative to the theme templates directory. */
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String file;
    }
}
