package run.halo.app.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.GenericGenerator;

/**
 * Link entity
 *
 * @author ryanwang
 * @date 2019-03-12
 */
@Data
@Entity
@Table(name = "links", indexes = {@Index(name = "links_name", columnList = "name")})
@ToString
@EqualsAndHashCode(callSuper = true)
public class Link extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "custom-id")
    @GenericGenerator(name = "custom-id",
        strategy = "run.halo.app.model.entity.support.CustomIdGenerator")
    private Integer id;

    /**
     * Link name.
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Link website address.
     */
    @Column(name = "url", length = 1023, nullable = false)
    private String url;

    /**
     * Website logo.
     */
    @Column(name = "logo", length = 1023)
    private String logo;

    /**
     * Website description.
     */
    @Column(name = "description")
    private String description;

    /**
     * Link team name.
     */
    @Column(name = "team")
    private String team;

    /**
     * Sort.
     */
    @Column(name = "priority")
    @ColumnDefault("0")
    private Integer priority;

    @Override
    public void prePersist() {
        super.prePersist();

        if (priority == null) {
            priority = 0;
        }

        if (logo == null) {
            logo = "";
        }

        if (description == null) {
            description = "";
        }

        if (team == null) {
            team = "";
        }
    }
}
