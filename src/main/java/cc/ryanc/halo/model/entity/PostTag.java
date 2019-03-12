package cc.ryanc.halo.model.entity;

import lombok.Data;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.Date;

/**
 * @author : RYAN0UP
 * @date : 2019-03-12
 */
@Data
@Entity
@Table(name = "post_tags")
@SQLDelete(sql = "update post_tags set deleted = true where id = ?")
@Where(clause = "deleted = false")
public class PostTag {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Integer id;

    /**
     * 文章编号
     */
    @Column(name = "post_id", columnDefinition = "int not null")
    private Integer postId;

    /**
     * 标签编号
     */
    @Column(name = "tag_id", columnDefinition = "int not null")
    private Integer tagId;

    /**
     * 创建时间戳
     */
    @Column(name = "create_time", columnDefinition = "timestamp default CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    /**
     * 更新时间戳
     */
    @Column(name = "update_time", columnDefinition = "timestamp default CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;

    /**
     * 是否已删除
     */
    @Column(name = "deleted", columnDefinition = "TINYINT default 0")
    private Boolean deleted;
}
