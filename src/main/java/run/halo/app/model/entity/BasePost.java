package run.halo.app.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.GenericGenerator;
import run.halo.app.model.enums.PostEditorType;
import run.halo.app.model.enums.PostStatus;

import javax.persistence.*;
import java.util.Date;

/**
 * Post base entity.
 *
 * @author johnniang
 * @author ryanwang
 */
@Data
@Entity(name = "BasePost")
@Table(name = "posts",
    indexes = {@Index(name = "posts_type_status", columnList = "type, status"),
        @Index(name = "posts_create_time", columnList = "create_time")})
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.INTEGER, columnDefinition = "int default 0")
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class BasePost extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "custom-id")
    @GenericGenerator(name = "custom-id", strategy = "run.halo.app.model.entity.support.CustomIdGenerator")
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
    @Column(name = "original_content", nullable = false)
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
     * Likes
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

        if (originalContent == null) {
            originalContent = "";
        }

        if (formatContent == null) {
            formatContent = "";
        }

        if (editorType == null) {
            editorType = PostEditorType.MARKDOWN;
        }
    }

}
