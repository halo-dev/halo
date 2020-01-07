package run.halo.app.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.model.enums.CommentStatus;
import run.halo.app.utils.ServiceUtils;

import javax.persistence.*;

/**
 * Base comment entity.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-03-20
 */
@Data
@Entity(name = "BaseComment")
@Table(name = "comments")
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.INTEGER, columnDefinition = "int default 0")
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class BaseComment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Commentator's name.
     */
    @Column(name = "author", columnDefinition = "varchar(50) not null")
    private String author;

    /**
     * Commentator's email.
     */
    @Column(name = "email", columnDefinition = "varchar(255) not null")
    private String email;

    /**
     * Commentator's ip address.
     */
    @Column(name = "ip_address", columnDefinition = "varchar(127) default ''")
    private String ipAddress;

    /**
     * Commentator's website.
     */
    @Column(name = "author_url", columnDefinition = "varchar(512) default ''")
    private String authorUrl;

    /**
     * Gravatar md5
     */
    @Column(name = "gravatar_md5", columnDefinition = "varchar(128) default ''")
    private String gravatarMd5;

    /**
     * Comment content.
     */
    @Column(name = "content", columnDefinition = "varchar(1023) not null")
    private String content;

    /**
     * Comment status.
     */
    @Column(name = "status", columnDefinition = "int default 1")
    private CommentStatus status;

    /**
     * Commentator's userAgent.
     */
    @Column(name = "user_agent", columnDefinition = "varchar(512) default ''")
    private String userAgent;

    /**
     * Is admin's comment.
     */
    @Column(name = "is_admin", columnDefinition = "tinyint default 0")
    private Boolean isAdmin;

    /**
     * Allow notification.
     */
    @Column(name = "allow_notification", columnDefinition = "tinyint default 1")
    private Boolean allowNotification;

    /**
     * Post id.
     */
    @Column(name = "post_id", columnDefinition = "int not null")
    private Integer postId;

    /**
     * Whether to top the comment.
     */
    @Column(name = "top_priority", columnDefinition = "int default 0")
    private Integer topPriority;

    /**
     * Parent comment.
     */
    @Column(name = "parent_id", columnDefinition = "bigint default 0")
    private Long parentId;

    @Override
    public void prePersist() {
        super.prePersist();

        if (ServiceUtils.isEmptyId(id)) {
            id = null;
        }

        if (ServiceUtils.isEmptyId(parentId)) {
            parentId = 0L;
        }

        if (ipAddress == null) {
            ipAddress = "";
        }

        if (authorUrl == null) {
            authorUrl = "";
        }

        if (gravatarMd5 == null) {
            gravatarMd5 = "";
        }

        if (status == null) {
            status = CommentStatus.AUDITING;
        }

        if (userAgent == null) {
            userAgent = "";
        }

        if (isAdmin == null) {
            isAdmin = false;
        }

        if (allowNotification == null) {
            allowNotification = true;
        }
    }
}
