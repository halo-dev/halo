package cc.ryanc.halo.model.entity;

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
@EqualsAndHashCode
public class Comment {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "create_time", columnDefinition = "timestamp default CURRENT_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    @Column(name = "update_time", columnDefinition = "timestamp default CURRENT_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;

    @Column(name = "deleted", columnDefinition = "tinyint default 0")
    private Boolean deleted;

    @Column(name = "author", columnDefinition = "varchar(50) not null")
    private String author;

    @Column(name = "email", columnDefinition = "varchar(50) default ''")
    private String email;

    @Column(name = "ip_address", columnDefinition = "varchar(127) default ''")
    private String ipAddress;

    @Column(name = "gavatar_md5", columnDefinition = "varchar(128) default ''")
    private String gavatarMd5;

    @Column(name = "content", columnDefinition = "varchar(1024) not null")
    private String content;

    @Column(name = "user_agent", columnDefinition = "varchar(512) default ''")
    private String userAgent;

    @Column(name = "is_admin", columnDefinition = "tinyint default 0")
    private Boolean isAdmin;

    @Column(name = "parent_id", columnDefinition = "bigint default 0")
    private Long parentId;
}
