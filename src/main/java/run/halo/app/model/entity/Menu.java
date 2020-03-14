package run.halo.app.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;

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
    @Column(name = "name", length = 50, nullable = false)
    private String name;

    /**
     * Menu access url.
     */
    @Column(name = "url", length = 1023, nullable = false)
    private String url;

    /**
     * Sort.
     */
    @Column(name = "priority")
    @ColumnDefault("0")
    private Integer priority;

    /**
     * Page opening method
     */
    @Column(name = "target", length = 20)
    @ColumnDefault("'_self'")
    private String target;

    /**
     * Menu icon,Template support required.
     */
    @Column(name = "icon", length = 50)
    private String icon;

    /**
     * Parent menu.
     */
    @Column(name = "parent_id")
    @ColumnDefault("0")
    private Integer parentId;

    /**
     * Menu team name.
     */
    @Column(name = "team")
    private String team;

    @Override
    public void prePersist() {
        super.prePersist();

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
