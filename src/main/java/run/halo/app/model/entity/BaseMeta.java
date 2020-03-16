package run.halo.app.model.entity;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

/**
 * Base meta entity.
 *
 * @author ryanwang
 * @author ikaisec
 * @date 2019-08-04
 */
@Data
@Entity(name = "BaseMeta")
@Table(name = "metas")
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.INTEGER, columnDefinition = "int default 0")
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class BaseMeta extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "custom-id")
    @GenericGenerator(name = "custom-id", strategy = "run.halo.app.model.entity.support.CustomIdGenerator")
    private Long id;

    /**
     * Post id.
     */
    @Column(name = "post_id", nullable = false)
    private Integer postId;

    /**
     * meta key
     */
    @Column(name = "meta_key", nullable = false)
    private String key;

    /**
     * meta value
     */
    @Column(name = "meta_value", length = 1023, nullable = false)
    private String value;
}
