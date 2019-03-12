package cc.ryanc.halo.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.Date;

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
@EqualsAndHashCode
public class Category {

    @Id
    @GeneratedValue
    private Integer id;

    @Column(name = "create_time", columnDefinition = "timestamp default CURRENT_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    @Column(name = "update_time", columnDefinition = "timestamp default CURRENT_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;

    @Column(name = "deleted", columnDefinition = "tinyint default 0")
    private Boolean deleted;

    @Column(name = "name", columnDefinition = "varchar(50) not null")
    private String name;

    @Column(name = "snake_name", columnDefinition = "varchar(50) not null")
    private String snakeName;

    @Column(name = "description", columnDefinition = "varchar(100) default ''")
    private String description;

    @Column(name = "parent_id", columnDefinition = "int default 0")
    private Integer parentId;

}
