package run.halo.app.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

/**
 * Post visit entity.
 *
 * @author chao19991005
 */
@Data
@Entity
@Table(name = "visits",
    indexes = {@Index(name = "visit_post_id", columnList = "post_id"),
            @Index(name = "visit_visit_id", columnList = "visit_id")})
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Visit extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "custom-id")
    @GenericGenerator(name = "custom-id", strategy = "run.halo.app.model.entity.support.CustomIdGenerator")
    private Integer id;

    /**
     * visit id.
     */
    @Column(name = "visit_id")
    private Integer visitId;

    /**
     * Post id.
     */
    @Column(name = "post_id")
    private Integer postId;

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) {
//            return true;
//        }
//        if (o == null || getClass() != o.getClass()) {
//            return false;
//        }
//        Visit visit = (Visit) o;
//        return Objects.equals(postId, visit.postId) &&
//                Objects.equals(visitId, visit.visitId);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(postId, visitId);
//    }
}
