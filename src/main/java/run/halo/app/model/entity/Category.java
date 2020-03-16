package run.halo.app.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.GenericGenerator;

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
@Table(name = "categories",
    indexes = {@Index(name = "categories_name", columnList = "name"),
        @Index(name = "categories_parent_id", columnList = "parent_id")})
@ToString
@EqualsAndHashCode(callSuper = true)
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "custom-id")
    @GenericGenerator(name = "custom-id", strategy = "run.halo.app.model.entity.support.CustomIdGenerator")
    private Integer id;

    /**
     * Category name.
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Category slug name.
     */
    @Deprecated
    @Column(name = "slug_name")
    private String slugName;

    /**
     * Category slug.
     */
    @Column(name = "slug", unique = true)
    private String slug;

    /**
     * Description,can be display on category page.
     */
    @Column(name = "description", length = 100)
    private String description;

    /**
     * Cover thumbnail of the category.
     */
    @Column(name = "thumbnail", length = 1023)
    private String thumbnail;

    /**
     * Parent category.
     */
    @Column(name = "parent_id")
    @ColumnDefault("0")
    private Integer parentId;

    @Override
    public void prePersist() {
        super.prePersist();

        if (description == null) {
            description = "";
        }

        if (parentId == null || parentId < 0) {
            parentId = 0;
        }
    }

}
