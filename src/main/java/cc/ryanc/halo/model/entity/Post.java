package cc.ryanc.halo.model.entity;

import cc.ryanc.halo.model.entity.enums.PostCreateFrom;
import cc.ryanc.halo.model.entity.enums.PostType;
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
@EqualsAndHashCode
public class Post {

    @Id
    @GeneratedValue
    private Integer id;

    @Column(name = "create_time", columnDefinition = "timestamp default CURRENT_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    @Column(name = "update_time", columnDefinition = "timestamp default CURRENT_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;

    @Column(name = "deleted", columnDefinition = "tinyint default 0")
    private Boolean deleted;

    @Column(name = "title", columnDefinition = "varchar(100) not null")
    private String title;

    @Column(name = "type", columnDefinition = "int default 0")
    private PostType type;

    @Column(name = "original_content", columnDefinition = "text not null")
    private String originalContent;

    @Column(name = "format_content", columnDefinition = "text not null")
    private String formatContent;

    @Column(name = "summary", columnDefinition = "varchar(500) default ''")
    private String summary;

    @Column(name = "thumbnail", columnDefinition = "varchar(1023) default ''")
    private String thumbnail;

    @Column(name = "visits", columnDefinition = "bigint default 0")
    private Long visits;

    @Column(name = "disallow_comment", columnDefinition = "int default 0")
    private Boolean disallowComment;

    @Column(name = "password", columnDefinition = "varchar(255) default ''")
    private String password;

    @Column(name = "template", columnDefinition = "varchar(255) default ''")
    private String template;

    @Column(name = "top_priority", columnDefinition = "int default 0")
    private Integer topPriority;

    @Column(name = "create_from", columnDefinition = "int default 0")
    private PostCreateFrom createFrom;

    @Column(name = "likes", columnDefinition = "bigint default 0")
    private Long likes;
}
