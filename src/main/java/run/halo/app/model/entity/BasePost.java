package run.halo.app.model.entity;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import run.halo.app.model.entity.Content.PatchedContent;
import run.halo.app.model.enums.PostEditorType;
import run.halo.app.model.enums.PostStatus;

/**
 * Post base entity.
 *
 * @author johnniang
 * @author ryanwang
 * @author coor.top
 */
@Data
@Entity(name = "BasePost")
@Table(name = "posts", indexes = {
    @Index(name = "posts_type_status", columnList = "type, status"),
    @Index(name = "posts_create_time", columnList = "create_time")})
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.INTEGER,
    columnDefinition = "int default 0")
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class BasePost extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "custom-id")
    @GenericGenerator(name = "custom-id", strategy = "run.halo.app.model.entity.support"
        + ".CustomIdGenerator")
    private Integer id;

    /**
     * Post title.
     */
    @Column(name = "title", nullable = false)
    private String title;

    /**
     * Post status.
     */
    @Column(name = "status")
    @ColumnDefault("1")
    private PostStatus status;

    /**
     * Post url.
     */
    @Deprecated
    @Column(name = "url")
    private String url;

    /**
     * Post slug.
     */
    @Column(name = "slug", unique = true)
    private String slug;

    /**
     * Post editor type.
     */
    @Column(name = "editor_type")
    @ColumnDefault("0")
    private PostEditorType editorType;

    /**
     * Original content,not format.
     */
    @Column(name = "original_content")
    @Lob
    private String originalContent;

    /**
     * Rendered content.
     */
    @Column(name = "format_content")
    @Lob
    private String formatContent;

    /**
     * Post summary.
     */
    @Column(name = "summary")
    @Lob
    private String summary;

    /**
     * Cover thumbnail of the post.
     */
    @Column(name = "thumbnail", length = 1023)
    private String thumbnail;

    /**
     * Post visits.
     */
    @Column(name = "visits")
    @ColumnDefault("0")
    private Long visits;

    /**
     * Whether to allow comments.
     */
    @Column(name = "disallow_comment")
    @ColumnDefault("0")
    private Boolean disallowComment;

    /**
     * Post password.
     */
    @Column(name = "password")
    private String password;

    /**
     * Custom template.
     */
    @Column(name = "template")
    private String template;

    /**
     * Whether to top the post.
     */
    @Column(name = "top_priority")
    @ColumnDefault("0")
    private Integer topPriority;

    /**
     * Likes.
     */
    @Column(name = "likes")
    @ColumnDefault("0")
    private Long likes;

    /**
     * Edit time.
     */
    @Column(name = "edit_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date editTime;

    /**
     * Meta keywords.
     */
    @Column(name = "meta_keywords", length = 511)
    private String metaKeywords;

    /**
     * Meta description.
     */
    @Column(name = "meta_description", length = 1023)
    private String metaDescription;

    /**
     * Content word count.
     */
    @Column(name = "word_count")
    @ColumnDefault("0")
    private Long wordCount;

    /**
     * Post content version.
     */
    @ColumnDefault("1")
    private Integer version;

    /**
     * This extra field don't correspond to any columns in the <code>Post</code> table because we
     * don't want to save this value.
     */
    @Transient
    private PatchedContent content;


    @Override
    public void prePersist() {
        super.prePersist();

        if (editTime == null) {
            editTime = getCreateTime();
        }

        if (status == null) {
            status = PostStatus.DRAFT;
        }

        if (summary == null) {
            summary = "";
        }

        if (thumbnail == null) {
            thumbnail = "";
        }

        if (disallowComment == null) {
            disallowComment = false;
        }

        if (password == null) {
            password = "";
        }

        if (template == null) {
            template = "";
        }

        if (topPriority == null) {
            topPriority = 0;
        }

        if (visits == null || visits < 0) {
            visits = 0L;
        }

        if (likes == null || likes < 0) {
            likes = 0L;
        }

        if (editorType == null) {
            editorType = PostEditorType.MARKDOWN;
        }

        if (wordCount == null || wordCount < 0) {
            wordCount = 0L;
        }

        if (version == null || version < 0) {
            version = 1;
        }

        // Clear the value of the deprecated attributes
        this.originalContent = "";
        this.formatContent = "";
    }

    @Override
    protected void preUpdate() {
        super.preUpdate();
        // Clear the value of the deprecated attributes
        this.originalContent = "";
        this.formatContent = "";
    }

    /**
     * Gets post content.
     *
     * @return a {@link PatchedContent} if present,otherwise an empty object
     */
    @NonNull
    public PatchedContent getContent() {
        if (this.content == null) {
            PatchedContent patchedContent = new PatchedContent();
            patchedContent.setOriginalContent("");
            patchedContent.setContent("");
            return patchedContent;
        }
        return content;
    }

    @Nullable
    public PatchedContent getContentOfNullable() {
        return this.content;
    }
}
