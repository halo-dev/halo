package run.halo.app.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.persistence.*;

/**
 * Category entity.
 *
 * @author johnniang
 */
@Entity
@Table(name = "categories")
@SQLDelete(sql = "update categories set deleted = true where id = ?")
@Where(clause = "deleted = false")
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Category name.
     */
    @Column(name = "name", columnDefinition = "varchar(50) not null")
    private String name;

    /**
     * Category slug name.
     */
    @Column(name = "slug_name", columnDefinition = "varchar(50) not null")
    private String slugName;

    /**
     * Description,can be display on category page.
     */
    @Column(name = "description", columnDefinition = "varchar(100) default ''")
    private String description;

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
