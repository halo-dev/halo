package cc.ryanc.halo.model.entity;

import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
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
     * Category id.
     */
    @Column(name = "category_id")
    private Integer categoryId;

    /**
     * Post id.
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
