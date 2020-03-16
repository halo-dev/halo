package run.halo.app.model.entity;

import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Objects;

/**
 * Post tag entity.
 *
 * @author ryanwang
 * @date 2019-03-12
 */
@Data
@Entity
@Table(name = "post_tags")
@ToString(callSuper = true)
public class PostTag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "custom-id")
    @GenericGenerator(name = "custom-id", strategy = "run.halo.app.model.entity.support.CustomIdGenerator")
    private Integer id;

    /**
     * Post id.
     */
    @Column(name = "post_id", nullable = false)
    private Integer postId;

    /**
     * Tag id.
     */
    @Column(name = "tag_id", nullable = false)
    private Integer tagId;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PostTag postTag = (PostTag) o;
        return Objects.equals(postId, postTag.postId) &&
            Objects.equals(tagId, postTag.tagId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(postId, tagId);
    }
}
