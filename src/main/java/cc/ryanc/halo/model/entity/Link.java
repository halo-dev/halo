package cc.ryanc.halo.model.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * Link entity
 *
 * @author : RYAN0UP
 * @date : 2019-03-12
 */
@Data
@Entity
@Table(name = "links")
public class Link {

    @Id
    @GeneratedValue
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
    @Column(name = "group", columnDefinition = "varchar(255) default ''")
    private String group;

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
}
