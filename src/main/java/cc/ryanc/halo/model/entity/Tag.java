package cc.ryanc.halo.model.entity;

import cc.ryanc.halo.utils.DateUtils;
import lombok.Data;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

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
@Table(name = "menus")
@SQLDelete(sql = "update menus set deleted = true where id = ?")
@Where(clause = "deleted = false")
public class Tag {

    @Id
    @GeneratedValue
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
    @Column(name = "snake_name", columnDefinition = "varchar(255) not null")
    private String snakeName;

    /**
     * 创建时间戳
     */
    @Column(name = "create_time", columnDefinition = "timestamp default CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    /**
     * 更新时间戳
     */
    @Column(name = "update_time", columnDefinition = "timestamp default CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;

    /**
     * 是否已删除
     */
    @Column(name = "deleted", columnDefinition = "TINYINT default 0")
    private Boolean deleted;

    @PrePersist
    public void prePersist() {
        id = null;
    }

    @PreUpdate
    public void preUpdate() {
        if (updateTime == null) {
            updateTime = DateUtils.now();
        }
    }
}
