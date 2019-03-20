package cc.ryanc.halo.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.util.Date;

/**
 * Tag entity
 *
 * @author : RYAN0UP
 * @date : 2019-03-12
 */
@Data
@Entity
@Table(name = "tags")
@SQLDelete(sql = "update tags set deleted = true where id = ?")
@Where(clause = "deleted = false")
@ToString
@EqualsAndHashCode(callSuper = true)
public class Tag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /**
     * 标签名
     */
    @Column(name = "name", columnDefinition = "varchar(255) not null")
    private String name;

    /**
     * 缩略名
     */
    @Column(name = "slug_name", columnDefinition = "varchar(255) not null")
    private String slugName;

    @Override
    protected void prePersist() {
        super.prePersist();
        id = null;
    }
}
