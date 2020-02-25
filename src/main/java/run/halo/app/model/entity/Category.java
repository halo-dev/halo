package run.halo.app.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;

/**
 * Category entity.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-03-15
 */
@Data
@Entity
@Table(name = "categories")
@ToString
@EqualsAndHashCode(callSuper = true)
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Category name.
     */
    @Column(name = "name", columnDefinition = "varchar(255) not null")
    private String name;

    /**
     * Category slug name.
     */
    @Deprecated
    @Column(name = "slug_name", columnDefinition = "varchar(50) not null", unique = true)
    private String slugName;

    /**
     * Category slug.
     */
    @Column(name = "slug", columnDefinition = "varchar(255)", unique = true)
    private String slug;

    /**
     * Description,can be display on category page.
     */
    @Column(name = "description", columnDefinition = "varchar(100) default ''")
    private String description;

    /**
     * Cover thumbnail of the category.
     */
    @Column(name = "thumbnail", columnDefinition = "varchar(1023) default ''")
    private String thumbnail;

    /**
     * Parent category.
     */
    @Column(name = "parent_id", columnDefinition = "int default 0")
    private Integer parentId;

    @Override
    public void prePersist() {
        super.prePersist();
        id = null;

        if (description == null) {
            description = "";
        }

        if (parentId == null || parentId < 0) {
            parentId = 0;
        }
    }

}
