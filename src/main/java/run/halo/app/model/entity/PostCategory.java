package run.halo.app.model.entity;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

/**
 * Post category entity.
 *
 * @author johnniang
 */
@Getter
@Setter
@ToString(callSuper = true)
@RequiredArgsConstructor
@Entity
@Table(name = "post_categories", indexes = {
    @Index(name = "post_categories_post_id", columnList = "post_id"),
    @Index(name = "post_categories_category_id", columnList = "category_id")})
public class PostCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "custom-id")
    @GenericGenerator(name = "custom-id",
        strategy = "run.halo.app.model.entity.support.CustomIdGenerator")
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PostCategory that = (PostCategory) o;
        return categoryId.equals(that.categoryId)
            && postId.equals(that.postId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoryId, postId);
    }
}
