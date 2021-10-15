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
 * Post tag entity.
 *
 * @author ryanwang
 * @date 2019-03-12
 */
@Getter
@Setter
@ToString(callSuper = true)
@RequiredArgsConstructor
@Entity
@Table(name = "post_tags", indexes = {
    @Index(name = "post_tags_post_id", columnList = "post_id"),
    @Index(name = "post_tags_tag_id", columnList = "tag_id")})
public class PostTag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "custom-id")
    @GenericGenerator(name = "custom-id",
        strategy = "run.halo.app.model.entity.support.CustomIdGenerator")
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
        return Objects.equals(postId, postTag.postId)
            && Objects.equals(tagId, postTag.tagId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(postId, tagId);
    }
}
