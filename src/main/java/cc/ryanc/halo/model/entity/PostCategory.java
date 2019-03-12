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

    @Column(name = "create_time", columnDefinition = "timestamp default CURRENT_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    @Column(name = "update_time", columnDefinition = "timestamp default CURRENT_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;

    @Column(name = "deleted", columnDefinition = "tinyint default 0")
    private Boolean deleted;

    @Column(name = "category_id")
    private Integer categoryId;

    @Column(name = "post_id")
    private Integer postId;
}
