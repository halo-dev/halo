package run.halo.app.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;

/**
 * Menu entity
 *
 * @author ryanwang
 * @date 2019-03-12
 */
@Data
@Entity
@Table(name = "menus")
@ToString
@EqualsAndHashCode(callSuper = true)
public class Menu extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /**
     * Menu name.
     */
    @Column(name = "name", columnDefinition = "varchar(50) not null")
    private String name;

    /**
     * Menu access url.
     */
    @Column(name = "url", columnDefinition = "varchar(1023) not null")
    private String url;

    /**
     * Sort.
     */
    @Column(name = "priority", columnDefinition = "int default 0")
    private Integer priority;

    /**
     * Page opening method
     */
    @Column(name = "target", columnDefinition = "varchar(20) default '_self'")
    private String target;

    /**
     * Menu icon,Template support required.
     */
    @Column(name = "icon", columnDefinition = "varchar(50) default ''")
    private String icon;

    /**
     * Parent menu.
     */
    @Column(name = "parent_id", columnDefinition = "int default 0")
    private Integer parentId;

    /**
     * Menu team name.
     */
    @Column(name = "team", columnDefinition = "varchar(255) default ''")
    private String team;

    @Override
    public void prePersist() {
        super.prePersist();

        id = null;

        if (priority == null) {
            priority = 0;
        }

        if (target == null) {
            target = "_self";
        }

        if (icon == null) {
            icon = "";
        }

        if (parentId == null) {
            parentId = 0;
        }

        if (team == null) {
            team = "";
        }
    }
}
