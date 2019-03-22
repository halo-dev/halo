package cc.ryanc.halo.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

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
     * 分类名称
     */
    @Column(name = "name", columnDefinition = "varchar(50) not null")
    private String name;

    /**
     * 缩略名
     */
    @Column(name = "slug_name", columnDefinition = "varchar(50) not null")
    private String slugName;

    /**
     * 描述
     */
    @Column(name = "description", columnDefinition = "varchar(100) default ''")
    private String description;

    /**
     * 上级目录
     */
    @Column(name = "parent_id", columnDefinition = "int default 0")
    private Integer parentId;

    @Override
    public void prePersist() {
        super.prePersist();
        id = null;
    }

}
