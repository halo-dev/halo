package cc.ryanc.halo.model.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * Gallery entity
 *
 * @author : RYAN0UP
 * @date : 2019-03-12
 */
@Data
@Entity
@Table(name = "galleries")
public class Gallery {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Integer id;

    /**
     * 图片名称
     */
    @Column(name = "name", columnDefinition = "varchar(255) not null")
    private String name;

    /**
     * 描述
     */
    @Column(name = "description", columnDefinition = "varchar(255) default ''")
    private String description;

    /**
     * 拍摄时间/创作时间
     */
    @Column(name = "take_time", columnDefinition = "timestamp not null")
    @Temporal(TemporalType.TIMESTAMP)
    private Date takeTime;

    /**
     * 拍摄地点
     */
    @Column(name = "location", columnDefinition = "varchar(255) default ''")
    private String location;

    /**
     * 缩略图
     */
    @Column(name = "thumbnail", columnDefinition = "varchar(1023) default ''")
    private String thumbnail;

    /**
     * 图片地址
     */
    @Column(name = "url", columnDefinition = "varchar(1023) not null")
    private String url;

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
