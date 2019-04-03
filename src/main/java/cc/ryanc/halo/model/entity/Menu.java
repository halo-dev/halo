package cc.ryanc.halo.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * Menu entity
 *
 * @author : RYAN0UP
 * @date : 2019-03-12
 */
@Data
@Entity
@Table(name = "menus")
@SQLDelete(sql = "update menus set deleted = true where id = ?")
@Where(clause = "deleted = false")
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
    @Column(name = "sort", columnDefinition = "int default 0")
    private Integer sort;

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


    @Override
    public void prePersist() {
        super.prePersist();

        id = null;

        if (sort == null) {
            sort = 0;
        }

        if (target == null) {
            target = "_self";
        }

        if (icon == null) {
            icon = "";
        }
    }
}
