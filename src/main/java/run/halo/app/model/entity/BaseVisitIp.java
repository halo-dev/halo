package run.halo.app.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

/**
 * Base visit ip address entity.
 *
 * @author KristenLawrence
 * @date 2020-05-07
 */
@Data
@Entity(name = "BaseVisitIp")
@Table(name = "visit_ip", indexes = {@Index(name = "base_visit_ip_post_id", columnList = "post_id")})
//@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.INTEGER, columnDefinition = "int default 0")
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class BaseVisitIp extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "custom-id")
    @GenericGenerator(name = "custom-id", strategy = "run.halo.app.model.entity.support.CustomIdGenerator")
    private Long id;

    /**
     * Post/Sheet id.
     */
    @Column(name = "post_id", nullable = false)
    private Integer postId;

    /**
     * Visitor's ip address.
     */
    @Column(name = "ip_address", length = 127, nullable = false)
    private String ipAddress;
}
