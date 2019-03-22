package cc.ryanc.halo.model.entity;

import cc.ryanc.halo.utils.DateUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

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
@ToString(callSuper = true)
public class PostCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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


    @Override
    public void prePersist() {
        super.prePersist();
        id = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PostCategory that = (PostCategory) o;
        return categoryId.equals(that.categoryId) &&
                postId.equals(that.postId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoryId, postId);
    }
}
