package run.halo.app.model.entity;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Post id.
     */
    @Column(name = "post_id", columnDefinition = "int not null")
    private Integer postId;

    /**
     * meta key
     */
    @Column(name = "meta_key", columnDefinition = "varchar(100) not null")
    private String key;

    /**
     * meta value
     */
    @Column(name = "meta_value", columnDefinition = "varchar(1023) not null")
    private String value;
}
