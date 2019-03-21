package cc.ryanc.halo.model.entity;

import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.Objects;

/**
 * Post tag entity.
 *
 * @author : RYAN0UP
 * @date : 2019-03-12
 */
@Data
@Entity
@Table(name = "post_tags")
@SQLDelete(sql = "update post_tags set deleted = true where id = ?")
@Where(clause = "deleted = false")
@ToString
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PostTag postTag = (PostTag) o;
        return Objects.equals(postId, postTag.postId) &&
                Objects.equals(tagId, postTag.tagId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), postId, tagId);
    }
}
