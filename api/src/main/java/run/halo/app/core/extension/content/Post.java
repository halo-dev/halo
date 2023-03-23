package run.halo.app.core.extension.content;

import static java.lang.Boolean.parseBoolean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.MetadataOperator;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.infra.ConditionList;

/**
 * <p>Post extension.</p>
 *
 * @author guqing
 * @see <a href="https://github.com/halo-dev/halo/issues/2322">issue#2322</a>
 * @since 2.0.0
 */
@Data
@ToString(callSuper = true)
@GVK(group = Constant.GROUP, version = Constant.VERSION, kind = Post.KIND,
    plural = "posts", singular = "post")
@EqualsAndHashCode(callSuper = true)
public class Post extends AbstractExtension {

    public static final String KIND = "Post";

    public static final GroupVersionKind GVK = GroupVersionKind.fromExtension(Post.class);

    public static final String CATEGORIES_ANNO = "content.halo.run/categories";
    public static final String LAST_RELEASED_SNAPSHOT_ANNO =
        "content.halo.run/last-released-snapshot";
    public static final String TAGS_ANNO = "content.halo.run/tags";
    public static final String DELETED_LABEL = "content.halo.run/deleted";
    public static final String PUBLISHED_LABEL = "content.halo.run/published";
    public static final String OWNER_LABEL = "content.halo.run/owner";
    public static final String VISIBLE_LABEL = "content.halo.run/visible";

    public static final String ARCHIVE_YEAR_LABEL = "content.halo.run/archive-year";

    public static final String ARCHIVE_MONTH_LABEL = "content.halo.run/archive-month";
    public static final String ARCHIVE_DAY_LABEL = "content.halo.run/archive-day";

    @Schema(requiredMode = RequiredMode.REQUIRED)
    private PostSpec spec;

    @Schema
    private PostStatus status;

    @JsonIgnore
    public PostStatus getStatusOrDefault() {
        if (this.status == null) {
            this.status = new PostStatus();
        }
        return status;
    }

    @JsonIgnore
    public boolean isDeleted() {
        return Objects.equals(true, spec.getDeleted())
            || getMetadata().getDeletionTimestamp() != null;
    }

    @JsonIgnore
    public boolean isPublished() {
        return isPublished(this.getMetadata());
    }

    public static boolean isPublished(MetadataOperator metadata) {
        var labels = metadata.getLabels();
        return labels != null && parseBoolean(labels.getOrDefault(PUBLISHED_LABEL, "false"));
    }

    @Data
    public static class PostSpec {
        @Schema(requiredMode = RequiredMode.REQUIRED, minLength = 1)
        private String title;

        @Schema(requiredMode = RequiredMode.REQUIRED, minLength = 1)
        private String slug;

        /**
         * 文章引用到的已发布的内容，用于主题端显示.
         */
        private String releaseSnapshot;

        private String headSnapshot;

        private String baseSnapshot;

        private String owner;

        private String template;

        private String cover;

        @Schema(requiredMode = RequiredMode.REQUIRED, defaultValue = "false")
        private Boolean deleted;

        @Schema(requiredMode = RequiredMode.REQUIRED, defaultValue = "false")
        private Boolean publish;

        private Instant publishTime;

        @Schema(requiredMode = RequiredMode.REQUIRED, defaultValue = "false")
        private Boolean pinned;

        @Schema(requiredMode = RequiredMode.REQUIRED, defaultValue = "true")
        private Boolean allowComment;

        @Schema(requiredMode = RequiredMode.REQUIRED, defaultValue = "PUBLIC")
        private VisibleEnum visible;

        @Schema(requiredMode = RequiredMode.REQUIRED, defaultValue = "0")
        private Integer priority;

        @Schema(requiredMode = RequiredMode.REQUIRED)
        private Excerpt excerpt;

        private List<String> categories;

        private List<String> tags;

        private List<Map<String, String>> htmlMetas;
    }

    @Data
    public static class PostStatus {
        @Schema(requiredMode = RequiredMode.REQUIRED)
        private String phase;

        @Schema
        private ConditionList conditions;

        private String permalink;

        private String excerpt;

        private Boolean inProgress;

        private Integer commentsCount;

        private List<String> contributors;

        private Instant lastModifyTime;

        @JsonIgnore
        public ConditionList getConditionsOrDefault() {
            if (this.conditions == null) {
                this.conditions = new ConditionList();
            }
            return conditions;
        }
    }

    @Data
    public static class Excerpt {

        @Schema(requiredMode = RequiredMode.REQUIRED, defaultValue = "true")
        private Boolean autoGenerate;

        private String raw;
    }

    public enum PostPhase {
        DRAFT,
        PENDING_APPROVAL,
        PUBLISHED,
        FAILED;

        /**
         * Convert string value to {@link PostPhase}.
         *
         * @param value enum value string
         * @return {@link PostPhase} if found, otherwise null
         */
        public static PostPhase from(String value) {
            for (PostPhase phase : PostPhase.values()) {
                if (phase.name().equalsIgnoreCase(value)) {
                    return phase;
                }
            }
            return null;
        }
    }

    public enum VisibleEnum {
        PUBLIC,
        INTERNAL,
        PRIVATE;

        /**
         * Convert value string to {@link VisibleEnum}.
         *
         * @param value enum value string
         * @return {@link VisibleEnum} if found, otherwise null
         */
        public static VisibleEnum from(String value) {
            for (VisibleEnum visible : VisibleEnum.values()) {
                if (visible.name().equalsIgnoreCase(value)) {
                    return visible;
                }
            }
            return null;
        }
    }

    @Data
    public static class CompactPost {
        private String name;

        private VisibleEnum visible;

        private Boolean published;

        public static Builder builder() {
            return new Builder();
        }

        /**
         * <p>Compact post builder.</p>
         * <p>Can not replace with lombok builder.</p>
         * <p>The class used by subclasses of {@link AbstractExtension} must have a no-args
         * constructor.</p>
         */
        public static class Builder {
            private String name;

            private VisibleEnum visible;

            private Boolean published;

            public Builder name(String name) {
                this.name = name;
                return this;
            }

            public Builder visible(VisibleEnum visible) {
                this.visible = visible;
                return this;
            }

            public Builder published(Boolean published) {
                this.published = published;
                return this;
            }

            public CompactPost build() {
                CompactPost compactPost = new CompactPost();
                compactPost.setName(name);
                compactPost.setVisible(visible);
                compactPost.setPublished(published);
                return compactPost;
            }
        }
    }

    public static void changePublishedState(Post post, boolean value) {
        Map<String, String> labels = MetadataUtil.nullSafeLabels(post);
        labels.put(PUBLISHED_LABEL, String.valueOf(value));
    }
}
