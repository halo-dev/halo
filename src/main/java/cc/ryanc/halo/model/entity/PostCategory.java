package cc.ryanc.halo.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.Date;

/**
 * Post category entity.
 *
 * @author johnniang
 */
@Entity
@Table(name = "post_categories")
@SQLDelete(sql = "update post_categories set deleted = true where id = ?")
@Where(clause = "deleted = false")
@Data
@ToString
@EqualsAndHashCode
public class PostCategory {

    @Id
    @GeneratedValue
    private Integer id;

    /**
     * 分类目录编号
     */
    @Column(name = "category_id")
    private Integer categoryId;

    /**
     * 文章编号
     */
    @Column(name = "post_id")
    private Integer postId;

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
