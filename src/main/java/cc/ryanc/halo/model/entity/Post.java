package cc.ryanc.halo.model.entity;

import cc.ryanc.halo.model.enums.PostCreateFrom;
import cc.ryanc.halo.model.enums.PostStatus;
import cc.ryanc.halo.model.enums.PostType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.Date;

/**
 * Post entity.
 *
 * @author johnniang
 */
@Entity
@Table(name = "posts")
@SQLDelete(sql = "update posts set deleted = true where id = ?")
@Where(clause = "deleted = false")
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 文章标题
     */
    @Column(name = "title", columnDefinition = "varchar(100) not null")
    private String title;

    /**
     * 文章类型
     * 0: 普通文章
     * 1: 自定义页面
     * 2: 日志
     */
    @Column(name = "type", columnDefinition = "int default 0")
    private PostType type;

    /**
     * Post status.
     */
    @Column(name = "status", columnDefinition = "int default 1")
    private PostStatus status;

    /**
     * Post url.
     */
    @Column(name = "url", columnDefinition = "varchar(255) not null")
    private String url;

    /**
     * 源内容
     */
    @Column(name = "original_content", columnDefinition = "text not null")
    private String originalContent;

    /**
     * 渲染后内容
     */
    @Column(name = "format_content", columnDefinition = "text not null")
    private String formatContent;

    /**
     * 摘要
     */
    @Column(name = "summary", columnDefinition = "varchar(500) default ''")
    private String summary;

    /**
     * 缩略图
     */
    @Column(name = "thumbnail", columnDefinition = "varchar(1023) default ''")
    private String thumbnail;

    /**
     * 浏览量
     */
    @Column(name = "visits", columnDefinition = "bigint default 0")
    private Long visits;

    /**
     * 是否允许评论
     */
    @Column(name = "disallow_comment", columnDefinition = "int default 0")
    private Boolean disallowComment;

    /**
     * 文章密码
     */
    @Column(name = "password", columnDefinition = "varchar(255) default ''")
    private String password;

    /**
     * 自定义渲染模板名称
     */
    @Column(name = "template", columnDefinition = "varchar(255) default ''")
    private String template;

    /**
     * 是否置顶
     */
    @Column(name = "top_priority", columnDefinition = "int default 0")
    private Integer topPriority;

    /**
     * 发布来源
     */
    @Column(name = "create_from", columnDefinition = "int default 0")
    private PostCreateFrom createFrom;

    /**
     * 点赞量/喜欢量
     */
    @Column(name = "likes", columnDefinition = "bigint default 0")
    private Long likes;

    /**
     * Edit time.
     */
    @Column(name = "edit_time", columnDefinition = "timestamp default CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date editTime;

    @Override
    public void prePersist() {
        super.prePersist();
        id = null;
    }

}
