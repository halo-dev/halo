package cc.ryanc.halo.model.entity;

import cc.ryanc.halo.utils.DateUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
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
@ToString
@EqualsAndHashCode(callSuper = true)
public class PostTag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Override
    public void prePersist() {
        super.prePersist();
        id = null;
    }
}
