package cc.ryanc.halo.model.entity;

import cc.ryanc.halo.model.enums.CommentStatus;
import cc.ryanc.halo.utils.DateUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.Date;

/**
 * Comment entity.
 *
 * @author johnniang
 */
@Entity
@Table(name = "comments")
@SQLDelete(sql = "update comments set deleted = true where id = ?")
@Where(clause = "deleted = false")
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 评论者昵称
     */
    @Column(name = "author", columnDefinition = "varchar(50) not null")
    private String author;

    /**
     * 评论者邮箱
     */
    @Column(name = "email", columnDefinition = "varchar(50) default ''")
    private String email;

    /**
     * 评论者 ip 地址
     */
    @Column(name = "ip_address", columnDefinition = "varchar(127) default ''")
    private String ipAddress;

    /**
     * 评论者网址
     */
    @Column(name = "author_url", columnDefinition = "varchar(512) default ''")
    private String authorUrl;

    /**
     * Gavatar md5
     */
    @Column(name = "gavatar_md5", columnDefinition = "varchar(128) default ''")
    private String gavatarMd5;

    /**
     * 评论内容
     */
    @Column(name = "content", columnDefinition = "varchar(1023) not null")
    private String content;

    /**
     * Comment status.
     */
    @Column(name = "status", columnDefinition = "int default 1")
    private CommentStatus status;

    /**
     * UA 信息
     */
    @Column(name = "user_agent", columnDefinition = "varchar(512) default ''")
    private String userAgent;

    /**
     * 是否为博主
     */
    @Column(name = "is_admin", columnDefinition = "tinyint default 0")
    private Boolean isAdmin;

    /**
     * Post id.
     */
    @Column(name = "post_id", columnDefinition = "int not null")
    private Integer postId;

    /**
     * 是否置顶
     */
    @Column(name = "top_priority", columnDefinition = "int default 0")
    private Integer topPriority;

    /**
     * 上级评论
     */
    @Column(name = "parent_id", columnDefinition = "bigint default 0")
    private Long parentId;

    @Override
    public void prePersist() {
        super.prePersist();
        id = null;
    }

}
