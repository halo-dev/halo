package cc.ryanc.halo.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * Link entity
 *
 * @author : RYAN0UP
 * @date : 2019-03-12
 */
@Data
@Entity
@Table(name = "links")
@SQLDelete(sql = "update links set deleted = true where id = ?")
@Where(clause = "deleted = false")
@ToString
@EqualsAndHashCode(callSuper = true)
public class Link extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /**
     * 友链名称
     */
    @Column(name = "name", columnDefinition = "varchar(255) not null")
    private String name;

    /**
     * 友链地址
     */
    @Column(name = "url", columnDefinition = "varchar(255) not null")
    private String url;

    /**
     * 友链 Logo
     */
    @Column(name = "logo", columnDefinition = "varchar(255) default ''")
    private String logo;

    /**
     * 描述
     */
    @Column(name = "description", columnDefinition = "varchar(255) default ''")
    private String description;

    /**
     * 分组
     */
    @Column(name = "team", columnDefinition = "varchar(255) default ''")
    private String team;


    @Override
    public void prePersist() {
        super.prePersist();
        id = null;
    }
}
