package run.halo.app.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;

/**
 * Link entity
 *
 * @author ryanwang
 * @date 2019-03-12
 */
@Data
@Entity
@Table(name = "links")
@ToString
@EqualsAndHashCode(callSuper = true)
public class Link extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /**
     * Link name.
     */
    @Column(name = "name", columnDefinition = "varchar(255) not null")
    private String name;

    /**
     * Link website address.
     */
    @Column(name = "url", columnDefinition = "varchar(1023) not null")
    private String url;

    /**
     * Website logo.
     */
    @Column(name = "logo", columnDefinition = "varchar(1023) default ''")
    private String logo;

    /**
     * Website description.
     */
    @Column(name = "description", columnDefinition = "varchar(255) default ''")
    private String description;

    /**
     * Link team name.
     */
    @Column(name = "team", columnDefinition = "varchar(255) default ''")
    private String team;

    /**
     * Sort.
     */
    @Column(name = "priority", columnDefinition = "int default 0")
    private Integer priority;

    @Override
    public void prePersist() {
        super.prePersist();

        id = null;

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
