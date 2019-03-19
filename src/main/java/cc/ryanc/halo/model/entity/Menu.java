package cc.ryanc.halo.model.entity;

import cc.ryanc.halo.utils.DateUtils;
import lombok.Data;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.Date;

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
public class Menu {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Integer id;

    /**
     * 菜单名称
     */
    @Column(name = "name", columnDefinition = "varchar(255) not null")
    private String name;

    /**
     * 菜单地址
     */
    @Column(name = "url", columnDefinition = "varchar(255) not null")
    private String url;

    /**
     * 排序
     */
    @Column(name = "sort", columnDefinition = "int default 0")
    private Integer sort;

    /**
     * 窗口打开方式
     */
    @Column(name = "target", columnDefinition = "varchar(20) default '_self'")
    private String target;

    /**
     * 菜单图标
     */
    @Column(name = "icon", columnDefinition = "varchar(50) default ''")
    private String icon;

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
