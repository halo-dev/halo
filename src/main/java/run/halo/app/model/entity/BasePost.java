package run.halo.app.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
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
@Table(name = "posts")
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.INTEGER, columnDefinition = "int default 0")
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class BasePost extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Post title.
     */
    @Column(name = "title", columnDefinition = "varchar(100) not null")
    private String title;

    /**
     * Post status.
     */
    @Column(name = "status", columnDefinition = "int default 1")
    private PostStatus status;

    /**
     * Post url.
     */
    @Deprecated
    @Column(name = "url", columnDefinition = "varchar(255) not null")
    private String url;

    /**
     * Post slug.
     */
    @Column(name = "slug", columnDefinition = "varchar(255)", unique = true)
    private String slug;

    /**
     * Post editor type.
     */
    @Column(name = "editor_type", columnDefinition = "int default 0")
    private PostEditorType editorType;

    /**
     * Original content,not format.
     */
    @Column(name = "original_content", columnDefinition = "longtext not null")
    private String originalContent;

    /**
     * Rendered content.
     *
     * @see run.halo.app.utils.MarkdownUtils#renderHtml(String)
     */
    @Column(name = "format_content", columnDefinition = "longtext not null")
    private String formatContent;

    /**
     * Post summary.
     */
    @Column(name = "summary", columnDefinition = "longtext default ''")
    private String summary;

    /**
     * Cover thumbnail of the post.
     */
    @Column(name = "thumbnail", columnDefinition = "varchar(1023) default ''")
    private String thumbnail;

    /**
     * Post visits.
     */
    @Column(name = "visits", columnDefinition = "bigint default 0")
    private Long visits;

    /**
     * Whether to allow comments.
     */
    @Column(name = "disallow_comment", columnDefinition = "int default 0")
    private Boolean disallowComment;

    /**
     * Post password.
     */
    @Column(name = "password", columnDefinition = "varchar(255) default ''")
    private String password;

    /**
     * Custom template.
     */
    @Column(name = "template", columnDefinition = "varchar(255) default ''")
    private String template;

    /**
     * Whether to top the post.
     */
    @Column(name = "top_priority", columnDefinition = "int default 0")
    private Integer topPriority;

    /**
     * Likes
     */
    @Column(name = "likes", columnDefinition = "bigint default 0")
    private Long likes;

    /**
     * Edit time.
     */
    @Column(name = "edit_time", columnDefinition = "timestamp default CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date editTime;

    /**
     * Meta keywords.
     */
    @Column(name = "meta_keywords", columnDefinition = "varchar(500) default ''")
    private String metaKeywords;

    /**
     * Meta description.
     */
    @Column(name = "meta_description", columnDefinition = "varchar(1023) default ''")
    private String metaDescription;

    @Override
    public void prePersist() {
        super.prePersist();

        id = null;

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
